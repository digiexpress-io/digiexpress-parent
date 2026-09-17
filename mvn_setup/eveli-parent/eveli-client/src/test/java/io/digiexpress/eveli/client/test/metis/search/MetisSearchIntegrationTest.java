package io.digiexpress.eveli.client.test.metis.search;

/*-
 * #%L
 * eveli-client
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.digiexpress.eveli.client.spi.assets.ContentDeployedEvent;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.digiexpress.eveli.client.spi.metis.search.MetisLiveIndexTrigger;
import io.digiexpress.eveli.client.spi.metis.search.MetisReindexListener;
import io.digiexpress.eveli.client.web.resources.gamut.GamutSiteSearchController;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Deployment.BundleStatus;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.program.ImmutableTopic;
import io.resys.limaone.program.ImmutableTopicBlob;
import io.resys.limaone.program.ImmutableTopicLink;
import io.resys.metis.api.ImmutableMetisConfig;
import io.resys.metis.api.MetisConfig;
import io.resys.metis.search.api.ImmutableMetisSearchConfig;
import io.resys.metis.search.api.ImmutableMetisSearchIndexingConfig;
import io.resys.metis.search.api.ImmutableMetisSearchQueryConfig;
import io.resys.metis.search.api.MetisSearchClient.SearchMode;
import io.resys.metis.search.api.MetisSearchConfig;
import io.resys.metis.search.api.MetisSearchIndexStatus.JobState;
import io.resys.metis.search.api.MetisSearchIndexingConfig;
import io.resys.metis.search.api.MetisSearchQueryConfig;
import io.resys.metis.search.spi.MetisSearchClientImpl;
import io.resys.metis.search.spi.index.GeneratedMetadata;
import io.resys.metis.search.spi.index.IndexingService;
import io.resys.metis.search.spi.index.MetadataGenerator;
import io.resys.metis.search.spi.index.ReindexJobService;
import io.resys.metis.search.spi.index.SiteContentReader;
import io.resys.metis.search.spi.index.SiteSearchDocumentBuilder;
import io.resys.metis.search.spi.query.FtsSearchService;
import io.resys.metis.search.spi.query.HybridSearchService;
import io.resys.metis.search.spi.query.VectorSearchService;
import io.resys.metis.search.spi.store.MetisSearchDb;
import io.resys.metis.search.spi.store.MetisSearchSql;
import io.resys.metis.spi.ai.EmbeddingService;
import io.resys.metis.spi.ai.StructuredChatService;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Exercises the whole index and search path against a real PostgreSQL with pgvector,
 * using the production Flyway migration and stubbed models.
 */
public class MetisSearchIntegrationTest {

  /**
   * The relevance cutoffs are switched off for the ranking tests. {@link StubEmbeddingModel}
   * is a hashed bag of words, so its similarities are an order of magnitude smaller than a
   * real model's and cannot be compared against production thresholds. The cutoff itself is
   * covered by {@link #weakMatchesAreDroppedRatherThanPaddingTheResults()}.
   */
  private static final MetisConfig PLATFORM = ImmutableMetisConfig.builder().build();
  private static final MetisSearchQueryConfig QUERY = ImmutableMetisSearchQueryConfig.builder()
      .minVectorScore(0)
      .scoreDropOffRatio(0)
      .build();
  private static final MetisSearchIndexingConfig INDEXING =
      ImmutableMetisSearchIndexingConfig.builder().build();
  private static final MetisSearchConfig CONFIG = ImmutableMetisSearchConfig.builder()
      .query(QUERY)
      .indexing(INDEXING)
      .build();
  private static final Executor DIRECT = Runnable::run;

  private static PostgreSQLContainer<?> postgres;
  private static PgPool pgPool;
  private static MetisSearchDb db;

  private ReindexJobService jobService;
  private VectorSearchService vectorSearch;
  private FtsSearchService ftsSearch;
  private HybridSearchService hybridSearch;

  @BeforeAll
  static void startDatabase() {
    postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres"));
    postgres.start();

    Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .locations("classpath:db/postgresql")
        .load()
        .migrate();

    pgPool = PgPool.pool(
        new PgConnectOptions()
            .setHost(postgres.getHost())
            .setPort(postgres.getFirstMappedPort())
            .setDatabase(postgres.getDatabaseName())
            .setUser(postgres.getUsername())
            .setPassword(postgres.getPassword()),
        new PoolOptions().setMaxSize(5));
    waitUntilPostgresqlAcceptsConnections(pgPool);
    MetisSearchSql.ensureSearchExtensions(pgPool, 1024);
    db = MetisSearchSql.create(pgPool);
  }

  private static void waitUntilPostgresqlAcceptsConnections(io.vertx.mutiny.sqlclient.Pool pool) {
    final var connection = pool.getConnection()
        .onFailure()
        .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
        .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }

  @AfterAll
  static void stopDatabase() {
    if (pgPool != null) {
      pgPool.close();
    }
    if (postgres != null) {
      postgres.stop();
    }
  }

  @BeforeEach
  void resetIndex() {
    MetisSearchSql.truncate(db);

    final var embeddingService = new EmbeddingService(new StubEmbeddingModel());
    jobService = new ReindexJobService(db);
    vectorSearch = new VectorSearchService(db, embeddingService, QUERY);
    ftsSearch = new FtsSearchService(db, QUERY);
    hybridSearch = new HybridSearchService(vectorSearch, ftsSearch, QUERY);
  }

  @Test
  void indexesTheSiteAndFindsServicesByKeywordAndByMeaning() {
    final var indexed = reindex(finnishSite(true), false);
    Assertions.assertEquals(3, indexed);
    Assertions.assertEquals(3L, countRows());

    final var byKeyword = ftsSearch.rankedResults("palautetta", "fi", 5);
    Assertions.assertFalse(byKeyword.isEmpty());
    Assertions.assertEquals("wf-feedback", byKeyword.get(0).getWorkflowId());

    // The stub embeds shared words, "kirjaston kirja" is closest to the library service.
    final var byMeaning = vectorSearch.rankedResults("kirjaston kirja", "fi", 5);
    Assertions.assertEquals("wf-library", byMeaning.get(0).getWorkflowId());

    final var hybrid = hybridSearch.rankedResults("palautetta", "fi", 5);
    Assertions.assertEquals("wf-feedback", hybrid.get(0).getWorkflowId());
    Assertions.assertTrue(hybrid.get(0).getScore() > 0);
  }

  @Test
  void hybridFusesBothListsAndKeepsEveryCandidate() {
    reindex(finnishSite(true), false);

    final var vector = vectorSearch.rankedResults("kirjasto", "fi", 10);
    final var fts = ftsSearch.rankedResults("kirjasto", "fi", 10);
    final var hybrid = hybridSearch.rankedResults("kirjasto", "fi", 10);

    Assertions.assertFalse(hybrid.isEmpty());
    Assertions.assertTrue(hybrid.size() <= vector.size() + fts.size());
    for (int i = 1; i < hybrid.size(); i++) {
      Assertions.assertTrue(hybrid.get(i - 1).getScore() >= hybrid.get(i).getScore(),
          "hybrid results must be ordered by fused score");
    }
    final var top = hybrid.get(0);
    Assertions.assertNotNull(top.getVectorScore());
  }

  @Test
  void hybridStillReturnsKeywordHitsWhenQueryEmbeddingFails() {
    reindex(finnishSite(true), false);

    final var broken = new VectorSearchService(
        db, new EmbeddingService(new FailingEmbeddingModel()), QUERY);
    final var hybrid = new HybridSearchService(broken, ftsSearch, QUERY);
    final var results = hybrid.rankedResults("kirjasto", "fi", 5);

    Assertions.assertFalse(results.isEmpty(), "keyword hits must still be returned");
    Assertions.assertEquals("wf-library", results.get(0).getWorkflowId());
  }

  @Test
  void hybridStillReturnsKeywordHitsWhenQueryEmbeddingTimesOut() {
    reindex(finnishSite(true), false);

    // Query embedding gets the search budget less two seconds, so a three second budget
    // abandons a slower model after one.
    final var budget = ImmutableMetisSearchQueryConfig.builder().from(QUERY).timeoutSeconds(3).build();
    final var slow = new EmbeddingService(new SlowEmbeddingModel(6_000));
    final var hybrid = new HybridSearchService(
        new VectorSearchService(db, slow, budget), ftsSearch, budget);
    final var started = System.nanoTime();
    final var results = hybrid.rankedResults("kirjasto", "fi", 5);
    final var elapsedMs = Duration.ofNanos(System.nanoTime() - started).toMillis();

    Assertions.assertTrue(elapsedMs < 4_000, "must not wait for the full embed, elapsed=" + elapsedMs);
    Assertions.assertFalse(results.isEmpty(), "keyword hits must still be returned");
    Assertions.assertEquals("wf-library", results.get(0).getWorkflowId());
  }

  /**
   * A nearest neighbour search returns as many rows as it is asked for regardless of distance,
   * so without a cutoff every query fills the whole result list with unrelated services.
   */
  @Test
  void weakMatchesAreDroppedRatherThanPaddingTheResults() {
    reindex(finnishSite(true), false);

    final var unfiltered = vectorSearch.rankedResults("kirjaston kirja", "fi", 10);
    Assertions.assertEquals(3, unfiltered.size(), "without a cutoff every document is returned");

    final var best = unfiltered.get(0).getVectorScore();
    final var runnerUp = unfiltered.get(1).getVectorScore();
    Assertions.assertTrue(best > runnerUp, "the stub must rank the library service highest");

    final var ratio = ImmutableMetisSearchQueryConfig.builder()
        .from(QUERY)
        .scoreDropOffRatio((runnerUp / best) + 0.01)
        .build();
    final var filtered = new VectorSearchService(db,
        new EmbeddingService(new StubEmbeddingModel()), ratio)
        .rankedResults("kirjaston kirja", "fi", 10);
    Assertions.assertEquals(1, filtered.size(), "the weaker tail must be dropped");
    Assertions.assertEquals("wf-library", filtered.get(0).getWorkflowId());

    final var floored = new VectorSearchService(db,
        new EmbeddingService(new StubEmbeddingModel()),
        ImmutableMetisSearchQueryConfig.builder().from(QUERY).minVectorScore(best + 0.01).build())
        .rankedResults("kirjaston kirja", "fi", 10);
    Assertions.assertEquals(List.of(), floored);
  }

  @Test
  void resultsAreCappedAtTheConfiguredMaximum() {
    final var client = new MetisSearchClientImpl(
        searchConfig(ImmutableMetisSearchQueryConfig.builder().from(QUERY).maxResults(2).build()),
        hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, DIRECT, DIRECT, "stub");
    reindex(finnishSite(true), false);

    final var results = client.query().locale("fi").limit(10)
        .findByText("kirjasto").await().indefinitely();
    Assertions.assertTrue(results.size() <= 2, "a caller cannot ask for more than the cap");
  }

  @Test
  void unchangedDocumentsAreSkippedAndRemovedOnesAreDeleted() {
    reindex(finnishSite(true), false);

    final var second = reindex(finnishSite(true), false);
    Assertions.assertEquals(0, second, "unchanged documents must not be processed again");
    Assertions.assertEquals(3L, countRows());

    reindex(finnishSite(false), false);
    Assertions.assertEquals(2L, countRows());
    Assertions.assertTrue(ftsSearch.rankedResults("palautetta", "fi", 5).stream()
        .noneMatch(result -> "wf-feedback".equals(result.getWorkflowId())));
  }

  @Test
  void aFailedContentReadLeavesTheIndexAlone() {
    reindex(finnishSite(true), false);
    Assertions.assertEquals(3L, countRows());

    final var contentReader = Mockito.mock(SiteContentReader.class);
    Mockito.when(contentReader.readDocuments())
        .thenThrow(new SiteContentReader.ContentUnavailableException("no content bundle is deployed"));

    final var jobId = jobService.tryStart("stub").orElseThrow();
    indexingService(contentReader).runReindex(jobId, false);

    final var status = jobService.latest().orElseThrow();
    Assertions.assertEquals("FAILED", status.getState().name());
    Assertions.assertEquals(3L, countRows(), "a failed read must not remove anything");
    Assertions.assertFalse(ftsSearch.rankedResults("palautetta", "fi", 5).isEmpty(),
        "search must keep answering from the previous index");
  }

  @Test
  void aSiteWithoutWorkflowsClearsTheIndex() {
    reindex(finnishSite(true), false);
    Assertions.assertEquals(3L, countRows());

    reindex(ImmutableLocalizedSite.builder().id("site-1").images("images").locale("fi").build(), false);
    Assertions.assertEquals(0L, countRows());
  }

  @Test
  void onlyOneReindexJobRunsAtATime() {
    final var first = jobService.tryStart("stub");
    Assertions.assertTrue(first.isPresent());

    Assertions.assertTrue(jobService.tryStart("stub").isEmpty(),
        "a second claim must be rejected while a job is running");

    jobService.markCompleted(first.getAsLong(), 0, 0, 1);
    Assertions.assertTrue(jobService.tryStart("stub").isPresent(),
        "a new job may start once the previous one finished");
  }

  @Test
  void aPublicationIdIsRecordedAndCountsAsIndexedUntilTheJobFails() {
    Assertions.assertFalse(jobService.isPublicationIndexed("pub-a"));
    Assertions.assertFalse(jobService.isPublicationIndexed(null));

    final var running = jobService.tryStart("stub", "pub-a").orElseThrow();
    Assertions.assertTrue(jobService.isPublicationIndexed("pub-a"));
    Assertions.assertEquals("pub-a", jobService.latest().orElseThrow().getPublicationId());

    jobService.markCompleted(running, 0, 0, 1);
    Assertions.assertTrue(jobService.isPublicationIndexed("pub-a"),
        "a completed job still counts so the reconciler does not start another");
    jobService.setBundleHash(running, "hash-aaa");
    Assertions.assertTrue(jobService.isPublicationIndexed("pub-a", "hash-aaa"));
    Assertions.assertFalse(jobService.isPublicationIndexed("pub-a", "hash-bbb"),
        "a completed job against a stale Runtime world must be retried");

    jobService.markFailed(jobService.tryStart("stub", "pub-b").orElseThrow(), "boom");
    Assertions.assertFalse(jobService.isPublicationIndexed("pub-b"),
        "a failed go-live must be retried");

    final var cancelled = jobService.tryStart("stub", "pub-c").orElseThrow();
    jobService.markCancelled(cancelled, 0, 0, 1);
    Assertions.assertFalse(jobService.isPublicationIndexed("pub-c"),
        "a cancelled job must be retried when the publication is still live");
  }

  @Test
  void aLivePublicationStartsAReindexJob() throws InterruptedException {
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, DIRECT, DIRECT, "stub");
    final var listener = new MetisReindexListener(
        client, reindexOnDeploymentProps(), liveTrigger(client, "pub-1"));

    listener.onContentDeployed(new ContentDeployedEvent("assets-publication"));

    awaitPublication("pub-1", JobState.COMPLETED, 10_000);
    final var first = jobService.latest().orElseThrow();
    Assertions.assertEquals("pub-1", first.getPublicationId());
    Assertions.assertEquals(JobState.COMPLETED, first.getState(), first.getError());

    listener.onContentDeployed(new ContentDeployedEvent("assets-publication"));
    Assertions.assertEquals(first.getJobId(), jobService.latest().orElseThrow().getJobId(),
        "a second event for the same live publication must not start another job");

    new MetisReindexListener(client, reindexOnDeploymentProps(), liveTrigger(client, "pub-2"))
        .onContentDeployed(new ContentDeployedEvent("assets-publication"));
    awaitPublication("pub-2", JobState.COMPLETED, 10_000);
    Assertions.assertEquals("pub-2", jobService.latest().orElseThrow().getPublicationId());
  }

  @Test
  void aStaleBundleHashDoesNotCountAsThePublicationBeingIndexed() throws InterruptedException {
    final var reader = Mockito.mock(SiteContentReader.class);
    Mockito.when(reader.readDocuments()).thenReturn(
        new SiteSearchDocumentBuilder(PLATFORM, INDEXING).buildDocuments(finnishSite(true)));
    Mockito.when(reader.currentBundleHash()).thenReturn(java.util.Optional.of("hash-bbb"));
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(reader), jobService, DIRECT, DIRECT, "stub");

    final var previous = jobService.tryStart("stub", "pub-1").orElseThrow();
    jobService.setBundleHash(previous, "hash-aaa");
    jobService.markCompleted(previous, 0, 0, 1);
    Assertions.assertFalse(client.index().isPublicationIndexed("pub-1"));

    liveTrigger(client, "pub-1").startIfLivePublicationChanged();
    awaitNewerJob(previous, "pub-1", JobState.COMPLETED, 10_000);
    final var latest = jobService.latest().orElseThrow();
    Assertions.assertTrue(latest.getJobId() > previous);
    Assertions.assertEquals("pub-1", latest.getPublicationId());
    Assertions.assertEquals("hash-bbb", latest.getBundleHash());
  }

  @Test
  void callerSuppliedLimitAndQueryLengthAreClamped() {
    reindex(finnishSite(true), false);
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, DIRECT, DIRECT, "stub");

    final var negative = client.query().locale("fi").limit(-1)
        .findByText("kirjasto").await().indefinitely();
    Assertions.assertFalse(negative.isEmpty(), "a negative limit must be clamped, not passed on");

    final var tooLong = "kirjasto ".repeat(200);
    Assertions.assertTrue(tooLong.length() > QUERY.getMaxQueryChars());
    Assertions.assertNotNull(client.query().locale("fi")
        .findByText(tooLong).await().indefinitely());
  }

  @Test
  void aJobIsOnlyReclaimedOnceItStopsReportingProgress() {
    final var jobId = jobService.tryStart("stub").orElseThrow();
    jobService.updateProgress(jobId, 1);

    final var impatient = new ReindexJobService(db, Duration.ofMinutes(30));
    Assertions.assertTrue(impatient.tryStart("stub").isEmpty(),
        "a job that just reported progress is alive");

    final var reclaiming = new ReindexJobService(db, Duration.ZERO);
    Assertions.assertTrue(reclaiming.tryStart("stub").isPresent(),
        "a job that went quiet must be reclaimable");
    Assertions.assertEquals(JobState.FAILED, jobService.findById(jobId).orElseThrow().getState());
  }

  @Test
  void shutdownReleasesTheClaimOfAJobThisInstanceStarted() {
    final var neverRuns = new java.util.ArrayList<Runnable>();
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, neverRuns::add, DIRECT, "stub");

    final var started = client.index().startReindex(false).await().indefinitely();
    Assertions.assertTrue(started.getAccepted());
    Assertions.assertTrue(jobService.tryStart("stub").isEmpty(), "the claim is held");

    client.shutdown();

    Assertions.assertEquals(JobState.FAILED, jobService.findById(started.getJobId()).orElseThrow().getState());
    Assertions.assertTrue(jobService.tryStart("stub").isPresent(),
        "the next instance must be able to start a job");
    Assertions.assertEquals(1, neverRuns.size());
  }

  @Test
  void searchIsRejectedForLocalesThatAreNotIndexed() {
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, DIRECT, DIRECT, "stub");

    Assertions.assertThrows(IllegalArgumentException.class,
        () -> client.query().locale("de").findByText("hallo").await().indefinitely());
    Assertions.assertNotNull(
        client.query().locale("FI").findByText("kirjasto").await().indefinitely(),
        "locale comparison is case-insensitive");
    Assertions.assertEquals(List.of(),
        client.query().locale("fi").findByText("  ").await().indefinitely());
    Assertions.assertNotNull(
        client.query().locale("fi").mode(SearchMode.HYBRID).limit(3).findByText("kirjasto")
            .await().indefinitely());
  }

  @Test
  void queuedDocumentsDoNotExpireBeforeTheyStart() {
    final var pool = Executors.newFixedThreadPool(1);
    try {
      final var indexing = ImmutableMetisSearchIndexingConfig.builder().from(INDEXING)
          .concurrency(1)
          .documentTimeoutSeconds(1)
          .build();
      final var jobId = jobService.tryStart("stub").orElseThrow();
      indexingService(manyServices(8), indexing, pool, slowMetadataGenerator(300))
          .runReindex(jobId, false);

      final var status = jobService.latest().orElseThrow();
      Assertions.assertEquals("COMPLETED", status.getState().name(), status.getError());
      Assertions.assertEquals(8, status.getProcessedCount());
      Assertions.assertEquals(8L, countRows());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void aPartialReindexDoesNotDeleteThePreviousIndex() {
    reindex(finnishSite(true), false);
    Assertions.assertEquals(3L, countRows());

    final var embeddingService = Mockito.mock(EmbeddingService.class);
    Mockito.when(embeddingService.embed(Mockito.anyString()))
        .thenThrow(new RuntimeException("embed down"));
    final var jobId = jobService.tryStart("stub").orElseThrow();
    new IndexingService(
        contentReader(finnishSite(true)),
        metadataGenerator(),
        embeddingService,
        jobService, db, DIRECT, INDEXING)
        .runReindex(jobId, true);

    final var status = jobService.latest().orElseThrow();
    Assertions.assertEquals("FAILED", status.getState().name());
    Assertions.assertTrue(status.getError().contains("Incomplete reindex"));
    Assertions.assertEquals(3L, countRows(), "a partial failure must not remove the previous index");
  }

  @Test
  void aCancellingJobStillHoldsTheSingleFlightGuard() {
    final var first = jobService.tryStart("stub").orElseThrow();
    Assertions.assertTrue(jobService.requestCancel(false, false));
    Assertions.assertTrue(jobService.tryStart("stub").isEmpty(),
        "CANCELLING still occupies the single flight guard");

    jobService.markCancelled(first, 0, 0, 1);
    Assertions.assertTrue(jobService.tryStart("stub").isPresent());
  }

  @Test
  void portalSearchFallsBackWhenNoReindexHasRun() {
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, DIRECT, DIRECT, "stub");
    Assertions.assertFalse(client.index().isIndexReadyForPortal());
    Assertions.assertTrue(jobService.latest().isEmpty());

    final var props = new EveliPropsMetisSearch();
    props.getQuery().setRateLimitRequests(0);
    final var response = new GamutSiteSearchController(client, props)
        .findByText("kirjasto", "fi", 8, Mockito.mock(HttpServletRequest.class))
        .await().indefinitely();
    Assertions.assertTrue(response.getFallback(), "an empty job table must keep keyword search");
    Assertions.assertTrue(response.getResults().isEmpty());
  }

  @Test
  void portalSearchFallsBackUntilAReindexHasCompleted() {
    reindex(finnishSite(true), false);
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, DIRECT, DIRECT, "stub");
    final var props = new EveliPropsMetisSearch();
    props.getQuery().setRateLimitRequests(0);
    final var controller = new GamutSiteSearchController(client, props);
    final var request = Mockito.mock(HttpServletRequest.class);

    final var ready = controller.findByText("kirjasto", "fi", 8, request).await().indefinitely();
    Assertions.assertFalse(ready.getFallback());
    Assertions.assertFalse(ready.getResults().isEmpty());

    final var inflightJob = jobService.tryStart("stub").orElseThrow();
    Assertions.assertTrue(client.index().isReindexInFlight());
    Assertions.assertFalse(client.index().isIndexReadyForPortal());
    final var inflight = controller.findByText("kirjasto", "fi", 8, request).await().indefinitely();
    Assertions.assertTrue(inflight.getFallback(), "portal visitors must keep keyword search during a rebuild");
    Assertions.assertTrue(inflight.getResults().isEmpty());

    Assertions.assertTrue(jobService.requestCancel(false, false));
    Assertions.assertTrue(client.index().isReindexInFlight(), "CANCELLING is still in flight");
    Assertions.assertTrue(controller.findByText("kirjasto", "fi", 8, request)
        .await().indefinitely().getFallback());

    jobService.markCancelled(inflightJob, 0, 0, 1);
    Assertions.assertFalse(client.index().isReindexInFlight());
    Assertions.assertFalse(client.index().isIndexReadyForPortal());
    final var afterCancel = controller.findByText("kirjasto", "fi", 8, request).await().indefinitely();
    Assertions.assertTrue(afterCancel.getFallback(),
        "a cancelled job leaves a mixed table, portal keeps keyword search");
    Assertions.assertTrue(afterCancel.getResults().isEmpty());

    final var liveHits = client.query().locale("fi").findByText("kirjasto").await().indefinitely();
    Assertions.assertFalse(liveHits.isEmpty(), "the live index is still queryable while the portal falls back");

    jobService.markCompleted(jobService.tryStart("stub").orElseThrow(), 0, 0, 1);
    final var afterComplete = controller.findByText("kirjasto", "fi", 8, request).await().indefinitely();
    Assertions.assertFalse(afterComplete.getFallback());
    Assertions.assertFalse(afterComplete.getResults().isEmpty());
  }

  @Test
  void portalSearchFallsBackAfterAFailedReindexUntilTheNextCompletedJob() {
    reindex(finnishSite(true), false);
    final var client = new MetisSearchClientImpl(
        CONFIG, hybridSearch, vectorSearch, ftsSearch,
        indexingService(finnishSite(true)), jobService, DIRECT, DIRECT, "stub");
    final var props = new EveliPropsMetisSearch();
    props.getQuery().setRateLimitRequests(0);
    final var controller = new GamutSiteSearchController(client, props);
    final var request = Mockito.mock(HttpServletRequest.class);

    jobService.markFailed(jobService.tryStart("stub").orElseThrow(), "embed down");
    Assertions.assertTrue(controller.findByText("kirjasto", "fi", 8, request)
        .await().indefinitely().getFallback());

    jobService.markCompleted(jobService.tryStart("stub").orElseThrow(), 0, 0, 1);
    Assertions.assertFalse(controller.findByText("kirjasto", "fi", 8, request)
        .await().indefinitely().getFallback());
  }

  @Test
  void cancellingAJobReleasesTheClaimAndLeavesTheIndex() throws Exception {
    reindex(finnishSite(true), false);
    Assertions.assertEquals(3L, countRows());

    final var reindexPool = Executors.newSingleThreadExecutor();
    final var indexingPool = Executors.newSingleThreadExecutor();
    final var started = new CountDownLatch(1);
    try {
      final var client = new MetisSearchClientImpl(
          CONFIG, hybridSearch, vectorSearch, ftsSearch,
          indexingService(finnishSite(true), INDEXING, indexingPool,
              blockingUntilCancelledOnceGenerator(started)),
          jobService, reindexPool, DIRECT, "stub");

      Assertions.assertTrue(client.index().startReindex(true).await().indefinitely().getAccepted());
      Assertions.assertTrue(started.await(5, TimeUnit.SECONDS));

      final var cancelled = client.index().cancelReindex().await().indefinitely();
      Assertions.assertTrue(cancelled.getAccepted());
      awaitState(JobState.CANCELLED, 10_000);
      Assertions.assertEquals(3L, countRows(), "cancel must not wipe the index");
      Assertions.assertTrue(jobService.tryStart("stub").isPresent(),
          "a new job may start once the previous one was cancelled");
    } finally {
      reindexPool.shutdownNow();
      indexingPool.shutdownNow();
    }
  }

  @Test
  void replaceCancelsTheRunningJobAndStartsAnother() throws Exception {
    final var reindexPool = Executors.newSingleThreadExecutor();
    final var indexingPool = Executors.newSingleThreadExecutor();
    final var started = new CountDownLatch(1);
    try {
      final var client = new MetisSearchClientImpl(
          CONFIG, hybridSearch, vectorSearch, ftsSearch,
          indexingService(finnishSite(true), INDEXING, indexingPool,
              blockingUntilCancelledOnceGenerator(started)),
          jobService, reindexPool, DIRECT, "stub");

      final var first = client.index().startReindex(false).await().indefinitely();
      Assertions.assertTrue(started.await(5, TimeUnit.SECONDS));

      final var replace = client.index().startReindex(false, true).await().indefinitely();
      Assertions.assertTrue(replace.getAccepted());
      Assertions.assertEquals(JobState.CANCELLING, replace.getState());

      awaitState(JobState.COMPLETED, 20_000);
      final var latest = jobService.latest().orElseThrow();
      Assertions.assertTrue(latest.getJobId().longValue() > first.getJobId().longValue(),
          "the replacement job must be a new row");
      Assertions.assertEquals(3L, countRows());
    } finally {
      reindexPool.shutdownNow();
      indexingPool.shutdownNow();
    }
  }

  @Test
  void primaryFtsHitsJoinHybridEvenWhenVectorAlreadyHasNeighbours() {
    reindex(finnishSite(true), false);

    final var embedder = new EmbeddingService(new StubEmbeddingModel());
    final var queryVector = embedder.embedQuery("palautetta");
    final var far = embedder.embed("zzzzunrelatedtokensxyz");
    MetisSearchSql.await(db.query().queryMetisSearchIndex().setEmbedding("wf-library", "fi",
        MetisSearchSql.toVectorLiteral(queryVector)));
    MetisSearchSql.await(db.query().queryMetisSearchIndex().setEmbedding("wf-feedback", "fi",
        MetisSearchSql.toVectorLiteral(far)));
    MetisSearchSql.await(db.query().queryMetisSearchIndex().setEmbedding("wf-parking", "fi",
        MetisSearchSql.toVectorLiteral(far)));

    // The production cutoffs, this test is about the fusion rather than the stub's scale.
    final var cutoffs = ImmutableMetisSearchQueryConfig.builder().build();
    final var vector = new VectorSearchService(db, embedder, cutoffs);
    final var fts = new FtsSearchService(db, cutoffs);
    final var hybrid = new HybridSearchService(vector, fts, cutoffs);

    final var vectorHits = vector.rankedResults("palautetta", "fi", 10);
    Assertions.assertTrue(vectorHits.stream().anyMatch(r -> "wf-library".equals(r.getWorkflowId())));
    Assertions.assertTrue(vectorHits.stream().noneMatch(r -> "wf-feedback".equals(r.getWorkflowId())),
        "feedback must sit outside the vector candidate set");

    Assertions.assertTrue(fts.primaryHits("palautetta", "fi", 10).stream()
        .anyMatch(r -> "wf-feedback".equals(r.getWorkflowId())));

    Assertions.assertTrue(hybrid.rankedResults("palautetta", "fi", 10).stream()
        .anyMatch(r -> "wf-feedback".equals(r.getWorkflowId())),
        "primary FTS must introduce the exact title hit");
  }

  @Test
  void weakVectorMatchesAreDroppedWhenKeywordSearchIsEmpty() {
    reindex(finnishSite(true), false);

    final var nonsense = "xyznotawordqqq";
    Assertions.assertTrue(ftsSearch.primaryHits(nonsense, "fi", 10).isEmpty());

    final var unfiltered = vectorSearch.rankedResults(nonsense, "fi", 10);
    Assertions.assertFalse(unfiltered.isEmpty(), "the stub still returns a nearest neighbour");
    final var best = unfiltered.get(0).getVectorScore();

    final var hybrid = new HybridSearchService(vectorSearch, ftsSearch,
        ImmutableMetisSearchQueryConfig.builder().from(QUERY)
            .minVectorScoreWithoutKeyword(best + 0.01)
            .build());
    Assertions.assertEquals(List.of(), hybrid.rankedResults(nonsense, "fi", 10),
        "gibberish must not fall through to the trigram fallback");
  }

  @Test
  void aMisspelledKeywordFallsThroughToTrigramSearch() {
    reindex(finnishSite(true), false);

    // Primary websearch stemming does not recover a dropped vowel; trigram word_similarity does.
    final var misspelled = "kirjsto";
    Assertions.assertTrue(ftsSearch.primaryHits(misspelled, "fi", 10).isEmpty(),
        "primary FTS must miss so the fallback path is the one under test");

    final var fallback = ftsSearch.rankedResults(misspelled, "fi", 10);
    Assertions.assertFalse(fallback.isEmpty());
    Assertions.assertEquals("wf-library", fallback.get(0).getWorkflowId());
  }

  private MetisLiveIndexTrigger liveTrigger(io.resys.metis.search.api.MetisSearchClient search, String publicationId) {
    return liveTrigger(search, publicationWorld(publicationId, OffsetDateTime.now().minusHours(1)));
  }

  @SuppressWarnings("unchecked")
  private static MetisLiveIndexTrigger liveTrigger(
      io.resys.metis.search.api.MetisSearchClient search, ImmutableModelWorld world) {
    final var authoring = Mockito.mock(Authoring.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(authoring.worldQuery().docs(BodyType.DEPLOYMENT).findAll())
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(world));
    final var envir = new EveliEditEnvir(null, null, authoring, null);
    final ObjectProvider<EveliEditEnvir> provider = Mockito.mock(ObjectProvider.class);
    Mockito.when(provider.getIfAvailable()).thenReturn(envir);
    return new MetisLiveIndexTrigger(search, reindexOnDeploymentProps(), provider, Runnable::run);
  }

  private static EveliPropsMetisSearch reindexOnDeploymentProps() {
    final var props = new EveliPropsMetisSearch();
    props.setReindexOnDeployment(true);
    return props;
  }

  private static ImmutableModelWorld publicationWorld(String id, OffsetDateTime startsAt) {
    final Model<Deployment> publication = ImmutableModel.<Deployment>builder()
        .id(id)
        .bodyHash(id)
        .bodyType(BodyType.DEPLOYMENT)
        .body(ImmutableDeployment.builder()
            .fromCommitId(UUID.randomUUID())
            .name(id)
            .createdBy("test")
            .createdAt(startsAt)
            .startsAt(startsAt)
            .description("")
            .status(BundleStatus.UNKNOWN)
            .build())
        .build();
    return ImmutableModelWorld.builder().name("test").putDeployments(id, publication).build();
  }

  private int reindex(LocalizedSite site, boolean force) {
    final var jobId = jobService.tryStart("stub").orElseThrow();
    indexingService(site).runReindex(jobId, force);

    final var status = jobService.latest().orElseThrow();
    Assertions.assertEquals("COMPLETED", status.getState().name(), status.getError());
    return status.getProcessedCount();
  }

  private static MetisSearchConfig searchConfig(MetisSearchQueryConfig query) {
    return ImmutableMetisSearchConfig.builder().from(CONFIG).query(query).build();
  }

  private IndexingService indexingService(LocalizedSite site) {
    return indexingService(site, INDEXING, DIRECT, metadataGenerator());
  }

  private IndexingService indexingService(SiteContentReader contentReader) {
    return new IndexingService(
        contentReader,
        metadataGenerator(),
        new EmbeddingService(new StubEmbeddingModel()),
        jobService,
        db,
        DIRECT,
        INDEXING);
  }

  private IndexingService indexingService(
      LocalizedSite site, MetisSearchIndexingConfig config, Executor executor, MetadataGenerator generator) {
    return new IndexingService(
        contentReader(site, config),
        generator,
        new EmbeddingService(new StubEmbeddingModel()),
        jobService,
        db,
        executor,
        config);
  }

  private SiteContentReader contentReader(LocalizedSite site) {
    return contentReader(site, INDEXING);
  }

  private SiteContentReader contentReader(LocalizedSite site, MetisSearchIndexingConfig config) {
    final var documents = new SiteSearchDocumentBuilder(PLATFORM, config).buildDocuments(site);
    final var contentReader = Mockito.mock(SiteContentReader.class);
    Mockito.when(contentReader.readDocuments()).thenReturn(documents);
    Mockito.when(contentReader.currentBundleHash()).thenReturn(java.util.Optional.empty());
    return contentReader;
  }

  private MetadataGenerator metadataGenerator() {
    final var chatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(chatClient.prompt().user(Mockito.anyString()).call().entity(GeneratedMetadata.class))
        .thenReturn(new GeneratedMetadata("Kuvaus", List.of("synonyymi"), List.of("liittyva lause")));
    return new MetadataGenerator(new StructuredChatService(chatClient));
  }

  private MetadataGenerator slowMetadataGenerator(long sleepMs) {
    final var chatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(chatClient.prompt().user(Mockito.anyString()).call().entity(GeneratedMetadata.class))
        .thenAnswer(invocation -> {
          Thread.sleep(sleepMs);
          return new GeneratedMetadata("Kuvaus", List.of("synonyymi"), List.of("liittyva lause"));
        });
    return new MetadataGenerator(new StructuredChatService(chatClient));
  }

  private MetadataGenerator blockingUntilCancelledOnceGenerator(CountDownLatch started) {
    final var first = new AtomicBoolean(true);
    final var chatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(chatClient.prompt().user(Mockito.anyString()).call().entity(GeneratedMetadata.class))
        .thenAnswer(invocation -> {
          if (first.compareAndSet(true, false)) {
            started.countDown();
            while (true) {
              final var latest = jobService.latest();
              if (latest.isEmpty() || latest.get().getState() != JobState.RUNNING) {
                throw new java.util.concurrent.CancellationException();
              }
              Thread.sleep(20);
            }
          }
          return new GeneratedMetadata("Kuvaus", List.of("synonyymi"), List.of("liittyva lause"));
        });
    return new MetadataGenerator(new StructuredChatService(chatClient));
  }

  private void awaitNewerJob(long previousJobId, String publicationId, JobState expected, long timeoutMs)
      throws InterruptedException {
    final var deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      final var status = jobService.latest();
      if (status.isPresent()
          && status.get().getJobId() != null
          && status.get().getJobId() > previousJobId
          && publicationId.equals(status.get().getPublicationId())
          && status.get().getState() == expected) {
        return;
      }
      Thread.sleep(50);
    }
    Assertions.fail("timed out waiting for a job newer than " + previousJobId + " for "
        + publicationId + " in state " + expected + ", last was " + jobService.latest().orElse(null));
  }

  private void awaitPublication(String publicationId, JobState expected, long timeoutMs)
      throws InterruptedException {
    final var deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      final var status = jobService.latest();
      if (status.isPresent()
          && publicationId.equals(status.get().getPublicationId())
          && status.get().getState() == expected) {
        return;
      }
      Thread.sleep(50);
    }
    Assertions.fail("timed out waiting for publication " + publicationId + " in state " + expected
        + ", last was " + jobService.latest().orElse(null));
  }

  private void awaitState(JobState expected, long timeoutMs) throws InterruptedException {
    final var deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      final var status = jobService.latest();
      if (status.isPresent() && status.get().getState() == expected) {
        return;
      }
      Thread.sleep(50);
    }
    Assertions.fail("timed out waiting for job state " + expected + ", last was "
        + jobService.latest().orElse(null));
  }

  private long countRows() {
    return MetisSearchSql.await(db.query().queryMetisSearchIndex().countDocuments());
  }

  /**
   * Three municipality services in Finnish, optionally without the feedback service so
   * that a document disappearing from the content can be asserted.
   */
  private LocalizedSite finnishSite(boolean withFeedback) {
    final var site = ImmutableLocalizedSite.builder().id("site-1").images("images").locale("fi");

    site.putLinks("wf-library", ImmutableTopicLink.builder()
        .id("wf-library").type("workflow").name("Varaa kirja kirjastosta").value("/library")
        .global(false).workflow(true).build());
    site.putLinks("wf-parking", ImmutableTopicLink.builder()
        .id("wf-parking").type("workflow").name("Hae pysakointilupaa").value("/parking")
        .global(false).workflow(true).build());
    site.putBlobs("blob-library", ImmutableTopicBlob.builder()
        .id("blob-library").value("Kirjaston kirja varataan verkossa.").build());
    site.putBlobs("blob-parking", ImmutableTopicBlob.builder()
        .id("blob-parking").value("Pysakointilupa haetaan asuinalueen mukaan.").build());
    site.putTopics("topic-library", ImmutableTopic.builder()
        .id("topic-library").name("Kirjasto").blob("blob-library").addLinks("wf-library").build());
    site.putTopics("topic-parking", ImmutableTopic.builder()
        .id("topic-parking").name("Liikenne").blob("blob-parking").addLinks("wf-parking").build());

    if (withFeedback) {
      site.putLinks("wf-feedback", ImmutableTopicLink.builder()
          .id("wf-feedback").type("workflow").name("Anna palautetta").value("/feedback")
          .global(false).workflow(true).build());
      site.putBlobs("blob-feedback", ImmutableTopicBlob.builder()
          .id("blob-feedback").value("Voit antaa palautetta kaupungin palveluista.").build());
      site.putTopics("topic-feedback", ImmutableTopic.builder()
          .id("topic-feedback").name("Palaute").blob("blob-feedback").addLinks("wf-feedback").build());
    }
    return site.build();
  }

  private LocalizedSite manyServices(int count) {
    final var site = ImmutableLocalizedSite.builder().id("site-many").images("images").locale("fi");
    for (int index = 0; index < count; index++) {
      final var id = "wf-" + index;
      site.putLinks(id, ImmutableTopicLink.builder()
          .id(id).type("workflow").name("Palvelu " + index).value("/svc-" + index)
          .global(false).workflow(true).build());
      site.putBlobs("blob-" + index, ImmutableTopicBlob.builder()
          .id("blob-" + index).value("Kuvaus palvelusta " + index).build());
      site.putTopics("topic-" + index, ImmutableTopic.builder()
          .id("topic-" + index).name("Aihe " + index).blob("blob-" + index).addLinks(id).build());
    }
    return site.build();
  }

  private static final class FailingEmbeddingModel extends StubEmbeddingModel {
    @Override
    public org.springframework.ai.embedding.EmbeddingResponse call(
        org.springframework.ai.embedding.EmbeddingRequest request) {
      throw new IllegalStateException("ollama is down");
    }
  }

  private static final class SlowEmbeddingModel extends StubEmbeddingModel {
    private final long sleepMs;

    private SlowEmbeddingModel(long sleepMs) {
      this.sleepMs = sleepMs;
    }

    @Override
    public org.springframework.ai.embedding.EmbeddingResponse call(
        org.springframework.ai.embedding.EmbeddingRequest request) {
      try {
        Thread.sleep(sleepMs);
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
      }
      return super.call(request);
    }
  }
}
