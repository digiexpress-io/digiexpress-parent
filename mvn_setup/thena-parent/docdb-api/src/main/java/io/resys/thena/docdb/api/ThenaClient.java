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
import io.resys.thena.docdb.api.actions.DocCommitActions;
import io.resys.thena.docdb.api.actions.DocQueryActions;
import io.resys.thena.docdb.api.actions.HistoryActions;
import io.resys.thena.docdb.api.actions.OrgCommitActions;
import io.resys.thena.docdb.api.actions.OrgHistoryActions;
import io.resys.thena.docdb.api.actions.OrgQueryActions;
import io.resys.thena.docdb.api.actions.PullActions;
import io.resys.thena.docdb.api.actions.TagActions;
import io.resys.thena.docdb.api.actions.TenantActions;
import io.resys.thena.docdb.api.actions.TenantActions.RepoResult;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocProjectObjects;
import io.resys.thena.docdb.api.models.ThenaGitObjects.GitRepoObjects;
import io.resys.thena.docdb.api.models.ThenaOrgObjects.OrgProjectObjects;
import io.smallrye.mutiny.Uni;

public interface ThenaClient {  
  TenantActions tenants();
  
  GitStructuredTenant git(String tenantIdOrName);
  GitStructuredTenant git(RepoResult repo);
  GitStructuredTenant git(Repo repo);
  
  DocStructuredTenant doc(String tenantIdOrName);
  DocStructuredTenant doc(RepoResult repo);
  DocStructuredTenant doc(Repo repo);
  
  OrgStructuredTenant org(String tenantIdOrName);
  OrgStructuredTenant org(RepoResult repo);
  OrgStructuredTenant org(Repo repo);
  

  interface OrgStructuredTenant {
    OrgCommitActions commit();
    OrgQueryActions find();
    OrgHistoryActions history();
    OrgProjectQuery project();

    // build world state
    interface OrgProjectQuery {
      Uni<QueryEnvelope<OrgProjectObjects>> get();
    }
  }


  // single document model
  interface DocStructuredTenant {
    DocCommitActions commit();
    DocQueryActions find();
    

    // build world state
    interface DocProjectQuery {
      DocProjectQuery projectName(String projectName);
      Uni<QueryEnvelope<DocProjectObjects>> get();
    }
  }

  
  // multi doc model, cropped git replica
  interface GitStructuredTenant {
    CommitActions commit();
    TagActions tag();
    DiffActions diff();
    HistoryActions history();
    PullActions pull();
    BranchActions branch();
    GitRepoQuery project();
    
    // build world state
    interface GitRepoQuery {
      Uni<QueryEnvelope<GitRepoObjects>> get();
    }

  }
}