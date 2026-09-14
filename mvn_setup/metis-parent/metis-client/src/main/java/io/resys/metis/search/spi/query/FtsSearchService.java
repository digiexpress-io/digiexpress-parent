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

import java.util.List;

import io.resys.metis.search.api.MetisSearchQueryConfig;
import io.resys.metis.search.api.MetisSearchResult;
import io.resys.metis.search.spi.index.TsConfig;
import io.resys.metis.search.spi.store.MetisSearchDb;
import io.resys.metis.search.spi.store.MetisSearchSql;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FtsSearchService {

  private final MetisSearchDb db;
  private final MetisSearchQueryConfig config;

  public List<MetisSearchResult> rankedResults(String query, String locale, int limit) {
    final var tsConfig = TsConfig.forLocale(locale);
    final var primary = primaryHits(query, locale, limit);

    // Keep the full FTS list: weaker keyword hits still break ties in fusion.
    if (primary.size() >= config.getMinResultsBeforeFallback()) {
      return primary;
    }
    return fallback(query, locale, tsConfig, limit);
  }

  public List<MetisSearchResult> primaryHits(String query, String locale, int limit) {
    final var tsConfig = TsConfig.forLocale(locale);
    return MetisSearchSql.await(db.query().queryMetisSearchIndex()
        .findFtsPrimary(tsConfig, query, locale, limit));
  }

  /** Stemmed OR matching plus trigram word similarity. Sequential scan: {@code unaccent()} plus {@code word_similarity >=} cannot use the GIN index. */
  private List<MetisSearchResult> fallback(String query, String locale, String tsConfig, int limit) {
    return MetisSearchSql.await(db.query().queryMetisSearchIndex()
        .findFtsFallback(tsConfig, query, locale, config.getTrgmThreshold(), limit));
  }
}
