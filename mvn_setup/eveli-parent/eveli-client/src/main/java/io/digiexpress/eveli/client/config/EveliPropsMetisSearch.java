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

  private Boolean enabled;

  /** Locales to index, comma separated. Only en, fi and sv have a PostgreSQL stemmer. */
  private String locales = "en, fi, sv";

  private Boolean autoReindexOnStartup = true;
  private Boolean reindexOnDeployment = true;
  private Integer livePublicationCheckSeconds = 60;

  private Query query = new Query();
  private Indexing indexing = new Indexing();

  @Data
  public static class Query {
    private Double vectorWeight = 0.8;
    private Double ftsWeight = 0.2;
    private Integer rrfK = 60;

    private Integer defaultLimit = 8;
    private Integer minResultsBeforeFallback = 1;
    private Double trgmThreshold = 0.2;

    private Integer maxQueryChars = 400;
    private Integer timeoutSeconds = 5;
    private Integer concurrency = 4;
    /** Concurrent query embeds; null uses {@code concurrency}. Use 1 with CPU-only Ollama. */
    private Integer embedConcurrency;
    private Integer queueCapacity = 50;
    private Integer rateLimitRequests = 10;
    private Integer rateLimitWindowSeconds = 10;

    /** Drop results scoring below this fraction of the best score for the same query. */
    private Double scoreDropOffRatio = 0.90;

    private Integer maxResults = 8;
    private Double minVectorScore = 0.30;

    /** Floor when FTS found nothing. */
    private Double minVectorScoreWithoutKeyword = 0.45;
  }

  @Data
  public static class Indexing {
    /** Must match the embedding model, bge-m3 produces 1024 dimensions. */
    private Integer embeddingDimension = 1024;

    /** 1 is optimal for CPU-only Ollama, raise when running Ollama on a GPU. */
    private Integer concurrency = 1;

    private Integer documentTimeoutSeconds = 600;
    private Integer abandonedAfterSeconds = 3600;

    private Integer maxLlmContextChars = 600;
    private Integer maxPageChars = 800;
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
