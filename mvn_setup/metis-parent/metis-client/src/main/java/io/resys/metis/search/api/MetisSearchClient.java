package io.resys.metis.search.api;

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

import io.smallrye.mutiny.Uni;

public interface MetisSearchClient {

  SearchQuery query();

  default Uni<MetisSearchIndexStatus> startReindex(boolean force) {
    return startReindex(force, false, null);
  }

  default Uni<MetisSearchIndexStatus> startReindex(boolean force, boolean replace) {
    return startReindex(force, replace, null);
  }

  Uni<MetisSearchIndexStatus> startReindex(boolean force, boolean replace, String publicationId);

  boolean isPublicationIndexed(String publicationId);

  Uni<MetisSearchIndexStatus> cancelReindex();

  Uni<MetisSearchIndexStatus> getIndexStatus();

  boolean isReindexInFlight();

  boolean isIndexReadyForPortal();

  long countIndexedDocuments();

  interface SearchQuery {
    SearchQuery locale(String locale);

    SearchQuery limit(int limit);

    SearchQuery mode(SearchMode mode);

    Uni<List<MetisSearchResult>> findByText(String text);
  }

  enum SearchMode {
    HYBRID, VECTOR, FTS
  }
}
