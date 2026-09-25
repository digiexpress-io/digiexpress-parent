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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.Setter;

@Data @Setter
@ConfigurationProperties(prefix = "eveli.metis.search")
public class EveliPropsMetisSearch {

  /** Capability flag. Requires eveli.metis.enabled. Enabling search alone fails boot. */
  private Boolean enabled;

  /** Locales to index, comma separated. Only en, fi and sv have a PostgreSQL stemmer. */
  private String locales = "en, fi, sv";

  /** Start an indexing job on boot when the index is still empty. */
  private Boolean autoReindexOnStartup = true;
  /** Incremental reindex after a live publication is created or a scheduled one goes live. */
  private Boolean reindexOnDeployment = true;
  /** How often to check whether a scheduled publication has gone live. Also the catch-up after a restart. */
  private Integer livePublicationCheckSeconds = 60;

  private Query query = new Query();
  private Indexing indexing = new Indexing();

  @Data
  public static class Query {
    /** Weight of the semantic ranking in reciprocal rank fusion. */
    private Double vectorWeight = 0.8;
    /** Weight of the keyword ranking in reciprocal rank fusion. */
    private Double ftsWeight = 0.2;
    /** Reciprocal rank fusion constant. Higher values flatten the influence of the top ranks. */
    private Integer rrfK = 60;

    /** Default number of results when the caller does not pass a limit. */
    private Integer defaultLimit = 8;
    /** Below this number of keyword hits the trigram similarity fallback is used. */
    private Integer minResultsBeforeFallback = 1;
    /** Minimum trigram similarity for that fallback. */
    private Double trgmThreshold = 0.2;

    /** Public queries longer than this are truncated. */
    private Integer maxQueryChars = 400;
    /** Upper bound in seconds for answering one search. Query embedding gets two seconds less. */
    private Integer timeoutSeconds = 5;
    /** Size of the bounded search executor. */
    private Integer concurrency = 4;
    /** Concurrent query embeds; null uses {@code concurrency}. Use 1 with CPU-only Ollama. */
    private Integer embedConcurrency;
    /** Queue of the search executor. A full queue answers fallback: true without embedding. */
    private Integer queueCapacity = 50;
    /** In-memory per remote address. Over-limit answers fallback: true. */
    private Integer rateLimitRequests = 10;
    /** Window for {@link #rateLimitRequests}. */
    private Integer rateLimitWindowSeconds = 10;

    /** Drop results scoring below this fraction of the best score for the same query. */
    private Double scoreDropOffRatio = 0.90;

    /** Caller cannot ask for more than this. Keep equal to defaultLimit. */
    private Integer maxResults = 8;
    /** When even the best vector match is below this, the query has no semantic answer. */
    private Double minVectorScore = 0.30;

    /** Floor when FTS found nothing. Defaults are the bge-m3 calibration, where gibberish scores 0.30–0.45; gemini-embedding-2 uses 0.63. */
    private Double minVectorScoreWithoutKeyword = 0.45;
  }

  @Data
  public static class Indexing {
    /** Must match the embedding model, bge-m3 produces 1024 dimensions. Does not alter the schema. */
    private Integer embeddingDimension = 1024;

    /** 1 is optimal for CPU-only Ollama, raise when running Ollama on a GPU. */
    private Integer concurrency = 1;

    /** Upper bound for metadata plus embedding of one document, from when that document starts. */
    private Integer documentTimeoutSeconds = 600;
    /** How long a running job may report no progress before another instance may reclaim it. */
    private Integer abandonedAfterSeconds = 3600;

    /** Truncation for the text handed to the metadata model. */
    private Integer maxLlmContextChars = 600;
    /** Truncation for the indexed page body. */
    private Integer maxPageChars = 800;
    /** A topic linking more than this many workflows is treated as a generic listing page. */
    private Integer genericTopicThreshold = 4;

    /** Bump to invalidate stored hashes after a prompt change. */
    private String metadataPromptVersion = "v1";
  }

  public int resolvedQueryEmbedConcurrency() {
    final var query = this.query == null ? new Query() : this.query;
    if (query.getEmbedConcurrency() != null) {
      return Math.max(1, query.getEmbedConcurrency());
    }
    final var pool = query.getConcurrency() == null ? 4 : query.getConcurrency();
    return Math.max(1, pool);
  }

  public List<String> getLocales() {
    if (locales == null || locales.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.stream(locales.split(","))
        .map(String::trim)
        .filter(e -> !e.isEmpty())
        .map(String::toLowerCase)
        .toList();
  }
}
