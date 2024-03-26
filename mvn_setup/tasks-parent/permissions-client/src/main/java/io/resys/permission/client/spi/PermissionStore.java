package io.resys.permission.client.spi;

import org.immutables.value.Value;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.ThenaClient.OrgStructuredTenant;
import io.resys.thena.api.models.Repo;

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

public interface PermissionStore {
  PermissionRepositoryQuery query();
  PermissionStoreConfig getConfig();
  Uni<Repo> getRepo();
  OrgStructuredTenant getOrg(String repoId);
  PermissionStore withRepoId(String repoId);
  
  interface PermissionRepositoryQuery {
    PermissionRepositoryQuery repoName(String repoName);
    PermissionStore build();
    Uni<PermissionStore> delete();
    Uni<PermissionStore> create();
    Uni<PermissionStore> createIfNot();
    Uni<Void> deleteAll();
    
  } 

  @Value.Immutable
  public interface PermissionStoreConfig {
    ThenaClient getClient();
    String getRepoId();
    PermissionAuthorProvider getAuthor();
  }
  
  @FunctionalInterface
  interface PermissionAuthorProvider {
    String get();
  }
}
