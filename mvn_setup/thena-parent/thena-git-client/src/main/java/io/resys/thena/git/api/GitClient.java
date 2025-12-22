package io.resys.thena.git.api;

/*-
 * #%L
 * thena-git-client
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

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.GitRepoObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface GitClient {  
  TenantActions tenants();
  
  GitStructuredTenant git(String tenantIdOrName);
  GitStructuredTenant git(CreatedTenant repo);
  GitStructuredTenant git(Tenant repo);
  

  
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
    Uni<QueryEnvelope<GitRepoObjects>> get();
  }


}
