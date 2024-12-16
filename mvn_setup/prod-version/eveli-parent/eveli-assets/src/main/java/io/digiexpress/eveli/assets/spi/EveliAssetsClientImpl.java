package io.digiexpress.eveli.assets.spi;

/*-
 * #%L
 * eveli-assets
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClientConfig;
import io.digiexpress.eveli.assets.api.ImmutableEveliAssetClientConfig;
import io.digiexpress.eveli.assets.spi.builders.CrudBuilderImpl;
import io.digiexpress.eveli.assets.spi.builders.QueryBuilderImpl;
import io.digiexpress.eveli.assets.spi.exceptions.AssetsAssert;
import io.digiexpress.eveli.assets.spi.exceptions.RepoException;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class EveliAssetsClientImpl implements EveliAssetClient {
  private final EveliAssetClientConfig config;

  @Override
  public EveliAssetClientConfig getConfig() {
    return this.config;
  }
  
  @Override
  public CrudBuilder crudBuilder() {
    return new CrudBuilderImpl(config);
  }

  @Override
  public QueryBuilder queryBuilder() {
    return new QueryBuilderImpl(config);
  }
  
  @Override
  public RepoBuilder repoBuilder() {
    return new RepoBuilder() {
      private String repoName = config.getRepoName();
      private String headName = config.getHeadName();
      @Override
      public RepoBuilder repoName(String repoName) {
        this.repoName = repoName;
        return this;
      }
      @Override
      public RepoBuilder headName(String headName) {
        this.headName = headName;
        return this;
      }
      @Override
      public Uni<EveliAssetClient> create() {
        AssetsAssert.notNull(repoName, () -> "repoName must be defined!");
        final var client = config.getClient();
        final var newRepo = client.tenants().commit().name(repoName, StructureType.git).build();
        
        return newRepo.onItem().transform((repoResult) -> {
          if(repoResult.getStatus() != CommitStatus.OK) {
            throw new RepoException("Can't create repository with name: '"  + repoName + "'!", repoResult); 
          }
          return build();
        });
      }
      @Override
      public EveliAssetClient build() {
        AssetsAssert.notNull(repoName, () -> "repoName must be defined!");
        return createWithNewConfig(ImmutableEveliAssetClientConfig.builder()
            .from(config)
            .repoName(repoName)
            .headName(headName == null ? config.getHeadName() : headName)
            .build());
      }
      @Override
      public Uni<Boolean> createIfNot() {
        final var client = config.getClient();
        
        return client.git(config.getRepoName()).tenants().get().onItem().transformToUni(repo -> {
          if(repo == null) {
            return client.tenants().commit().name(config.getRepoName(), StructureType.git).build().onItem().transform(newRepo -> true);
          }
          return Uni.createFrom().item(false);
        });
      }
    };
  }

  private EveliAssetsClientImpl createWithNewConfig(EveliAssetClientConfig config) {
    return new EveliAssetsClientImpl(config);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private ImmutableEveliAssetClientConfig.Builder config = ImmutableEveliAssetClientConfig.builder();
    
    public Builder config(Consumer<ImmutableEveliAssetClientConfig.Builder> config) {
      config.accept(this.config);
      return this;
    }
    public EveliAssetsClientImpl build() {
      return new EveliAssetsClientImpl(config.build());
    }
  }

}
