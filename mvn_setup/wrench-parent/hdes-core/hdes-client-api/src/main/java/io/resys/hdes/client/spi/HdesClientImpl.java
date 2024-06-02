package io.resys.hdes.client.spi;

import java.util.ArrayList;
import java.util.Arrays;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesAstTypes;
import io.resys.hdes.client.api.HdesCache;
import io.resys.hdes.client.api.HdesClient;
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
import io.resys.hdes.client.spi.util.HdesAssert;

public class HdesClientImpl implements HdesClient {

  private final HdesTypesMapper defs;
  private final HdesAstTypes ast;
  private final HdesClientConfig config;
  
  public HdesClientImpl(HdesTypesMapper types, HdesAstTypes ast, HdesClientConfig config) {
    super();
    this.defs = types;
    this.ast = ast;
    this.config = config;
  }
  @Override
  public ExecutorBuilder executor(ProgramEnvir envir) {
    return new HdesClientExecutorBuilder(envir, defs, config.getDependencyInjectionContext());    
  }
  @Override
  public EnvirBuilder envir() {
    ProgramEnvirFactory factory = new ProgramEnvirFactory(ast, defs, config);
    return new HdesClientEnvirBuilder(factory, defs);
  }
  @Override
  public AstBuilder ast() {
    return new HdesClientAstBuilder(defs, ast);
  }
  @Override
  public HdesAstTypes types() {
    return ast;
  }
  @Override
  public HdesTypesMapper mapper() {
    return defs;
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
        return new DecisionProgramBuilder(defs).build(ast);
      }
      @Override
      public FlowProgram ast(AstFlow ast) {
        return new FlowProgramBuilder(defs).build(ast);
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
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {  
    private ObjectMapper objectMapper;
    private ServiceInit serviceInit;
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
    public HdesClientImpl build() {
      HdesAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      HdesAssert.notNull(serviceInit, () -> "serviceInit must be defined!");
      HdesAssert.notNull(dependencyInjectionContext, () -> "dependencyInjectionContext must be defined!");
      
      HdesCache cache = this.cache;
      if(cache == null) {
        cache = HdesClientEhCache.builder().build("");
      }
      
      final var config = new HdesClientConfigImpl(flowVisitors, cache, serviceInit, dependencyInjectionContext);
      final var types = new HdesTypeDefsFactory(objectMapper, config);
      final var ast = new HdesAstTypesImpl(objectMapper, config);
      return new HdesClientImpl(types, ast, config);
    }
  }

  private static class HdesClientConfigImpl implements HdesClientConfig {
    private final List<AstFlowNodeVisitor> flowVisitors = new ArrayList<>();
    private final HdesCache cache;
    private final ServiceInit serviceInit;
    private final DependencyInjectionContext dependencyInjectionContext;
    
    public HdesClientConfigImpl(List<AstFlowNodeVisitor> flowVisitors, HdesCache cache, ServiceInit serviceInit, DependencyInjectionContext dependencyInjectionContext) {
      this.flowVisitors.addAll(flowVisitors);
      this.cache = cache;
      this.serviceInit = serviceInit;
      this.dependencyInjectionContext = dependencyInjectionContext;
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
    public List<AstFlowNodeVisitor> getFlowVisitors() {
      return flowVisitors;
    }
    @Override
    public HdesClientConfig config(AstFlowNodeVisitor... changes) {
      this.flowVisitors.addAll(Arrays.asList(changes));
      return this;
    }
    @Override
    public DependencyInjectionContext getDependencyInjectionContext() {
      return dependencyInjectionContext;
    }
  }
}
