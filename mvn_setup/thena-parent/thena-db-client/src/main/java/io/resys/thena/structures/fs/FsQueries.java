package io.resys.thena.structures.fs;

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

import io.resys.thena.api.actions.FsQueryActions.FsArchiveQueryType;
import io.resys.thena.api.entities.fs.FsCommit;
import io.resys.thena.api.entities.fs.FsCommitTree;
import io.resys.thena.api.entities.fs.FsUniqueDirentLabel;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsObject.FsDocType;
import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface FsQueries {
  ThenaDataSource getDataSource();
  InternalDirentQuery dirents();
  InternalDirentLabelQuery direntLabels();
  
  InternalCommitTreeQuery commitTree();
  InternalCommitQuery commit();
  InternalDirentSequence direntSequences();
  
  
  interface InternalCommitTreeQuery {
    Uni<List<FsCommitTree>> findAllByDirentId(String direntId);
  }
  
  interface InternalCommitQuery {
    Uni<List<FsCommit>> findAllByDirentId(String direntId);
  }
  

  interface InternalDirentSequence {
    Uni<Long> nextVal();
    Uni<List<Long>> nextVal(long howMany);
  }

  interface InternalDirentLabelQuery {
    Uni<List<FsUniqueDirentLabel>> findAllUnique();
  }    
  
  interface InternalDirentQuery {
    InternalDirentQuery lockForUpdate();
    
    InternalDirentQuery onlyDocs(FsDocType ...docs);
    
    InternalDirentQuery excludeDocs(FsDocType ...docs); // multiple will be OR
    InternalDirentQuery archived(FsArchiveQueryType includeArchived); // true to exclude any tasks with archiveAt date present
    InternalDirentQuery direntId(String ...direntId); // multiple will be OR
    
    Multi<FsDirentContainer> findAll();
    Uni<FsDirentContainer> getById(String direntId);
  }
}
