package io.resys.hdes.client.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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


import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.thena.cockpit.client.api.ImmutableCockpitAwareProps;
import io.resys.hdes.client.api.HdesAstTypes;
import io.resys.hdes.client.api.HdesCache;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ServiceProgram;
import io.resys.hdes.client.spi.cache.HdesClientEhCache;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.config.HdesClientConfig.AstFlowNodeVisitor;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.decision.DecisionCSVBuilder;
import io.resys.hdes.client.spi.decision.DecisionProgramBuilder;
import io.resys.hdes.client.spi.envir.ProgramEnvirFactory;
import io.resys.hdes.client.spi.flow.FlowProgramBuilder;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.hdes.client.spi.groovy.ServiceProgramBuilder;
import io.resys.hdes.client.spi.summary.HdesClientSummaryBuilder;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class HdesClientImpl implements HdesClient {
  private final HdesStore store;
  private final HdesClientConfig config;
  private final CockpitAwareProps cockpitAwareProps;
  private final static String COCKPIT_TYPE = "WRENCH";
  
  public HdesClientImpl(HdesStore store, HdesClientConfig config) {
    super();
    this.store = store;
    this.config = config;
    this.cockpitAwareProps = ImmutableCockpitAwareProps.builder()
        .tenantName(store.getRepoName())
        .autoCreate(false)
        .resolver(Uni.createFrom().item(Optional.empty()))
        .build();
  }
  
  @Override
  public Uni<HdesClient> withCockpit() {
    return cockpitAwareProps.getResolver().onItem()
        .transform(cockpit -> {
          
          config.getCache().flushAll();
          
          if(cockpit.isEmpty()) {
            return this.repo().repoName(cockpitAwareProps.getTenantName()).build();
          }
          
          final var tenantName = cockpit.get().getTenants().stream()
            .filter(t -> t.getCockpitConfigTenantType().equals(COCKPIT_TYPE))
            .map(e -> e.getExternalId())
            .findFirst()
            .orElse(cockpitAwareProps.getTenantName());
          
          return this.repo().repoName(tenantName).build();
        });
  }
  @Override
  public CockpitAwareProps getCockpitAwareProps() {
    return this.cockpitAwareProps;
  }
  @Override
  public Uni<HdesClient> withCockpitAwareProps() {
    return Uni.createFrom().item(this.repo().repoName(cockpitAwareProps.getTenantName()).build()); 
  }
  
  @Override
  public ExecutorBuilder executor(ProgramEnvir envir) {
    return new HdesClientExecutorBuilder(envir, config.getTypes(), config.getDependencyInjectionContext());    
  }
  @Override
  public EnvirBuilder envir() {
    ProgramEnvirFactory factory = new ProgramEnvirFactory(config);
    return new HdesClientEnvirBuilder(factory, config.getTypes());
  }

  @Override
  public SummaryBuilder summary() {
    return new HdesClientSummaryBuilder();
  }

  @Override
  public AstBuilder ast() {
    return new HdesClientAstBuilder(config.getTypes(), config.getAst());
  }
  @Override
  public HdesStore store() {
    return store;
  }
  @Override
  public HdesAstTypes types() {
    return config.getAst();
  }
  @Override
  public HdesTypesMapper mapper() {
    return config.getTypes();
  }
  @Override
  public ProgramBuilder program() {
    return new ProgramBuilder() {
      @Override
      public ServiceProgram ast(AstService ast) {
        return new ServiceProgramBuilder(config).build(ast);
      }
      @Override
      public DecisionProgram ast(AstDecision ast) {
        return new DecisionProgramBuilder(config.getTypes()).build(ast);
      }
      @Override
      public FlowProgram ast(AstFlow ast) {
        return new FlowProgramBuilder(config.getTypes()).build(ast);
      }
    };
  }
  @Override
  public CSVBuilder csv() {
    return new CSVBuilder() {
      @Override
      public String ast(AstDecision ast) {
        return DecisionCSVBuilder.build(ast);
      }
    };
  }
  
  public HdesClientConfig config() {
    return this.config;
  }

  @Override
  public HdesClient withBranch(String branchName) {
    final var newStore = store.withBranch(branchName);
    final var newConfig = config.withBranch(branchName);
    return new HdesClientImpl(newStore, newConfig);
  }

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {  
    private ObjectMapper objectMapper;
    private ServiceInit serviceInit;
    private HdesStore store;
    private HdesCache cache;
    private DependencyInjectionContext dependencyInjectionContext;
    private final List<AstFlowNodeVisitor> flowVisitors = new ArrayList<>(Arrays.asList(new IdValidator()));
    

    public Builder flowVisitors(AstFlowNodeVisitor ...visitors) {
      this.flowVisitors.addAll(Arrays.asList(visitors));
      return this;
    }
    public Builder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public Builder dependencyInjectionContext(DependencyInjectionContext dependencyInjectionContext) {
      this.dependencyInjectionContext = dependencyInjectionContext;
      return this;
    }
    public Builder serviceInit(ServiceInit serviceInit) {
      this.serviceInit = serviceInit;
      return this;
    }
    public Builder cache(HdesCache cache) {
      this.cache = cache;
      return this;
    }
    public Builder store(HdesStore store) {
      this.store = store;
      return this;
    }
    public HdesClientImpl build() {
      HdesAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      HdesAssert.notNull(serviceInit, () -> "serviceInit must be defined!");
      HdesAssert.notNull(store, () -> "store must be defined!");
      HdesAssert.notNull(dependencyInjectionContext, () -> "dependencyInjectionContext must be defined!");
      
      HdesCache cache = this.cache;
      if(cache == null) {
        cache = HdesClientEhCache.builder().build(store.getRepoName());
      }
      final var types = new HdesTypeDefsFactory(objectMapper);
      final var ast = new HdesAstTypesImpl(types, flowVisitors);
      final var config = new HdesClientConfigImpl(cache, serviceInit, dependencyInjectionContext, types, ast);

      return new HdesClientImpl(store, config);
    }
  }

  public static class HdesClientConfigImpl implements HdesClientConfig {

    private final HdesCache cache;
    private final ServiceInit serviceInit;
    private final DependencyInjectionContext dependencyInjectionContext;
    private final HdesTypesMapper types;
    private final HdesAstTypes ast;

    private final Optional<String> branchName;
    
    public HdesClientConfigImpl(
        HdesCache cache, ServiceInit serviceInit, 
        DependencyInjectionContext dependencyInjectionContext, 
        HdesTypesMapper types,
        HdesAstTypes ast) {

      this.cache = cache;
      this.types = types;
      this.ast = ast;
      this.serviceInit = serviceInit;
      this.dependencyInjectionContext = dependencyInjectionContext;
      this.branchName = Optional.empty();
    }

    public HdesClientConfigImpl(
        HdesCache cache, ServiceInit serviceInit, 
        DependencyInjectionContext dependencyInjectionContext, 
        String branchName,
        HdesTypesMapper types,
        HdesAstTypes ast) {
      this.cache = cache;
      this.types = types;
      this.ast = ast;
      this.serviceInit = serviceInit;
      this.dependencyInjectionContext = dependencyInjectionContext;
      this.branchName = Optional.ofNullable(branchName);
    }
    @Override
    public ServiceInit getServiceInit() {
      return serviceInit;
    }
    @Override
    public HdesCache getCache() {
      return cache;
    }
    @Override
    public Optional<String> getBranchName() {
      return branchName;
    }
    @Override
    public HdesClientConfig withBranch(String branchName) {
      Objects.requireNonNull(branchName, () -> "branchName can't be null!");
      return new HdesClientConfigImpl(cache.withName(branchName), serviceInit, dependencyInjectionContext, branchName, types, ast);
    }

    @Override
    public DependencyInjectionContext getDependencyInjectionContext() {
      return dependencyInjectionContext;
    }

    public HdesTypesMapper getTypes() {
      return types;
    }

    public HdesAstTypes getAst() {
      return ast;
    }
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
      public Uni<HdesClient> create() {
        HdesAssert.notNull(repoName, () -> "repoName must be defined!");
        return store().repo().repoName(repoName).headName(headName).createIfNot()
            .onItem().transform(tuple -> {
              
              
              return new HdesClientImpl(tuple.getItem2(), 
                  new HdesClientConfigImpl(
                      config.getCache().withName(repoName), 
                      config.getServiceInit(),
                      config.getDependencyInjectionContext(),
                      config.getTypes(),
                      config.getAst()
              ));
            });
      }
      @Override
      public HdesClient build() {
        HdesAssert.notNull(repoName, () -> "repoName must be defined!");
        final var newStore = store().repo().repoName(repoName).headName(headName).build();
        final var newCache = config.getCache().withName(repoName);
        final var newConfig = new HdesClientConfigImpl( 
            newCache, 
            config.getServiceInit(), 
            config.getDependencyInjectionContext(),
            config.getTypes(),
            config.getAst());
        return new HdesClientImpl(newStore, newConfig);
      }
    };
  }
}
