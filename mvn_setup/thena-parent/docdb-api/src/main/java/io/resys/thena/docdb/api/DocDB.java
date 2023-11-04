package io.resys.thena.docdb.api;

import io.resys.thena.docdb.api.actions.BranchActions;

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

import io.resys.thena.docdb.api.actions.CommitActions;
import io.resys.thena.docdb.api.actions.DiffActions;
import io.resys.thena.docdb.api.actions.DocAppendActions;
import io.resys.thena.docdb.api.actions.DocFindActions;
import io.resys.thena.docdb.api.actions.HistoryActions;
import io.resys.thena.docdb.api.actions.PullActions;
import io.resys.thena.docdb.api.actions.RepoActions;
import io.resys.thena.docdb.api.actions.TagActions;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocProjectObjects;
import io.resys.thena.docdb.api.models.ThenaGitObjects.ProjectObjects;
import io.smallrye.mutiny.Uni;

public interface DocDB {
  RepoActions repo();
  GitModel git();
  DocModel doc();
  
  
  interface DocModel {
    DocAppendActions append();
    DocFindActions find();
    

    // build world state
    interface DocProjectQuery {
      DocProjectQuery projectName(String projectName);
      Uni<QueryEnvelope<DocProjectObjects>> get();
    }
  }
  
  interface GitModel {
    CommitActions commit();
    TagActions tag();
    DiffActions diff();
    HistoryActions history();
    PullActions pull();
    BranchActions branch();
    GitProjectQuery project();
    
    // build world state
    interface GitProjectQuery {
      GitProjectQuery projectName(String projectName);
      Uni<QueryEnvelope<ProjectObjects>> get();
    }

  }
}