package io.resys.hdes.compiler.spi.units;

/*-
 * #%L
 * hdes-compiler
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.Optional;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceType;
import io.resys.hdes.compiler.spi.spec.JavaSpecUtil;
import io.resys.hdes.executor.api.HdesDef;
import io.resys.hdes.executor.api.HdesDefPromise;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.api.TraceBody.Accepts;
import io.resys.hdes.executor.api.TraceBody.Returns;

public class ImmutableCompilerNode implements CompilerNode {
  
  private final String root;
  private final RootNode ctx;
  private final String fl;
  private final String dt;
  private final String st;
  
  public ImmutableCompilerNode(
      RootNode envir, String root, String fl, String dt, String st) {
    super();
    this.ctx = envir;
    this.root = root;
    this.fl = root + "." + fl;
    this.dt = root + "." + dt;
    this.st = root + "." + st;
  }
  

  @Override
  public Token getToken() {
    return ctx.getToken();
  }
  
  @Override
  public FlowUnit fl(FlowBody body) {
    CompilerType compilerType = createType(fl, body);
    return new ImmutableFlowUnit(this, compilerType, body);
  }
  
  
  @Override
  public ServiceUnit st(ServiceBody body) {
    CompilerType compilerType = createType(st, body);
    return new ImmutableServiceUnit(this, compilerType, body);
  }

  @Override
  public ImmutableDecisionTableUnit dt(DecisionTableBody body) {
    CompilerType compilerType = createType(this.dt, body);
    return new ImmutableDecisionTableUnit(this, compilerType, body);
  }
  

  public CompilerType createType(String srcPkg, BodyNode body) {
    Assertions.notNull(srcPkg, () -> "pkg can't be null!");
    Assertions.notNull(body, () -> "body can't be null!");
    
    String pkg = pkg(srcPkg, body);
    ClassName api = ClassName.get(pkg, JavaSpecUtil.capitalize(body.getId().getValue()));
    ClassName impl = ClassName.get(pkg, body.getId().getValue() + "Gen");
    
    ClassName inputValue = ClassName.get(api.canonicalName(), api.simpleName() + "Accepts");
    ClassName outputValue = ClassName.get(api.canonicalName(), api.simpleName() + "Returns");
    ClassName returnType = ClassName.get(api.canonicalName(), api.simpleName() + "Trace");
    
    return ImmutableCompilerType.builder().pkg(pkg).sourceType(sourceType(body))
        
        .api(ImmutableCompilerEntry.builder()
            .superinterface(createSuperinterface(body, inputValue, returnType))
            .name(api).build())
        
        .impl(ImmutableCompilerEntry.builder().superinterface(api).name(impl).build())
        
        .accepts(ImmutableCompilerEntry.builder().superinterface(ClassName.get(Accepts.class)).name(inputValue).build())
        .returns(ImmutableCompilerEntry.builder().superinterface(ClassName.get(Returns.class)).name(outputValue).build())
        
        .returnType(ImmutableCompilerEntry.builder().superinterface(ClassName.get(TraceEnd.class)).name(returnType).build())
        .build();
  }

  private ParameterizedTypeName createSuperinterface(BodyNode node, ClassName inputValue, ClassName returnType) {
    if(node instanceof ServiceBody) {
      ServiceBody service = (ServiceBody) node;
      if(service.getCommand().getPromise().isPresent()) {
        return ParameterizedTypeName.get(ClassName.get(HdesDefPromise.class), inputValue, returnType);
      }
    }
    return ParameterizedTypeName.get(ClassName.get(HdesDef.class), inputValue, returnType);
  }
  
  private ResourceType sourceType(BodyNode node) {
    if(node instanceof FlowBody) {
      return ResourceType.FL;        
    } else if(node instanceof DecisionTableBody) {
      return ResourceType.DT; 
    } else if(node instanceof ServiceBody) {
      return ResourceType.ST; 
    }
    throw new IllegalArgumentException("sourceType not implemented for: " + node + "!");
  }

  private String pkg(String srcPkg, BodyNode body) {
    if(body instanceof FlowBody) {
      FlowBody node = (FlowBody) body;
      return srcPkg + "." + node.getId().getValue().toLowerCase();      
    } else if(body instanceof DecisionTableBody) {
      return srcPkg;
    } else if(body instanceof ServiceBody) {
      return srcPkg;
    }
    throw new IllegalArgumentException("Pkg not implemented for: " + body + "!");
  }

  public static Config config() {
    return new Config();
  }

  public static class Config {
    private RootNode envir;
    private String root;
    private String flows;
    private String decisionTables;
    private String serviceTasks;

    public Config ast(RootNode envir) {
      this.envir = envir;
      return this;
    }
    
    public Config root(String root) {
      this.root = root;
      return this;
    }

    public Config flows(String flows) {
      this.flows = flows;
      return this;
    }

    public Config decisionTables(String decisionTables) {
      this.decisionTables = decisionTables;
      return this;
    }
    
    public Config serviceTasks(String serviceTasks) {
      this.serviceTasks = serviceTasks;
      return this;
    }

    public ImmutableCompilerNode build() {
      Assertions.notNull(envir, () -> "ast can't be null!");
      
      return new ImmutableCompilerNode(
          envir,
          Optional.ofNullable(root).orElse("io.resys.hdes.compiler"),
          Optional.ofNullable(flows).orElse("fl"),
          Optional.ofNullable(decisionTables).orElse("dt"),
          Optional.ofNullable(serviceTasks).orElse("st"));
    }
  }
}
