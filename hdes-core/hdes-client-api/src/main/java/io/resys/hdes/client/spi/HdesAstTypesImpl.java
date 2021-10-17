package io.resys.hdes.client.spi;

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

import org.codehaus.groovy.control.CompilerConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import groovy.lang.GroovyClassLoader;
import io.resys.hdes.client.api.HdesAstTypes;
import io.resys.hdes.client.spi.HdesTypeDefsFactory.ServiceInit;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.decision.DecisionAstBuilderImpl;
import io.resys.hdes.client.spi.flow.FlowAstBuilderImpl;
import io.resys.hdes.client.spi.groovy.GroovyCompilationCustomizer;
import io.resys.hdes.client.spi.groovy.ServiceAstBuilderImpl;

public class HdesAstTypesImpl implements HdesAstTypes {
  private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
  private final GroovyClassLoader gcl;
  private final HdesTypeDefsFactory typeDefs;
  private final HdesClientConfig config;
  
  public HdesAstTypesImpl(ObjectMapper objectMapper, ServiceInit init, HdesClientConfig config) {
    super();
    CompilerConfiguration groovyConfig = new CompilerConfiguration();
    groovyConfig.setTargetBytecode(CompilerConfiguration.JDK8);
    groovyConfig.addCompilationCustomizers(new GroovyCompilationCustomizer());
    
    this.config = config;
    this.gcl = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), groovyConfig);
    this.typeDefs = new HdesTypeDefsFactory(objectMapper, init);
  }
  @Override
  public DecisionAstBuilder decision() {
    return new DecisionAstBuilderImpl(typeDefs);
  }
  @Override
  public FlowAstBuilder flow() {
    return new FlowAstBuilderImpl(yaml, typeDefs, config.getFlowVisitors());
  }
  @Override
  public ServiceAstBuilder service() {
    return new ServiceAstBuilderImpl(typeDefs, gcl);
  }
  @Override
  public DataTypeAstBuilder dataType() {
    return typeDefs.dataType();
  }
}
