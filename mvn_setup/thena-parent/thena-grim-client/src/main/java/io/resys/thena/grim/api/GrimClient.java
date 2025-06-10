package io.resys.thena.grim.api;

/*-
 * #%L
 * thena-grim-client
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
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimProjectObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface GrimClient {  
  TenantActions tenants();

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
}
