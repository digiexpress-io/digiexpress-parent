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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.compiler.spi.spec.JavaSpecUtil;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerEntry;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerType;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;

public class ImmutableFlowUnit implements FlowUnit {
  private final ImmutableCompilerNode parent;
  private final CompilerType compilerType;
  private final FlowBody ctx;

  public ImmutableFlowUnit(ImmutableCompilerNode parent, CompilerType compilerType, FlowBody ctx) {
    super();
    this.parent = parent;
    this.compilerType = compilerType;
    this.ctx = ctx;
  }
  
  public CompilerEntry getAccepts(ObjectDef object) {
    ClassName name = ClassName.get(compilerType.getApi().getName().canonicalName(), "Accepts" + JavaSpecUtil.capitalize(object.getName()));
    TypeName superinterface = compilerType.getReturns().getSuperinterface();
    return ImmutableCompilerEntry.builder().name(name).superinterface(superinterface).build();
  }
  
  @Override
  public CompilerEntry getReturns(ObjectDef object) {
    ClassName name = ClassName.get(compilerType.getApi().getName().canonicalName(), "Returns" + JavaSpecUtil.capitalize(object.getName()));
    TypeName superinterface = compilerType.getReturns().getSuperinterface();
    return ImmutableCompilerEntry.builder().name(name).superinterface(superinterface).build();
  }
  

  @Override
  public CompilerEntry getEndAs(Step step) {
    ClassName name = ClassName.get(compilerType.getApi().getName().canonicalName(), "EndAs" + JavaSpecUtil.capitalize(step.getId().getValue()));
    TypeName superinterface = compilerType.getReturns().getSuperinterface();
    return ImmutableCompilerEntry.builder().name(name).superinterface(superinterface).build();
  }
  
  @Override
  public CompilerType getType() {
    return compilerType;
  }

  @Override
  public Token getToken() {
    return ctx.getToken();
  }
  
  @Override
  public FlowBody getBody() {
    return ctx;
  }
}
