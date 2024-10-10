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

import java.util.function.Consumer;

import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableStencilConfig;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilConfig;
import io.thestencil.client.api.StencilStore;
import io.thestencil.client.spi.builders.QueryBuilderImpl;
import io.thestencil.client.spi.exceptions.RepoException;


public class StencilStoreImpl extends PersistenceCommands implements StencilStore {
  
  public StencilStoreImpl(StencilConfig config) {
    super(config);
  }

  @Override
  public StencilConfig getConfig() {
    return super.config;
  }
  
  @Override
  public QueryBuilder query() {
    return new QueryBuilderImpl(config);
  }

  @Override
  public StoreRepoBuilder repo() {
    return new StoreRepoBuilder() {
      private String repoName = config.getRepoName();
      private String headName = config.getHeadName();
      @Override
      public StoreRepoBuilder repoName(String repoName) {
        this.repoName = repoName;
        return this;
      }
      @Override
      public StoreRepoBuilder headName(String headName) {
        this.headName = headName;
        return this;
      }
      @Override
      public Uni<StencilStore> create() {
        StencilAssert.notNull(repoName, () -> "repoName must be defined!");
        final var client = config.getClient();
        final var newRepo = client.repo().create().name(repoName).build();
        return newRepo.onItem().transform((repoResult) -> {
          if(repoResult.getStatus() != RepoStatus.OK) {
            throw new RepoException("Can't create repository with name: '"  + repoName + "'!", repoResult); 
          }
          return build();
        });
      }
      @Override
      public StencilStore build() {
        StencilAssert.notNull(repoName, () -> "repoName must be defined!");
        return createWithNewConfig(ImmutableStencilConfig.builder()
            .from(config)
            .repoName(repoName)
            .headName(headName == null ? config.getHeadName() : headName)
            .build());
      }
      @Override
      public Uni<Boolean> createIfNot() {
        final var client = config.getClient();
        
        return client.repo().query().id(config.getRepoName()).get().onItem().transformToUni(repo -> {
          if(repo == null) {
            return client.repo().create().name(config.getRepoName()).build().onItem().transform(newRepo -> true); 
          }
          return Uni.createFrom().item(false);
        });
      }
    };
  }
  
  public String gid(EntityType type) {
    return config.getGidProvider().getNextId(type);
  }

  @Override
  public String getRepoName() {
    return config.getRepoName();
  }
  @Override
  public String getHeadName() {
    return config.getHeadName();
  }
  protected StencilStoreImpl createWithNewConfig(StencilConfig config) {
    return new StencilStoreImpl(config);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private ImmutableStencilConfig.Builder config = ImmutableStencilConfig.builder();
    
    public Builder config(Consumer<ImmutableStencilConfig.Builder> config) {
      config.accept(this.config);
      return this;
    }
    public StencilStoreImpl build() {
      return new StencilStoreImpl(config.build());
    }
  }
}
