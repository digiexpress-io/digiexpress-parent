package io.resys.thena.api;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.api.actions.DocCommitActions;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.GitBranchActions;
import io.resys.thena.api.actions.GitCommitActions;
import io.resys.thena.api.actions.GitDiffActions;
import io.resys.thena.api.actions.GitHistoryActions;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GitTagActions;
import io.resys.thena.api.actions.GrimCommitActions;
import io.resys.thena.api.actions.GrimQueryActions;
import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.api.actions.OrgHistoryActions;
import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.GitEntity.IsGitObject;
import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimProjectObjects;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.ThenaContainer;
import io.smallrye.mutiny.Uni;

public interface ThenaClient {  
  TenantActions tenants();
  
  GitStructuredTenant git(String tenantIdOrName);
  GitStructuredTenant git(TenantCommitResult repo);
  GitStructuredTenant git(Tenant repo);
  
  DocStructuredTenant doc(String tenantIdOrName);
  DocStructuredTenant doc(TenantCommitResult repo);
  DocStructuredTenant doc(Tenant repo);
  
  OrgStructuredTenant org(String tenantIdOrName);
  OrgStructuredTenant org(TenantCommitResult repo);
  OrgStructuredTenant org(Tenant repo);

  GrimStructuredTenant grim(String tenantIdOrName);
  GrimStructuredTenant grim(TenantCommitResult repo);
  GrimStructuredTenant grim(Tenant repo);

  
  // workflow/task like structure
  interface GrimStructuredTenant {
    String getTenantId();
    GrimCommitActions commit();
    GrimQueryActions find();
    GrimProjectQuery tenants();
  }
  // build world state
  interface GrimProjectQuery {
    Uni<QueryEnvelope<GrimProjectObjects>> get();
  }


  
  // organization tree like structure 
  interface OrgStructuredTenant {
    OrgCommitActions commit();
    OrgQueryActions find();
    OrgHistoryActions history();
    OrgProjectQuery tenants();
  }
  // build world state
  interface OrgProjectQuery {
    Uni<QueryEnvelope<OrgProjectObjects>> get();
  }


  // single document model
  interface DocStructuredTenant {
    DocCommitActions commit();
    DocQueryActions find();
  }
  // build world state
  interface DocProjectQuery {
    DocProjectQuery tenant(String tenantId);
    Uni<QueryEnvelope<DocTenantObjects>> get();
  }
  
  // multi doc model, cropped git replica
  interface GitStructuredTenant {
    GitCommitActions commit();
    GitTagActions tag();
    GitDiffActions diff();
    GitHistoryActions history();
    GitPullActions pull();
    GitBranchActions branch();
    GitTenantQuery tenants();
  }
  
  // build world state
  interface GitTenantQuery {
    Uni<QueryEnvelope<ThenaClient.GitRepoObjects>> get();
  }

  @Value.Immutable 
  interface GitRepoObjects extends ThenaContainer {
    Map<String, Branch> getBranches();
    Map<String, Tag> getTags();
    Map<String, IsGitObject> getValues();   
  }
}
