package io.resys.hdes.compiler.spi.fl.visitors.mapping;

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

import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAs;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.MappingNode.ExpressionMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FastMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FieldMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.MappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowMappingDefVisitor;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.MappingEvent;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;

public class StepAsMappingDefVisitor implements FlowMappingDefVisitor<CodeBlock, CodeBlock> {
  
  @Override
  public CodeBlock visitBody(StepAs node, HdesTree ctx) {
    final var unit = ctx.get().node(FlowUnit.class);
    final var next = ctx.next(node).next(node.getMapping());
    final var returnType = unit.getEndAs(ctx.get().node(Step.class));
    
    final var body = CodeBlock.builder()
        .add("\r\n  ")
        .add("$T.builder()", ImmutableSpec.from(returnType.getName()));

    node.getMapping().getValues().forEach(def -> body.add(visitMappingDef(def, next)));
    
    return body.add(".build()").build();
  }

  @Override
  public CodeBlock visitFieldMappingDef(FieldMappingDef node, HdesTree ctx) {
    return CodeBlock.builder()
        .add(".$L($L)", node.getLeft().getValue(), visitMappingDef(node.getRight(), ctx))
        .build();
  }
  
  @Override
  public CodeBlock visitFastMappingDef(FastMappingDef node, HdesTree ctx) {
    final var def = ctx.returns().build(node.getValue()).getReturns();
    final var exp = ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build().getValue();
    return CodeBlock.builder().add(".$L($L)", def.getName(), exp).build();
  }
  
  @Override
  public CodeBlock visitExpressionMappingDef(ExpressionMappingDef node, HdesTree ctx) {
    return ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build().getValue();
  }
  
  @Override
  public CodeBlock visitObjectMappingDef(ObjectMappingDef node, HdesTree ctx) {
    final var next = ctx.next(node);
    final var body = CodeBlock.builder();
    node.getValues().forEach(d -> body.add(visitMappingDef(d, next)));
    return body.add(".build()").build();
  }

  @Override
  public CodeBlock visitMappingDef(MappingDef node, HdesTree ctx) {
    if(node instanceof ExpressionMappingDef) {
      return visitExpressionMappingDef((ExpressionMappingDef) node, ctx);
    } else if(node instanceof FastMappingDef) {
      return visitFastMappingDef((FastMappingDef) node, ctx);
    } else if(node instanceof FieldMappingDef) {
      return visitFieldMappingDef((FieldMappingDef) node, ctx);
    } else if(node instanceof ObjectMappingDef) {
      return visitObjectMappingDef((ObjectMappingDef) node, ctx);
    }
    throw new IllegalArgumentException("not implemented"); 
  }

  @Override
  public CodeBlock visitBody(CallDef node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public CodeBlock visitBody(CallDef def, MappingEvent event, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public CodeBlock visitBody(EndPointer node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }
}
