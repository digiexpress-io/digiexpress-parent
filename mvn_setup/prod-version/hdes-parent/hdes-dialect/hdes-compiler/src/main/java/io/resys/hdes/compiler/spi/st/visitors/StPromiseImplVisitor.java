package io.resys.hdes.compiler.spi.st.visitors;

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

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.nodes.ServiceNode.CommandInvocation;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServicePromise;
import io.resys.hdes.ast.api.visitors.ServiceBodyVisitor;
import io.resys.hdes.compiler.spi.spec.HdesDefPromiseSpec;
import io.resys.hdes.compiler.spi.spec.HdesDefSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.st.mapping.ServiceMappingFactory;
import io.resys.hdes.compiler.spi.units.CompilerNode.ServiceUnit;
import io.resys.hdes.executor.api.ImmutablePromiseDataId;
import io.resys.hdes.executor.spi.beans.ImmutableTrace;

public class StPromiseImplVisitor implements ServiceBodyVisitor<StSpec, TypeSpec> {
  
  @Value.Immutable
  public interface StPromiseExecSpec extends StSpec {
    Consumer<HdesDefPromiseSpec.ImplBuilder> getExecution();
    Consumer<CodeBlock.Builder> getValue();
  }

  @Override
  public TypeSpec visitBody(ServiceTree ctx) {
    final var unit = ctx.get().node(ServiceUnit.class);
    final var impl = HdesDefPromiseSpec.impl(unit.getType());

    final var command = unit.getBody().getCommand();
    final var canonicalName = ClassName.bestGuess(visitInvocation(command.getClassName()));
    final var mapping = createMapping(ctx);
    
    final var onEnter = CodeBlock.builder()
      .addStatement("final var parent = $T.builder().id($S).body($L).build()", 
          ImmutableTrace.class, "input", HdesDefSpec.ACCESS_INPUT_VALUE)
      
      .addStatement("final var dataId = new $T().onEnter(input, $L)", canonicalName, mapping)
      .addStatement("final var returns = $T.builder().dataId(dataId).build()", ImmutablePromiseDataId.class)
      .addStatement("return $T.builder().id($S).parent(parent).body(returns).promise()", 
          ImmutableTrace.class, ctx.getValue().getId().getValue());

    final var onComplete = CodeBlock.builder()
        .addStatement("final var parent = $T.builder().id($S).body($L).build()", 
            ImmutableTrace.class, "input", HdesDefSpec.ACCESS_INPUT_VALUE)
        .addStatement("final var invocation = new $T().onComplete(dataId, data, input, $L)", canonicalName, mapping)
        .addStatement("final var returns = $L", ServiceMappingFactory.returns(ctx))
        .addStatement("return $T.builder().id($S).time(System.currentTimeMillis()).parent(parent).body(returns).build()", 
            ImmutableSpec.from(unit.getType().getReturnType().getName()), ctx.getValue().getId().getValue());
    
    return impl
        .onEnter(onEnter.build())
        .onComplete(onComplete.build())
        .build().build();
  }
  
  private CodeBlock createMapping(ServiceTree ctx) {
    final var command = ctx.getValue().getCommand();
    final var mapping = CodeBlock.builder();
    visitMapping(command.getMapping(), ctx).getValue().accept(mapping);
    return mapping.build();
  }
  
  
  @Override
  public StPromiseExecSpec visitCommandInvocation(CommandInvocation command, HdesTree ctx) {
    final var canonicalName = visitInvocation(command.getClassName());
    final var mapping = CodeBlock.builder();
    visitMapping(command.getMapping(), ctx).getValue().accept(mapping);
    
    return ImmutableStPromiseExecSpec.builder()
        .execution(e -> {})
        .value(b -> {
          b.addStatement("final var invocation = new $T().accept(input, $L)", ClassName.bestGuess(canonicalName), mapping.build());
        })
        .build();
  }
  
  @Override
  public StPromiseExecSpec visitClassName(InvocationNode invocation, HdesTree ctx) {
    return ImmutableStPromiseExecSpec.builder()
        .execution(e -> {})
        .value(b -> {})
        .build();
  }
  
  private String visitInvocation(InvocationNode invocation) {
    if(invocation instanceof SimpleInvocation) {
      SimpleInvocation simple = (SimpleInvocation) invocation;
      return simple.getValue();
    } else if(invocation instanceof NestedInvocation) {
      NestedInvocation nested = (NestedInvocation) invocation;
      String path = visitInvocation(nested.getPath());
      String value = visitInvocation(nested.getValue());
      return path + "." + value;
    }
    throw new IllegalArgumentException("Not supported invocation: " + invocation + "!"); 
  }

  @Override
  public StPromiseExecSpec visitMapping(ObjectMappingDef mapping, HdesTree ctx) {
    return ImmutableStPromiseExecSpec.builder()
        .execution(e -> {})
        .value(b -> b.add(ServiceMappingFactory.accepts(mapping, ctx)))
        .build();
  }
  
  @Override
  public StPromiseExecSpec visitHeaders(Headers node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StPromiseExecSpec visitHeader(TypeDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StPromiseExecSpec visitHeader(ScalarDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StPromiseExecSpec visitHeader(ObjectDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StSpec visitPromise(ServicePromise promise, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }
}
