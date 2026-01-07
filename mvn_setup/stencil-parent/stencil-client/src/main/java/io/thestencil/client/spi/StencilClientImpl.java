package io.thestencil.client.spi;

import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.thena.cockpit.client.api.CockpitAware;
import io.digiexpress.thena.cockpit.client.api.ImmutableCockpitAwareProps;

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

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableStencilConfig;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilStore;
import io.thestencil.client.spi.builders.SiteCommitLogBuilderImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StencilClientImpl implements StencilClient {
  public final static String COCKPIT_TYPE = "STENCIL";
  private final StencilStore store;
  private final CockpitAwareProps cockpitAwareProps;
  
  public StencilClientImpl(StencilStore store) {
    this.store = store;
    this.cockpitAwareProps = ImmutableCockpitAwareProps.builder()
        .tenantName(store.getRepoName())
        .provider(CockpitAware.EMPTY_PROVIDER)
        .build();
  }
  public StencilClientImpl(StencilStore store, Optional<CockpitAwareProvider> cockpitAwareProvider) {
    this.store = store;
    this.cockpitAwareProps = ImmutableCockpitAwareProps.builder()
        .tenantName(store.getRepoName())
        .provider(cockpitAwareProvider.orElse(CockpitAware.EMPTY_PROVIDER))
        .build();
  }
  
  @Override
  public Uni<StencilClient> withCockpit() {
    return cockpitAwareProps.getProvider().get().onItem()
        .transform(cockpit -> {
          
          if(cockpit.isEmpty()) {
            return this.withRepo(cockpitAwareProps.getTenantName());
          }
          
          final var tenantName = cockpit.get().getTenants().stream()
            .filter(t -> t.getCockpitConfigTenantType().equals(COCKPIT_TYPE))
            .map(e -> e.getExternalId())
            .findFirst()
            .orElse(cockpitAwareProps.getTenantName());
          
          return this.withRepo(tenantName);
        });
  }
  @Override
  public CockpitAwareProps getCockpitAwareProps() {
    return this.cockpitAwareProps;
  }
  @Override
  public Uni<StencilClient> withCockpitAwareProps() {
    return Uni.createFrom().item(this.withRepo(cockpitAwareProps.getTenantName())); 
  }
  
  @Override
  public MarkdownBuilder markdown() {
    return new MarkdownBuilderImpl();
  }
  @Override
  public SitesBuilder sites() {
    return new SitesBuilderImpl();
  }
  @Override
  public SiteCommitLogBuilder commitLog() {
    return new SiteCommitLogBuilderImpl(store);
  }
  @Override
  public ClientRepoBuilder repo() {
    return new ClientRepoBuilder() {
      private String repoName = store.getRepoName();
      private String headName = store.getHeadName();
      @Override
      public ClientRepoBuilder repoName(String repoName) {
        this.repoName = repoName;
        return this;
      }
      @Override
      public ClientRepoBuilder headName(String headName) {
        this.headName = headName;
        return this;
      }
      @Override
      public Uni<StencilClient> create() {
        StencilAssert.notNull(repoName, () -> "repoName must be defined!");
        return store.repo().repoName(repoName).headName(headName).createIfNot()
            .onItem().transform(newConfig -> {
              return new StencilClientImpl(newConfig.getItem2());
            });
      }
      @Override
      public StencilClient build() {
        StencilAssert.notNull(repoName, () -> "repoName must be defined!");
        final var newConfig = store.repo().repoName(repoName).headName(headName).build();
        return new StencilClientImpl(newConfig);
      }
    };
  }

  @Override
  public StencilStore getStore() {
    return store;
  }
  


  public StencilClientImpl withRepo(String repoId, String headName) {
    return new StencilClientImpl(store.withRepo(repoId, headName));
  }
  public StencilClientImpl withRepo(String repoId) {
    return new StencilClientImpl(store.withRepo(repoId, store.getHeadName()));
  }
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private ImmutableStencilConfig.Builder config = ImmutableStencilConfig.builder();
    private boolean inmemory = false;
    public Builder inmemory() {
      inmemory = true;
      return this;
    }
    
    public Builder config(Consumer<ImmutableStencilConfig.Builder> config) {
      config.accept(this.config);
      return this;
    }
    public Builder defaultObjectMapper() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new GuavaModule());
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.registerModule(new Jdk8Module());
      config.objectMapper(objectMapper);
      return this;
    }
    public StencilClientImpl build() {
      final var store = inmemory ? new StencilStoreInMemory(config) : new StencilStoreImpl(this.config.build());
      return new StencilClientImpl(store);
    }
  }
}
