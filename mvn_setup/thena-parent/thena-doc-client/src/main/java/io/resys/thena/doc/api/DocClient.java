package io.resys.thena.doc.api;

/*-
 * #%L
 * thena-doc-client
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
import io.resys.thena.api.entities.doc.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface DocClient {
  TenantActions tenants();
  DocDataSource unwrap();
  
  DocStructuredTenant doc(String tenantIdOrName);
  DocStructuredTenant doc(TenantCommitResult repo);
  DocStructuredTenant doc(Tenant repo);
  
  
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
}
