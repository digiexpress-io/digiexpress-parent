package io.resys.thena.contract.client.api;

/*-
 * #%L
 * thena-contract-client
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

public interface ContractClient {
  TenantActions tenants();
  
  ContractTenant withTenant();
  ContractTenant withTenant(String tenantIdOrName);
  ContractTenant withTenant(TenantCommitResult repo);
  ContractTenant withTenant(Tenant repo);

  
  // workflow/task like structure
  interface ContractTenant {
    String getTenantId();
    ContractCommitActions commit();
    ContractQueryActions find();
  }

}
