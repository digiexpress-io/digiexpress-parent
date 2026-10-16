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

  IndexQuery index();

  interface SearchQuery {
    SearchQuery locale(String locale);

    SearchQuery limit(int limit);

    SearchQuery mode(SearchMode mode);

    Uni<List<MetisSearchResult>> findByText(String text);
  }

  interface IndexQuery {

    /**
     * Claims at most one cluster-wide job. {@code force} rebuilds every document even
     * when the content hash matches. {@code replace} cancels an in-flight job and starts
     * another once it drains. {@code publicationId} is stamped on the job so later
     * ticks can skip work that is already indexed.
     *
     * @return {@code accepted=false} (HTTP 409 at the API) when another replica already
     *         holds the in-flight claim; that is the lock working, not a failure
     */
    Uni<MetisSearchIndexStatus> startReindex(boolean force, boolean replace, String publicationId);

    default Uni<MetisSearchIndexStatus> startReindex(boolean force) {
      return startReindex(force, false, null);
    }

    default Uni<MetisSearchIndexStatus> startReindex(boolean force, boolean replace) {
      return startReindex(force, replace, null);
    }

    Uni<MetisSearchIndexStatus> cancelReindex();

    Uni<MetisSearchIndexStatus> getIndexStatus();

    /**
     * True when a completed or still in-flight job already covers this live publication at the
     * current bundle hash, with the current embedding model.
     */
    boolean isPublicationIndexed(String publicationId);

    /** True while any replica holds a RUNNING or CANCELLING job. */
    boolean isReindexInFlight();

    /**
     * True only when the latest job has COMPLETED with the current embedding model. Until then
     * the portal serves keyword search.
     */
    boolean isIndexReadyForPortal();

    /** True when the latest completed job used the current embedding model. False after a model switch. */
    boolean isIndexCurrent();

    long countIndexedDocuments();
  }

  enum SearchMode {
    HYBRID, VECTOR, FTS
  }
}
