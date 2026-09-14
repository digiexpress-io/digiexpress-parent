package io.resys.metis.search.spi;

/*-
 * #%L
 * metis-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import io.resys.metis.search.api.ImmutableMetisSearchIndexStatus;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchConfig;
import io.resys.metis.search.api.MetisSearchIndexStatus;
import io.resys.metis.search.api.MetisSearchIndexStatus.JobState;
import io.resys.metis.search.api.MetisSearchResult;
import io.resys.metis.search.spi.index.IndexingService;
import io.resys.metis.search.spi.index.ReindexJobService;
import io.resys.metis.search.spi.query.FtsSearchService;
import io.resys.metis.search.spi.query.HybridSearchService;
import io.resys.metis.search.spi.query.VectorSearchService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MetisSearchClientImpl implements MetisSearchClient {

  private final MetisSearchConfig config;
  private final HybridSearchService hybridSearch;
  private final VectorSearchService vectorSearch;
  private final FtsSearchService ftsSearch;
  private final IndexingService indexingService;
  private final ReindexJobService jobService;
  private final Executor reindexExecutor;
  private final Executor searchExecutor;
  private final String embeddingModel;

  private final AtomicLong runningJobId = new AtomicLong(NO_JOB);
  private static final long NO_JOB = -1;

  @Override
  public SearchQuery query() {
    return new SearchQueryImpl();
  }

  @Override
  public Uni<MetisSearchIndexStatus> startReindex(boolean force, boolean replace, String publicationId) {
    return blocking(() -> doStartReindex(force, replace, publicationId));
  }

  @Override
  public boolean isPublicationIndexed(String publicationId) {
    return jobService.isPublicationIndexed(publicationId, indexingService.currentBundleHash().orElse(null));
  }

  @Override
  public Uni<MetisSearchIndexStatus> cancelReindex() {
    return blocking(() -> {
      final var accepted = jobService.requestCancel(false, false);
      return latestStatus(accepted);
    });
  }

  private MetisSearchIndexStatus doStartReindex(boolean force, boolean replace, String publicationId) {
    if (replace && jobService.requestCancel(true, force)) {
      return latestStatus(true);
    }
    return claimAndSchedule(force, publicationId);
  }

  private MetisSearchIndexStatus claimAndSchedule(boolean force, String publicationId) {
    final var jobId = jobService.tryStart(embeddingModel, publicationId);
    if (jobId.isEmpty()) {
      return latestStatus(false);
    }
    final var claimed = jobId.getAsLong();
    runningJobId.set(claimed);
    try {
      reindexExecutor.execute(() -> {
        try {
          indexingService.runReindex(claimed, force);
        } finally {
          runningJobId.compareAndSet(claimed, NO_JOB);
          jobService.consumeRestart(claimed).ifPresent(restartForce -> {
            final var next = doStartReindex(restartForce, false, publicationId);
            if (!next.getAccepted()) {
              log.warn("Metis could not start the replacement reindex after cancelling job: {}",
                  claimed);
            }
          });
        }
      });
    } catch (RuntimeException e) {
      runningJobId.compareAndSet(claimed, NO_JOB);
      jobService.markFailed(claimed, "The reindex could not be scheduled: " + e.getMessage());
      throw e;
    }
    return ImmutableMetisSearchIndexStatus.builder()
        .accepted(true)
        .jobId(claimed)
        .state(JobState.RUNNING)
        .publicationId(publicationId)
        .build();
  }

  public void shutdown() {
    final var jobId = runningJobId.getAndSet(NO_JOB);
    if (jobId == NO_JOB) {
      return;
    }
    log.warn("Metis is stopping while reindex job: {} is running, releasing the claim", jobId);
    try {
      jobService.markFailed(jobId, "Application stopped while the job was running");
    } catch (Exception e) {
      log.warn("Metis could not release reindex job: {}, because of: {}", jobId, e.getMessage());
    }
  }

  @Override
  public Uni<MetisSearchIndexStatus> getIndexStatus() {
    return blocking(() -> latestStatus(true));
  }

  @Override
  public boolean isReindexInFlight() {
    return jobService.hasInflightJob();
  }

  @Override
  public boolean isIndexReadyForPortal() {
    return jobService.isIndexReadyForPortal();
  }

  @Override
  public long countIndexedDocuments() {
    return indexingService.countIndexedDocuments();
  }

  /** Worker pool: awaiting Thena SQL on the Vert.x loop deadlocks; reindexExecutor can be busy for hours. */
  private <T> Uni<T> blocking(java.util.function.Supplier<T> supplier) {
    return Uni.createFrom().item(supplier)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }

  private MetisSearchIndexStatus latestStatus(boolean accepted) {
    final var indexed = indexingService.countIndexedDocuments();
    return jobService.latest()
        .map(status -> (MetisSearchIndexStatus) ImmutableMetisSearchIndexStatus.builder()
            .from(status)
            .accepted(accepted)
            .indexedDocuments(indexed)
            .build())
        .orElseGet(() -> ImmutableMetisSearchIndexStatus.builder()
            .accepted(accepted)
            .state(accepted ? JobState.NONE : JobState.RUNNING)
            .indexedDocuments(indexed)
            .build());
  }

  private class SearchQueryImpl implements SearchQuery {
    private String locale;
    private int limit = config.getQuery().getDefaultLimit();
    private SearchMode mode = SearchMode.HYBRID;

    @Override
    public SearchQuery locale(String locale) {
      this.locale = locale == null ? null : locale.toLowerCase();
      return this;
    }

    @Override
    public SearchQuery limit(int limit) {
      this.limit = limit;
      return this;
    }

    @Override
    public SearchQuery mode(SearchMode mode) {
      this.mode = mode == null ? SearchMode.HYBRID : mode;
      return this;
    }

    @Override
    public Uni<List<MetisSearchResult>> findByText(String text) {
      if (text == null || text.isBlank()) {
        return Uni.createFrom().item(List.of());
      }
      if (locale == null || !config.getLocales().contains(locale)) {
        return Uni.createFrom().failure(new IllegalArgumentException(
            "Unsupported locale: '" + locale + "', supported: " + config.getLocales()));
      }
      final var query = config.getQuery();
      final var trimmed = text.trim();
      final var searchText = trimmed.length() > query.getMaxQueryChars()
          ? trimmed.substring(0, query.getMaxQueryChars())
          : trimmed;
      final var capped = Math.max(1, Math.min(limit, query.getMaxResults()));

      return Uni.createFrom().item(() -> switch (mode) {
            case VECTOR -> vectorSearch.rankedResults(searchText, locale, capped);
            case FTS -> ftsSearch.rankedResults(searchText, locale, capped);
            case HYBRID -> hybridSearch.rankedResults(searchText, locale, capped);
          })
          .runSubscriptionOn(searchExecutor)
          .ifNoItem().after(Duration.ofSeconds(query.getTimeoutSeconds())).fail();
    }
  }
}
