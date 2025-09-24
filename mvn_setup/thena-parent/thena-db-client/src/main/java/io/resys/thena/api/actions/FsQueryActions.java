package io.resys.thena.api.actions;

/*-
 * #%L
 * thena-db-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.entities.fs.FsUniqueDirentLabel;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsObject.FsDocType;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;



public interface FsQueryActions {

  DirentQuery direntQuery();
  DirentLabelQuery direntLabelQuery();

  
  interface DirentLabelQuery {
    Uni<List<FsUniqueDirentLabel>> findAllUnique();
  }
  
  interface DirentQuery {
    // optimization, exclude explicitly doc-s that we don't need 
    DirentQuery excludeDocs(FsDocType... docs);
    
    DirentQuery archived(FsArchiveQueryType includeArchived);
    DirentQuery addDirentId(List<String> ids); // include only data for given dirents
    DirentQuery lockForUpdate();
    
    
    Uni<QueryEnvelope<FsDirentContainer>> get(String direntIdOrExtId);
    Uni<QueryEnvelopeList<FsDirentContainer>> findAll();
  }
  
  enum FsArchiveQueryType {
    ALL, ONLY_ARCHIVED, ONLY_IN_FORCE
  }
}
