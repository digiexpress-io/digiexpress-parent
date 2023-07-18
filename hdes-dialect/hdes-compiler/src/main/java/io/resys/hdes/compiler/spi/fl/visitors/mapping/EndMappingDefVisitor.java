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

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.IterateAction;
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
import io.resys.hdes.compiler.spi.fl.visitors.FlSpec;
import io.resys.hdes.compiler.spi.fl.visitors.mapping.EndMappingDefVisitor.FlEndMappingSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerEntry;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;

public class EndMappingDefVisitor implements FlowMappingDefVisitor<FlEndMappingSpec, CodeBlock> {
  
  @Value.Immutable
  public interface FlEndMappingSpec extends FlSpec {
    Consumer<CodeBlock.Builder> getValue();
  }
  
  @Override
  public CodeBlock visitBody(EndPointer node, HdesTree ctx) {
    final var unit = ctx.get().node(FlowUnit.class);
    final var next = ctx.next(node).next(node.getMapping());
    
    final CompilerEntry returnType;
    final var insideIteration = ctx.find().ctx(IterateAction.class);
    
    if(insideIteration.isPresent()) {
      returnType = unit.getEndAs(insideIteration.get().get().node(Step.class));
    } else {
      returnType = unit.getType().getReturns();
    }
    
    final var body = CodeBlock.builder()
        .add("\r\n  ")
        .add("$T.builder()", ImmutableSpec.from(returnType.getName()));

    node.getMapping().getValues()
      .forEach(def -> visitMappingDef(def, next).getValue().accept(body));
    
    return body.add(".build()").build();
  }

  @Override
  public FlEndMappingSpec visitFieldMappingDef(FieldMappingDef node, HdesTree ctx) {
    final var body = CodeBlock.builder();
    visitMappingDef(node.getRight(), ctx).getValue().accept(body);
    return ImmutableFlEndMappingSpec.builder()
        .value(code -> code.add(".$L($L)", node.getLeft().getValue(), body.build()))
        .build();
  }
  
  @Override
  public FlEndMappingSpec visitFastMappingDef(FastMappingDef node, HdesTree ctx) {
    final var def = ctx.returns().build(node.getValue()).getReturns();
    final var exp = ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build().getValue();
    return ImmutableFlEndMappingSpec.builder()
        .value(code -> code.add(".$L($L)", def.getName(), exp))
        .build();
  }
  
  @Override
  public FlEndMappingSpec visitExpressionMappingDef(ExpressionMappingDef node, HdesTree ctx) {
    return ImmutableFlEndMappingSpec.builder()
        .value(code -> code
            .add(ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build()
            .getValue()))
        .build();
  }
  
  @Override
  public FlEndMappingSpec visitObjectMappingDef(ObjectMappingDef node, HdesTree ctx) {
    final var next = ctx.next(node);
    return ImmutableFlEndMappingSpec.builder()
        .value(code -> {
          //code.add(codeBlock);
          node.getValues().forEach(d -> visitMappingDef(d, next).getValue().accept(code));
          code.add(".build()");
        })
        .build();
  }

  @Override
  public FlEndMappingSpec visitMappingDef(MappingDef node, HdesTree ctx) {
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
  public CodeBlock visitBody(StepAs def, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }
}
