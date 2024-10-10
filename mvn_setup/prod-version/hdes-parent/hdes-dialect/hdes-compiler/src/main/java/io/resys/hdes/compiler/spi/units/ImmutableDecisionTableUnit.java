package io.resys.hdes.compiler.spi.units;

import java.util.List;

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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMapping;
import io.resys.hdes.compiler.spi.spec.JavaSpecUtil;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerEntry;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerType;
import io.resys.hdes.compiler.spi.units.CompilerNode.DecisionTableUnit;

public class ImmutableDecisionTableUnit implements DecisionTableUnit {
  private final ImmutableCompilerNode parent;
  private final CompilerType compilerType;
  private final CompilerEntry listValue;
  private final DecisionTableBody ctx;
  private final TypeName constantsType;

  public ImmutableDecisionTableUnit(ImmutableCompilerNode parent, CompilerType compilerType, DecisionTableBody ctx) {
    super();
    this.parent = parent;
    this.compilerType = compilerType;
    this.ctx = ctx;
    
    if(ctx.getHitPolicy() instanceof HitPolicyAll) {
      listValue = ImmutableCompilerEntry.builder()
        .name(ClassName.get(compilerType.getReturns().getName().packageName(), compilerType.getApi().getName().simpleName() + "Value"))
        .superinterface(compilerType.getReturns().getSuperinterface())
        .build();
    } else {
      listValue = compilerType.getReturns();
    }
    
    if (ctx.getHitPolicy() instanceof HitPolicyMapping) {
      HitPolicyMapping matrix = (HitPolicyMapping) ctx.getHitPolicy();
      Class<?> type = JavaSpecUtil.type(matrix.getDefTo());
      constantsType = ParameterizedTypeName.get(List.class, type);
    } else {
      constantsType = listValue.getName();
    }
  }
  
  @Override
  public CompilerEntry getConstants() {
    ClassName api = compilerType.getApi().getName();
    return ImmutableCompilerEntry.builder()
        .name(ClassName.get(api.canonicalName(), ctx.getId().getValue() + "Const"))
        .superinterface(constantsType)
        .build();
  }

  @Override
  public CompilerType getType() {
    return compilerType;
  }

  @Override
  public DecisionTableBody getBody() {
    return ctx;
  }

  @Override
  public Token getToken() {
    return ctx.getToken();
  }

  @Override
  public CompilerEntry getListValue() {
    return listValue;
  }
}

