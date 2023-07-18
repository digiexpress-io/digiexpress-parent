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
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
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
import io.resys.hdes.compiler.spi.fl.visitors.mapping.FlowCallDefMappingDefVisitor.FlowMappingSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;

public class FlowCallDefMappingDefVisitor implements FlowMappingDefVisitor<FlowMappingSpec, CodeBlock> {

  @Value.Immutable
  public interface FlowMappingSpec extends FlSpec {
    Consumer<CodeBlock.Builder> getValue();
  }
  
  @Override
  public CodeBlock visitBody(CallDef def, HdesTree ctx) {
    CompilerNode compilerNode = ctx.get().node(CompilerNode.class);
    String dependencyId = def.getId().getValue();
    FlowBody body = (FlowBody) ctx.getRoot().getBody(dependencyId);
    FlowUnit unit = compilerNode.fl(body);
    
    final var mapping = CodeBlock.builder();
    visitObjectMappingDef(def.getMapping(), ctx.next(def).next(unit)).getValue().accept(mapping);
        
    final var call = def.getIndex().map(index -> "call" + index).orElse("call");
    final var impl = unit.getType().getImpl().getName();
    final var result = CodeBlock.builder()
        .addStatement("final var $L = new $T().apply($L)", call, impl, mapping.build());
    
    return result.build();
  }
  
  @Override
  public FlowMappingSpec visitMappingDef(MappingDef node, HdesTree ctx) {
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
  public FlowMappingSpec visitObjectMappingDef(ObjectMappingDef node, HdesTree ctx) {
    final var next = ctx.next(node);
    return ImmutableFlowMappingSpec.builder()
        .value(c -> {
          FlowUnit unit = ctx.get().node(FlowUnit.class);
          c.add("$T.builder()", ImmutableSpec.from(unit.getType().getAccepts().getName()));
          node.getValues().forEach(v -> visitMappingDef(v, next).getValue().accept(c));
          c.add(".build()");
        })
        .build();
  }

  @Override
  public FlowMappingSpec visitFieldMappingDef(FieldMappingDef node, HdesTree ctx) {
    return ImmutableFlowMappingSpec.builder()
        .value(code -> {
          final var body = CodeBlock.builder();
          visitMappingDef(node.getRight(), ctx).getValue().accept(body);
          code.add(".$L($L)", node.getLeft().getValue(), body.build());
        })
        .build();
  }
  
  @Override
  public FlowMappingSpec visitFastMappingDef(FastMappingDef node, HdesTree ctx) {
    final var def = ctx.returns().build(node.getValue()).getReturns();
    final var exp = ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build().getValue();
    return ImmutableFlowMappingSpec.builder()
        .value(code -> code.add(".$L($L)", def.getName(), exp))
        .build();
  }
  
  @Override
  public FlowMappingSpec visitExpressionMappingDef(ExpressionMappingDef node, HdesTree ctx) {
    return ImmutableFlowMappingSpec.builder()
        .value(code -> code
            .add(ExpressionFactory.builder().body(node.getValue()).tree(ctx.next(node)).build()
            .getValue()))
        .build();
  }

  @Override
  public CodeBlock visitBody(EndPointer node, HdesTree ctx) {
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
