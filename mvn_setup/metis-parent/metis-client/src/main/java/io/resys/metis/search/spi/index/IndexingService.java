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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.resys.metis.search.api.MetisSearchIndexingConfig;
import io.resys.metis.search.spi.store.MetisSearchDb;
import io.resys.metis.search.spi.store.MetisSearchKeepKeys;
import io.resys.metis.search.spi.store.MetisSearchSql;
import io.resys.metis.spi.ai.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class IndexingService {

  public static final String WIDTH_PROBE = "Metis embedding width probe";

  private final SiteContentReader contentReader;
  private final MetadataGenerator metadataGenerator;
  private final EmbeddingService embeddingService;
  private final ReindexJobService jobService;
  private final MetisSearchDb db;
  private final Executor indexingExecutor;
  private final MetisSearchIndexingConfig config;

  public void runReindex(long jobId, boolean force) {
    final var start = System.currentTimeMillis();
    var skipped = 0;
    var processed = 0;
    try {
      if (!jobService.isRunning(jobId)) {
        jobService.markCancelled(jobId, 0, 0, 0);
        return;
      }
      assertEmbeddingColumnWidth();
      assertEmbeddingModelWidth();

      final var documents = contentReader.readDocuments();
      final var bundleHash = contentReader.currentBundleHash();
      if (bundleHash != null) {
        jobService.setBundleHash(jobId, bundleHash.orElse(null));
      }
      jobService.setTotal(jobId, documents.size());
      if (!jobService.isRunning(jobId)) {
        jobService.markCancelled(jobId, 0, 0, System.currentTimeMillis() - start);
        return;
      }

      final var existingHashes = loadExistingHashes();
      final List<SiteSearchDocument> toProcess = new ArrayList<>();
      for (final var doc : documents) {
        final var existing = existingHashes.get(key(doc.workflowId(), doc.locale()));
        if (force || existing == null || !existing.equals(doc.contentHash())) {
          toProcess.add(doc);
        }
      }
      skipped = documents.size() - toProcess.size();
      jobService.setSkipped(jobId, skipped);
      log.info("Metis reindex job: {}, {} document(s), {} to process, {} skipped, force: {}",
          jobId, documents.size(), toProcess.size(), skipped, force);

      final var outcome = processDocuments(jobId, toProcess);
      processed = outcome.indexed();
      final var durationMs = System.currentTimeMillis() - start;

      if (outcome.cancelled() || !jobService.isRunning(jobId)) {
        jobService.markCancelled(jobId, processed, skipped, durationMs);
        log.warn("Metis reindex job: {} cancelled, {} of {} indexed, {} skipped",
            jobId, processed, toProcess.size(), skipped);
        return;
      }
      if (outcome.failed() > 0) {
        jobService.markFailed(jobId, "Incomplete reindex: " + processed + " of " + toProcess.size()
            + " documents indexed, " + outcome.failed() + " failed");
        log.error("Metis reindex job: {} incomplete, {} of {} indexed, {} failed, {} skipped",
            jobId, processed, toProcess.size(), outcome.failed(), skipped);
        return;
      }

      deleteStaleRows(documents);

      jobService.markCompleted(jobId, processed, skipped, durationMs);
      log.info("Metis reindex job: {} completed, {} of {} processed, {} skipped in {} ms",
          jobId, processed, toProcess.size(), skipped, durationMs);
    } catch (Exception e) {
      final var durationMs = System.currentTimeMillis() - start;
      if (!jobService.isRunning(jobId)) {
        jobService.markCancelled(jobId, processed, skipped, durationMs);
        return;
      }
      log.error("Metis reindex job: {} failed because of: {}", jobId, e.toString(), e);
      jobService.markFailed(jobId, e.toString());
    }
  }

  private ProcessOutcome processDocuments(long jobId, List<SiteSearchDocument> docs) {
    if (docs.isEmpty()) {
      return new ProcessOutcome(0, 0, false);
    }
    final var done = new AtomicInteger();
    final var indexed = new AtomicInteger();
    final var failed = new AtomicInteger();
    final var timeoutSeconds = Math.max(1, config.getDocumentTimeoutSeconds());
    final var concurrency = Math.max(1, config.getConcurrency());

    var next = 0;
    var cancelled = false;
    final List<CompletableFuture<Void>> inFlight = new ArrayList<>();

    while (true) {
      if (!jobService.isRunning(jobId)) {
        cancelled = true;
      }
      while (!cancelled && inFlight.size() < concurrency && next < docs.size()) {
        if (!jobService.isRunning(jobId)) {
          cancelled = true;
          break;
        }
        final var doc = docs.get(next++);
        inFlight.add(submitDocument(jobId, doc, docs.size(), timeoutSeconds, indexed, failed, done));
      }
      compactFinished(inFlight);
      if (inFlight.isEmpty()) {
        if (cancelled || next >= docs.size()) {
          break;
        }
        continue;
      }
      CompletableFuture.anyOf(inFlight.toArray(CompletableFuture[]::new)).join();
      compactFinished(inFlight);
    }
    return new ProcessOutcome(indexed.get(), failed.get(), cancelled);
  }

  private CompletableFuture<Void> submitDocument(
      long jobId,
      SiteSearchDocument doc,
      int total,
      int timeoutSeconds,
      AtomicInteger indexed,
      AtomicInteger failed,
      AtomicInteger done) {

    return CompletableFuture
        .runAsync(() -> indexDocument(jobId, doc), indexingExecutor)
        .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .handle((ignored, error) -> {
          if (error == null) {
            indexed.incrementAndGet();
          } else if (isCancellation(error)) {
            log.info("Metis stopped workflow: {}, locale: {}, because the job was cancelled",
                doc.workflowId(), doc.locale());
          } else {
            failed.incrementAndGet();
            log.warn("Metis skipped workflow: {}, locale: {}, because of: {}",
                doc.workflowId(), doc.locale(), describe(error));
          }
          final var completed = done.incrementAndGet();
          jobService.updateProgress(jobId, completed);
          log.debug("Metis indexed {}/{}: {}, locale: {}",
              completed, total, doc.title(), doc.locale());
          return null;
        });
  }

  private void compactFinished(List<CompletableFuture<Void>> inFlight) {
    final Iterator<CompletableFuture<Void>> iterator = inFlight.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().isDone()) {
        iterator.remove();
      }
    }
  }

  /** The VECTOR(n) width is created when search is enabled; this property only checks that it still matches. */
  private void assertEmbeddingColumnWidth() {
    final var columnType = MetisSearchSql.await(index().embeddingColumnType());
    final var expected = "vector(" + config.getEmbeddingDimension() + ")";
    if (columnType == null || !columnType.equalsIgnoreCase(expected)) {
      throw new IllegalStateException(
          "eveli.metis.search.indexing.embedding-dimension is " + config.getEmbeddingDimension()
          + " but metis_search_index.embedding is " + columnType
          + ". The column width is created when search is enabled, not by this property.");
    }
  }

  /**
   * The column check does not cover what the model returns. Without this a wrong dimension
   * setting fails every document separately at upsert time.
   */
  private void assertEmbeddingModelWidth() {
    final var width = embeddingService.embed(WIDTH_PROBE).length;
    if (width != config.getEmbeddingDimension()) {
      throw new IllegalStateException(
          "The embedding model returned " + width + " dimensions but eveli.metis.search.indexing.embedding-dimension is "
          + config.getEmbeddingDimension() + ". Configure the model to return " + config.getEmbeddingDimension()
          + " dimensions, for example eveli.metis.google-genai.embedding.dimensions.");
    }
  }

  private void indexDocument(long jobId, SiteSearchDocument doc) {
    if (!jobService.isRunning(jobId)) {
      throw new CancellationException("reindex job is no longer running");
    }
    final var metadata = metadataGenerator.generate(doc);
    final var embedding = embeddingService.embed(doc.searchText() + "\n" + metadata);
    if (!jobService.isRunning(jobId)) {
      throw new CancellationException("reindex job is no longer running");
    }
    upsertDocument(doc, metadata, embedding);
  }

  private void upsertDocument(SiteSearchDocument doc, String aiMetadata, float[] embedding) {
    final var tsConfig = TsConfig.forLocale(doc.locale());
    MetisSearchSql.await(index().upsert(
        doc.workflowId(),
        doc.topicId(),
        doc.locale(),
        doc.title(),
        doc.category(),
        doc.searchText(),
        aiMetadata,
        tsConfig,
        doc.title(),
        doc.description(),
        aiMetadata,
        doc.supplementalText(),
        MetisSearchSql.toVectorLiteral(embedding),
        doc.contentHash()));
  }

  private Map<String, String> loadExistingHashes() {
    final Map<String, String> hashes = new HashMap<>();
    for (final var row : MetisSearchSql.await(index().findContentHashes())) {
      hashes.put(key(row.workflowId(), row.locale()), row.contentHash());
    }
    return hashes;
  }

  private void deleteStaleRows(List<SiteSearchDocument> documents) {
    if (documents.isEmpty()) {
      final var removed = MetisSearchSql.await(index().deleteAll(true));
      log.warn("Metis cleared the whole index, {} row(s), the deployed site has no workflows", removed.size());
      return;
    }
    final var keep = new MetisSearchKeepKeys(documents.stream()
        .map(doc -> new MetisSearchKeepKeys.Key(doc.workflowId(), doc.locale()))
        .toList());
    final var removed = MetisSearchSql.await(index().deleteStale(keep));
    if (!removed.isEmpty()) {
      log.info("Metis removed {} stale index row(s)", removed.size());
    }
  }

  private String key(String workflowId, String locale) {
    return workflowId + "|" + locale;
  }

  public java.util.Optional<String> currentBundleHash() {
    final var hash = contentReader.currentBundleHash();
    return hash == null ? java.util.Optional.empty() : hash;
  }

  public long countIndexedDocuments() {
    return MetisSearchSql.await(index().countDocuments());
  }

  private io.resys.metis.search.spi.store.MetisSearchDbQuery.MetisSearchIndexQuery index() {
    return db.query().queryMetisSearchIndex();
  }

  private static boolean isCancellation(Throwable error) {
    for (var cause = error; cause != null; cause = cause.getCause()) {
      if (cause instanceof CancellationException) {
        return true;
      }
    }
    return false;
  }

  private static String describe(Throwable error) {
    var cause = error;
    while (cause.getCause() != null && cause.getCause() != cause) {
      cause = cause.getCause();
    }
    return cause.toString();
  }

  private record ProcessOutcome(int indexed, int failed, boolean cancelled) {}
}
