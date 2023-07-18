package io.resys.hdes.compiler.spi.st.mapping;

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

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.nodes.ServiceNode.CommandInvocation;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServicePromise;
import io.resys.hdes.ast.api.visitors.ServiceBodyVisitor;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.spec.JavaSpecUtil;
import io.resys.hdes.compiler.spi.st.mapping.ServiceMappingFactory.StMappingSpec;
import io.resys.hdes.compiler.spi.st.visitors.StSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.ServiceUnit;

public class ServiceReturnsMapping implements ServiceBodyVisitor<StSpec, CodeBlock> {

  @Override
  public CodeBlock visitBody(ServiceTree ctx) {
    final var unit = ctx.get().node(ServiceUnit.class);
    final var body = CodeBlock.builder().add("$T.builder()", ImmutableSpec.from(unit.getType().getReturns().getName()));
    
    visitHeaders(ctx.getValue().getHeaders(), ctx.next(ctx.getValue())).getValue().accept(body);;
    
    return body.add(".build()").build();
  }

  @Override
  public StMappingSpec visitHeaders(Headers node, HdesTree ctx) {
    final var next = ctx.next(node);
    return ImmutableStMappingSpec.builder()
        .value(b -> node.getReturnDefs().forEach(h -> visitHeader(h, next).getValue().accept(b)))
        .build();
  }

  @Override
  public StMappingSpec visitHeader(TypeDef node, HdesTree ctx) {
    if(node instanceof ScalarDef) {
      return visitHeader((ScalarDef) node, ctx);
    }
    return visitHeader((ObjectDef) node, ctx);
  }

  @Override
  public StMappingSpec visitHeader(ScalarDef node, HdesTree ctx) {
    return ImmutableStMappingSpec.builder()
        .value(code -> code.add(".$L(invocation.$L())", node.getName(), JavaSpecUtil.getMethodName(node.getName())))
        .build();
  }
  

  @Override
  public StMappingSpec visitHeader(ObjectDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StSpec visitCommandInvocation(CommandInvocation command, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StSpec visitClassName(InvocationNode invocation, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StSpec visitMapping(ObjectMappingDef mapping, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StSpec visitPromise(ServicePromise promise, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

}
