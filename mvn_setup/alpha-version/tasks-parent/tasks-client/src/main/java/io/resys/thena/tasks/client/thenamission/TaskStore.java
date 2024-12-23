package io.resys.thena.tasks.client.thenamission;

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

public interface TaskStore {
  TaskTenantQuery query();
  TaskStoreConfig getConfig();
  Uni<Tenant> getRepo();
  TaskStore withTenantId(String tenantId);
  
  interface TaskTenantQuery {
    TaskTenantQuery tenantName(String tenantName);
    TaskStore build();
    Uni<TaskStore> delete();
    Uni<TaskStore> create();
    Uni<TaskStore> createIfNot();
  } 
}
