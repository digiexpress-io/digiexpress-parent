package io.resys.thena.api;

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

import io.resys.thena.api.actions.FsCommitActions;
import io.resys.thena.api.actions.FsQueryActions;
import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.api.actions.OrgHistoryActions;
import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsProjectObjects;
import io.resys.thena.api.entities.org.ThenaOrgObjects.OrgProjectObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface ThenaClient {  
  TenantActions tenants();

  OrgStructuredTenant org(String tenantIdOrName);
  OrgStructuredTenant org(CreatedTenant repo);
  OrgStructuredTenant org(Tenant repo);

  FsStructuredTenant fs(String tenantIdOrName);
  FsStructuredTenant fs(CreatedTenant repo);
  FsStructuredTenant fs(Tenant repo);  
  
  
  interface FsStructuredTenant {
    String getTenantId();
    FsCommitActions commit();
    FsQueryActions find();
    FsProjectQuery tenants();
  }
  // build world state
  interface FsProjectQuery {
    Uni<QueryEnvelope<FsProjectObjects>> get();
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
}
