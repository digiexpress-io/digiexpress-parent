package io.thestencil.client.spi;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.GitBranchActions;
import io.resys.thena.api.actions.GitCommitActions;
import io.resys.thena.api.actions.GitDiffActions;
import io.resys.thena.api.actions.GitHistoryActions;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GitTagActions;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableBranch;
import io.thestencil.client.api.ImmutableStencilConfig;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilConfig;
import io.thestencil.client.api.StencilConfig.EntityState;
import io.thestencil.client.api.StencilStore;
import io.vertx.core.json.JsonObject;

public class StencilStoreInMemory implements StencilStore {
  private final StencilConfig config;
  
  public StencilStoreInMemory(ImmutableStencilConfig.Builder init) {
    super();
    init
      .authorProvider(() -> "not supported!")
      .serializer((entity) -> {
        try { 
          return new JsonObject(getObjectMapper().writeValueAsString(entity));
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .deserializer(new DeserializerInMemory())
      .gidProvider(type -> {
        throw new IllegalArgumentException("no read or writes supported!");
      })
      .repoName("in-memory")
      .headName("in-memory")
      .client(new ThenaClientInMemeory());
    this.config = init.build();
  }
  
  private ObjectMapper getObjectMapper() {
    return config.getObjectMapper();
  }
  
  private static class DeserializerInMemory implements StencilConfig.Deserializer {

    @Override
    public Entity<?> fromString(JsonObject value) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public <T extends EntityBody> Entity<T> fromString(EntityType type, JsonObject value) {
      throw new IllegalArgumentException("no read or writes supported!");
    }
    
  }
  
  private static class ThenaClientInMemeory implements ThenaClient {
    @Override
    public TenantActions tenants() {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public GitStructuredTenant git(String repoId) {
      return new GitStructuredTenant() {
        
        @Override
        public GitCommitActions commit() {
          throw new IllegalArgumentException("no read or writes supported!");
        }
        @Override
        public GitTagActions tag() {
          throw new IllegalArgumentException("no read or writes supported!");
        }
        @Override
        public GitDiffActions diff() {
          throw new IllegalArgumentException("no read or writes supported!");
        }
        @Override
        public GitHistoryActions history() {
          throw new IllegalArgumentException("no read or writes supported!");
        }
        @Override
        public GitPullActions pull() {
          throw new IllegalArgumentException("no read or writes supported!");
        }
        @Override
        public GitBranchActions branch() {
          throw new IllegalArgumentException("no read or writes supported!");
        }
        @Override
        public GitRepoQuery project() {
          throw new IllegalArgumentException("no read or writes supported!");
        }
      };
    }
    @Override
    public DocStructuredTenant doc(String repoId) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public OrgStructuredTenant org(String repoId) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public GitStructuredTenant git(TenantCommitResult repo) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public GitStructuredTenant git(Tenant repo) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public DocStructuredTenant doc(TenantCommitResult repo) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public DocStructuredTenant doc(Tenant repo) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public OrgStructuredTenant org(TenantCommitResult repo) {
      throw new IllegalArgumentException("no read or writes supported!");
    }

    @Override
    public OrgStructuredTenant org(Tenant repo) {
      throw new IllegalArgumentException("no read or writes supported!");
    }
    
  }

  @Override
  public StencilConfig getConfig() {
    return this.config;
  }
  
  @Override
  public <T extends EntityBody> Uni<Entity<T>> delete(Entity<T> toBeDeleted) {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public <T extends EntityBody> Uni<EntityState<T>> get(String blobId, EntityType type) {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public <T extends EntityBody> Uni<Entity<T>> save(Entity<T> toBeSaved) {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public <T extends EntityBody> Uni<Entity<T>> create(Entity<T> toBeSaved) {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public Uni<List<Entity<?>>> saveAll(List<Entity<?>> toBeSaved) {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public Uni<List<Entity<?>>> batch(BatchCommand batch) {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public QueryBuilder query() {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public StoreRepoBuilder repo() {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public String getRepoName() {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public String getHeadName() {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public String gid(EntityType type) {
    throw new IllegalArgumentException("no read or writes supported!");
  }

  @Override
  public StencilStore withRepo(String repoId, String headName) {
    return this;
  }

  @Override
  public BranchQuery queryBranches() {
    return new BranchQuery() {
      
      @Override
      public Uni<List<Branch>> findAll() {
        return Uni.createFrom().item(Arrays.asList(ImmutableBranch.builder().name(getHeadName()).commitId("in-memory").build()));
      }
    };
  }
}
