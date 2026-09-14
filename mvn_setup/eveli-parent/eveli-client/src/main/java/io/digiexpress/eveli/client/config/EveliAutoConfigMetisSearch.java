package io.digiexpress.eveli.client.config;

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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.spi.metis.search.MetisLiveIndexTrigger;
import io.digiexpress.eveli.client.spi.metis.search.MetisLivePublicationReconciler;
import io.digiexpress.eveli.client.spi.metis.search.MetisReindexListener;
import io.digiexpress.eveli.client.web.resources.gamut.GamutSiteSearchController;
import io.digiexpress.eveli.client.web.resources.worker.MetisSearchApiController;
import io.resys.metis.api.MetisConfig;
import io.resys.metis.search.api.ImmutableMetisSearchConfig;
import io.resys.metis.search.api.ImmutableMetisSearchIndexingConfig;
import io.resys.metis.search.api.ImmutableMetisSearchQueryConfig;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchConfig;
import io.resys.metis.search.spi.MetisSearchClientImpl;
import io.resys.metis.search.spi.index.IndexingService;
import io.resys.metis.search.spi.index.MetadataGenerator;
import io.resys.metis.search.spi.index.ReindexJobService;
import io.resys.metis.search.spi.index.SiteContentReader;
import io.resys.metis.search.spi.index.SiteSearchDocumentBuilder;
import io.resys.metis.search.spi.query.FtsSearchService;
import io.resys.metis.search.spi.query.HybridSearchService;
import io.resys.metis.search.spi.query.VectorSearchService;
import io.resys.metis.search.spi.store.MetisSearchSql;
import io.resys.metis.spi.ai.EmbeddingService;
import io.resys.metis.spi.ai.StructuredChatService;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.sqlclient.Pool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnBooleanProperty(value = "eveli.metis.search.enabled", havingValue = true, matchIfMissing = false)
@ConditionalOnBean(MetisConfig.class)
@EnableConfigurationProperties(value = { EveliPropsMetisSearch.class })
public class EveliAutoConfigMetisSearch {

  @Bean
  public MetisSearchConfig metisSearchConfig(EveliPropsMetisSearch props) {
    final var query = props.getQuery();
    final var indexing = props.getIndexing();
    return ImmutableMetisSearchConfig.builder()
        .locales(props.getLocales())
        .query(ImmutableMetisSearchQueryConfig.builder()
            .vectorWeight(query.getVectorWeight())
            .ftsWeight(query.getFtsWeight())
            .rrfK(query.getRrfK())
            .defaultLimit(query.getDefaultLimit())
            .maxQueryChars(query.getMaxQueryChars())
            .timeoutSeconds(query.getTimeoutSeconds())
            .minResultsBeforeFallback(query.getMinResultsBeforeFallback())
            .trgmThreshold(query.getTrgmThreshold())
            .scoreDropOffRatio(query.getScoreDropOffRatio())
            .maxResults(query.getMaxResults())
            .minVectorScore(query.getMinVectorScore())
            .minVectorScoreWithoutKeyword(query.getMinVectorScoreWithoutKeyword())
            .build())
        .indexing(ImmutableMetisSearchIndexingConfig.builder()
            .embeddingDimension(indexing.getEmbeddingDimension())
            .concurrency(indexing.getConcurrency())
            .documentTimeoutSeconds(indexing.getDocumentTimeoutSeconds())
            .abandonedAfterSeconds(indexing.getAbandonedAfterSeconds())
            .maxLlmContextChars(indexing.getMaxLlmContextChars())
            .maxPageChars(indexing.getMaxPageChars())
            .genericTopicThreshold(indexing.getGenericTopicThreshold())
            .metadataPromptVersion(indexing.getMetadataPromptVersion())
            .build())
        .build();
  }

  /** One reindex at a time. Plain ExecutorService so it does not steal {@code @Async} resolution. */
  @Bean(name = "metisSearchReindexExecutor", destroyMethod = "shutdownNow")
  public ExecutorService metisSearchReindexExecutor() {
    return Executors.newSingleThreadExecutor(namedThreads("metis-search-reindex-"));
  }

  @Bean(name = "metisSearchIndexingExecutor", destroyMethod = "shutdownNow")
  public ExecutorService metisSearchIndexingExecutor(MetisSearchConfig config) {
    final var concurrency = Math.max(1, config.getIndexing().getConcurrency());
    return Executors.newFixedThreadPool(concurrency, namedThreads("metis-search-indexing-"));
  }

  /** Bounded search pool. A full queue rejects; the portal then uses keyword search. */
  @Bean(name = "metisSearchQueryExecutor", destroyMethod = "shutdownNow")
  public ExecutorService metisSearchQueryExecutor(EveliPropsMetisSearch props) {
    final var concurrency = Math.max(1, props.getQuery().getConcurrency());
    final var queueCapacity = Math.max(1, props.getQuery().getQueueCapacity());
    return new ThreadPoolExecutor(
        concurrency, concurrency,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(queueCapacity),
        namedThreads("metis-search-query-"),
        new ThreadPoolExecutor.AbortPolicy());
  }

  /** Non-daemon: a daemon thread would die on JVM exit and leave the job row RUNNING. */
  private static ThreadFactory namedThreads(String prefix) {
    final var counter = new AtomicInteger();
    return runnable -> new Thread(runnable, prefix + counter.incrementAndGet());
  }

  @Bean(destroyMethod = "shutdown")
  public MetisSearchClient metisSearchClient(
      MetisConfig platform,
      MetisSearchConfig config,
      io.resys.limaone.program.Runtime runtime,
      Pool pgPool,
      EmbeddingModel embeddingModel,
      EmbeddingService embeddingService,
      StructuredChatService chatService,
      @Qualifier("metisSearchReindexExecutor") Executor reindexExecutor,
      @Qualifier("metisSearchIndexingExecutor") Executor indexingExecutor,
      @Qualifier("metisSearchQueryExecutor") Executor searchExecutor) {

    final var query = config.getQuery();
    final var indexing = config.getIndexing();

    final var db = MetisSearchSql.create(pgPool);
    final var documentBuilder = new SiteSearchDocumentBuilder(platform, indexing);
    final var contentReader = new SiteContentReader(runtime, config, documentBuilder);
    final var jobService = new ReindexJobService(
        db, Duration.ofSeconds(indexing.getAbandonedAfterSeconds()));
    final var indexingService = new IndexingService(
        contentReader,
        new MetadataGenerator(chatService),
        embeddingService,
        jobService,
        db,
        indexingExecutor,
        indexing);

    final var vectorSearch = new VectorSearchService(db, embeddingService, query);
    final var ftsSearch = new FtsSearchService(db, query);

    final var embeddingModelId = platform.getEmbeddingModelId().isBlank()
        ? embeddingModel.getClass().getSimpleName()
        : platform.getEmbeddingModelId();

    log.info("Metis site search enabled for locales: {}, embedding model: {}",
        config.getLocales(), embeddingModelId);

    return new MetisSearchClientImpl(
        config,
        new HybridSearchService(vectorSearch, ftsSearch, query),
        vectorSearch,
        ftsSearch,
        indexingService,
        jobService,
        reindexExecutor,
        searchExecutor,
        embeddingModelId);
  }

  @Bean
  public MetisLiveIndexTrigger metisLiveIndexTrigger(
      MetisSearchClient search,
      EveliPropsMetisSearch props,
      ObjectProvider<EveliEditEnvir> editEnvir) {
    return new MetisLiveIndexTrigger(
        search, props, editEnvir, Infrastructure.getDefaultWorkerPool());
  }

  @Bean
  public MetisLivePublicationReconciler metisLivePublicationReconciler(
      MetisLiveIndexTrigger trigger, EveliPropsMetisSearch props) {
    return new MetisLivePublicationReconciler(trigger, props);
  }

  @Bean
  public MetisReindexListener metisReindexListener(
      MetisSearchClient search, EveliPropsMetisSearch props, MetisLiveIndexTrigger trigger) {
    return new MetisReindexListener(search, props, trigger);
  }

  @Bean
  public GamutSiteSearchController gamutSiteSearchController(
      MetisSearchClient search, EveliPropsMetisSearch props) {
    return new GamutSiteSearchController(search, props);
  }

  @Bean
  public MetisSearchApiController metisSearchApiController(
      MetisSearchClient search, MetisLiveIndexTrigger trigger) {
    return new MetisSearchApiController(search, trigger);
  }
}
