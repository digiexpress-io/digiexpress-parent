package io.resys.thena.projects.client.spi.store;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.doc.ThenaDocConfig;

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

public interface ProjectStore {
  DocumentRepositoryQuery query();
  ThenaDocConfig getConfig();
  Uni<Tenant> getRepo();
  ProjectStore withRepoId(String repoId);
  
  interface DocumentRepositoryQuery {
    DocumentRepositoryQuery repoName(String repoName);
    DocumentRepositoryQuery externalId(String externalId);
    DocumentRepositoryQuery repoType(StructureType repoType);
    ProjectStore build();
    Uni<ProjectStore> delete();
    Uni<ProjectStore> create();
    Uni<ProjectStore> createIfNot();
    Uni<Void> deleteAll();
  } 
  

}
