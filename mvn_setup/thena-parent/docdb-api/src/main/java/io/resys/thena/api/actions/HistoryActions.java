package io.resys.thena.api.actions;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.entities.git.ThenaGitObjects.HistoryObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface HistoryActions {

  BlobHistoryQuery blobQuery();
  
  interface BlobHistoryQuery {
    BlobHistoryQuery branchName(String branchName);
    BlobHistoryQuery matchBy(MatchCriteria ... matchCriteria);
    BlobHistoryQuery matchBy(List<MatchCriteria> matchCriteria);

    BlobHistoryQuery docId(String docId); // entity name
    BlobHistoryQuery latestOnly(); // search only from last known version
    BlobHistoryQuery latestOnly(boolean latest); // search only from last known version
    Uni<QueryEnvelope<HistoryObjects>> get();
  }
}
