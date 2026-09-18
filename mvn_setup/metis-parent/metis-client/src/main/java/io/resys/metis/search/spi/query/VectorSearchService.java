package io.resys.metis.search.spi.query;

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

import io.resys.metis.search.api.MetisSearchQueryConfig;
import io.resys.metis.search.api.MetisSearchResult;
import io.resys.metis.search.spi.store.MetisSearchDb;
import io.resys.metis.search.spi.store.MetisSearchSql;
import io.resys.metis.spi.ai.EmbeddingService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VectorSearchService {

  private final MetisSearchDb db;
  private final EmbeddingService embeddingService;
  private final MetisSearchQueryConfig config;

  public List<MetisSearchResult> rankedResults(String query, String locale, int limit) {
    return relevantOnly(nearest(query, locale, limit));
  }

  private List<MetisSearchResult> relevantOnly(List<MetisSearchResult> rows) {
    if (rows.isEmpty()) {
      return rows;
    }
    final var best = rows.get(0).getVectorScore();
    if (best == null || best < config.getMinVectorScore()) {
      return List.of();
    }
    final var floor = best * config.getScoreDropOffRatio();
    return rows.stream()
        .filter(row -> row.getVectorScore() != null && row.getVectorScore() >= floor)
        .toList();
  }

  private Duration embedTimeout() {
    return Duration.ofSeconds(Math.max(1L, config.getTimeoutSeconds() - 2L));
  }

  private List<MetisSearchResult> nearest(String query, String locale, int limit) {
    final var queryVector = embeddingService.embedQuery(query, embedTimeout());
    return MetisSearchSql.await(db.query().queryMetisSearchIndex()
        .findNearest(MetisSearchSql.toVectorLiteral(queryVector), locale, limit));
  }
}
