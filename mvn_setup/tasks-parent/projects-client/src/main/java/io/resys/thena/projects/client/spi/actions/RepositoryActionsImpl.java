package io.resys.thena.projects.client.spi.actions;

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

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.actions.RepositoryActions;
import io.resys.thena.projects.client.api.actions.RepositoryQuery;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.spi.ProjectsClientImpl;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepositoryActionsImpl implements RepositoryActions {
  private final DocumentStore ctx;
  @Override
  public Uni<Repo> getRepo() {
    return ctx.getRepo();
  }
  @Override
  public RepositoryQuery query() {
    DocumentStore.DocumentRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      @Override public Uni<TenantConfigClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public Uni<TenantConfigClient> create() { return repo.create().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public TenantConfigClient build() { return new ProjectsClientImpl(repo.build()); }
      @Override public Uni<TenantConfigClient> delete() { return repo.delete().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public Uni<TenantConfigClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new ProjectsClientImpl(ctx)); }
      @Override
      public RepositoryQuery repoName(String repoName, TenantRepoConfigType type) {
        repo.repoName(repoName).headName(MainBranch.HEAD_NAME);
        
        switch (type) {
        case CRM: { repo.repoType(RepoType.doc); break; }
        case DIALOB: { repo.repoType(RepoType.doc); break; }
        case TENANT: { repo.repoType(RepoType.doc); break; }

        case TASKS: { repo.repoType(RepoType.doc); break; }
        case STENCIL: { repo.repoType(RepoType.git); break; }
        case WRENCH: { repo.repoType(RepoType.git); break; }
        }
        return this;
      }
    };
  }
}
