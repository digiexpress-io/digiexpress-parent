package io.resys.crm.client.spi.store;

import io.resys.thena.api.entities.Tenant;

/*-
 * #%L
 * thena-tasks-client
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

import io.smallrye.mutiny.Uni;

public interface CrmStore {
  CrmTenantQuery query();
  CrmStoreConfig getConfig();
  Uni<Tenant> getRepo();
  CrmStore withRepoId(String repoId);
  
  interface CrmTenantQuery {
    CrmTenantQuery repoName(String repoName);
    CrmTenantQuery headName(String headName);
    CrmStore build();
    Uni<CrmStore> delete();
    Uni<CrmStore> create();
    Uni<CrmStore> createIfNot();
    Uni<Void> deleteAll();
  } 
  

}
