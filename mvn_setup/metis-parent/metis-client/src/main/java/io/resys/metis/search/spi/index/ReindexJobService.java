package io.resys.metis.search.spi.index;

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
import java.util.Optional;
import java.util.OptionalLong;

import io.resys.metis.search.api.MetisSearchIndexStatus;
import io.resys.metis.search.api.MetisSearchIndexStatus.JobState;
import io.resys.metis.search.spi.store.MetisSearchDb;
import io.resys.metis.search.spi.store.MetisSearchDbQuery;
import io.resys.metis.search.spi.store.MetisSearchSql;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReindexJobService {

  /** Measured from last progress, not from start, so a long healthy run is not reclaimed. */
  private final Duration abandonedAfter;
  private final MetisSearchDb db;

  public ReindexJobService(MetisSearchDb db) {
    this(db, Duration.ofHours(1));
  }

  public ReindexJobService(MetisSearchDb db, Duration abandonedAfter) {
    this.db = db;
    this.abandonedAfter = abandonedAfter;
  }

  public OptionalLong tryStart(String embeddingModel) {
    return tryStart(embeddingModel, null);
  }

  public OptionalLong tryStart(String embeddingModel, String publicationId) {
    failAbandonedJobs();
    final var id = MetisSearchSql.await(
        jobs().tryStart(embeddingModel, publicationId)
            .onFailure().recoverWithUni(error -> MetisSearchSql.isDuplicate(error)
                ? Uni.createFrom().item(Optional.empty())
                : Uni.createFrom().failure(error)));
    return id.map(OptionalLong::of).orElse(OptionalLong.empty());
  }

  public boolean isPublicationIndexed(String publicationId) {
    return isPublicationIndexed(publicationId, null);
  }

  public boolean isPublicationIndexed(String publicationId, String bundleHash) {
    if (publicationId == null || publicationId.isBlank()) {
      return false;
    }
    if (bundleHash == null || bundleHash.isBlank()) {
      return MetisSearchSql.await(jobs().countByPublication(publicationId)) > 0;
    }
    return MetisSearchSql.await(jobs().countByPublicationAndBundle(publicationId, bundleHash)) > 0;
  }

  public boolean isIndexReadyForPortal() {
    return latest()
        .map(status -> status.getState() == JobState.COMPLETED)
        .orElse(false);
  }

  public boolean requestCancel(boolean restart, boolean force) {
    return !MetisSearchSql.await(jobs().requestCancel(restart, force)).isEmpty();
  }

  public boolean hasInflightJob() {
    return MetisSearchSql.await(jobs().countInflight()) > 0;
  }

  public boolean isRunning(long jobId) {
    return MetisSearchSql.await(jobs().findStatus(jobId)).filter("RUNNING"::equals).isPresent();
  }

  public Optional<Boolean> consumeRestart(long jobId) {
    final var force = MetisSearchSql.await(jobs().findRestartForce(jobId));
    if (force.isEmpty()) {
      return Optional.empty();
    }
    MetisSearchSql.await(jobs().clearRestart(jobId));
    return force;
  }

  private void failAbandonedJobs() {
    final var released = MetisSearchSql.await(jobs().failAbandoned(abandonedAfter.toSeconds()));
    if (!released.isEmpty()) {
      log.warn("Metis released {} abandoned reindex job(s)", released.size());
    }
  }

  public void setTotal(long jobId, int total) {
    MetisSearchSql.await(jobs().setTotal(total, jobId));
  }

  public void setBundleHash(long jobId, String bundleHash) {
    MetisSearchSql.await(jobs().setBundleHash(bundleHash, jobId));
  }

  public void setSkipped(long jobId, int skipped) {
    MetisSearchSql.await(jobs().setSkipped(skipped, jobId));
  }

  /** Also the job heartbeat: abandoned jobs are detected from {@code last_progress_at}. */
  public void updateProgress(long jobId, int processed) {
    MetisSearchSql.await(jobs().updateProgress(processed, jobId));
  }

  public void markCompleted(long jobId, int processed, int skipped, long durationMs) {
    MetisSearchSql.await(jobs().markCompleted(processed, skipped, durationMs, jobId));
  }

  public void markFailed(long jobId, String error) {
    MetisSearchSql.await(jobs().markFailed(truncate(error), jobId));
  }

  public void markCancelled(long jobId, int processed, int skipped, long durationMs) {
    MetisSearchSql.await(jobs().markCancelled(processed, skipped, durationMs, jobId));
  }

  public Optional<MetisSearchIndexStatus> latest() {
    return MetisSearchSql.await(jobs().findLatest());
  }

  public Optional<MetisSearchIndexStatus> findById(long jobId) {
    return MetisSearchSql.await(jobs().findById(jobId));
  }

  private MetisSearchDbQuery.MetisSearchReindexJobQuery jobs() {
    return db.query().queryMetisSearchReindexJob();
  }

  private String truncate(String error) {
    if (error == null) {
      return null;
    }
    return error.length() > 2000 ? error.substring(0, 2000) : error;
  }
}
