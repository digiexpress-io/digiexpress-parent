package io.thestencil.client.spi;

import java.util.List;

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
import java.util.stream.Collectors;

import io.resys.thena.api.actions.TenantActions.TenantStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableBranch;
import io.thestencil.client.api.ImmutableStencilConfig;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilConfig;
import io.thestencil.client.api.StencilStore;
import io.thestencil.client.spi.builders.QueryBuilderImpl;
import io.thestencil.client.spi.exceptions.ImmutableStoreExceptionMsg;
import io.thestencil.client.spi.exceptions.RepoException;
import io.thestencil.client.spi.exceptions.StoreException;


public class StencilStoreImpl extends PersistenceCommands implements StencilStore {
  
  public StencilStoreImpl(StencilConfig config) {
    super(config);
  }

  @Override
  public StencilConfig getConfig() {
    return super.config;
  }

  @Override
  public StencilStore withRepo(String repoId, String headName) {
    return new StencilStoreImpl(ImmutableStencilConfig.builder()
        .from(config)
        .repoName(repoId)
        .headName(headName)
        .build());
  }
  @Override
  public BranchQuery queryBranches() {
    return new BranchQuery() {
      @Override
      public Uni<List<Branch>> findAll() {
        return getConfig().getClient().git(getRepoName()).project()
            .get().onItem().transform(objects -> {
              if(objects.getStatus() != QueryEnvelopeStatus.OK) {
                throw new StoreException("STENCIL_BRANCH_QUERY_FAIL", null, 
                    ImmutableStoreExceptionMsg.builder()
                    .id(objects.getRepo().getId())
                    .value(objects.getRepo().getName())
                    .addAllArgs(objects.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
                    .build()); 
              }
              
              return objects.getObjects().getBranches().values().stream()
                  .map(branch -> {
                    final Branch result = ImmutableBranch.builder().commitId(branch.getCommit()).name(branch.getName()).build();
                    
                    return result;
                  })
                  .toList();
            });
      }
    };
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
        final var newRepo = client.tenants().commit().name(repoName, StructureType.git).build();
        return newRepo.onItem().transform((repoResult) -> {
          if(repoResult.getStatus() != TenantStatus.OK) {
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
        
        return client.git(config.getRepoName()).project().get().onItem().transformToUni(repo -> {
          if(repo == null) {
            return client.tenants().commit().name(config.getRepoName(), StructureType.git).build().onItem().transform(newRepo -> true); 
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
