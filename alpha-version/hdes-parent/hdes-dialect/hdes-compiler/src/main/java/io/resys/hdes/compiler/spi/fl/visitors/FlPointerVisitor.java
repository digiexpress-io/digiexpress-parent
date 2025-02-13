package io.resys.hdes.compiler.spi.fl.visitors;

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
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.IterateAction;
import io.resys.hdes.ast.api.nodes.FlowNode.IterationEndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.SplitPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.FlowNode.StepPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenPointer;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowPointerVisitor;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory;
import io.resys.hdes.compiler.spi.fl.visitors.mapping.FlowMappingFactory;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.spi.beans.ImmutableMatchedCondition;
import io.resys.hdes.executor.spi.beans.ImmutableTrace;

public class FlPointerVisitor implements FlowPointerVisitor<FlSpec, FlSpec> {
  
  @Value.Immutable
  public interface FlPointerSpec extends FlSpec {
    Consumer<CodeBlock.Builder> getValue();
  }

  @Override
  public FlPointerSpec visitPointer(StepPointer pointer, HdesTree ctx) {
    if(pointer instanceof EndPointer) {
      return visitEndPointer((EndPointer) pointer, ctx);
    } else if(pointer instanceof SplitPointer) {
      return visitSplitPointer((SplitPointer) pointer, ctx);
    } else if(pointer instanceof WhenPointer) {
      return visitWhenPointer((WhenPointer) pointer, ctx);
    } else if(pointer instanceof ThenPointer) {
      return visitThenPointer((ThenPointer) pointer, ctx); 
    }
    throw new IllegalArgumentException("not implemented");
  }
  
  @Override
  public FlPointerSpec visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    final var nested = pointer.getValues().stream()
        .map(p -> visitPointer(p, next))
        .collect(Collectors.toList());

    final var code = CodeBlock.builder();
    int index = -1;
    for(StepPointer step : pointer.getValues()) {
      final var builder = CodeBlock.builder();
      nested.get(++index).getValue().accept(builder);
      final var controlCode = builder.build();
      
      if(controlCode.isEmpty()) {
        continue;
      }
      
      if(index > 0) {
        code.add("else ");
      }
      
      if(step instanceof WhenPointer) {
        code.add(controlCode);
      } else {
        code.add("{").add(controlCode).add("}");
      }
    }
    return ImmutableFlPointerSpec.builder()
        .value(c -> c.add(code.build())).build();
  }

  @Override
  public FlPointerSpec visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    final var parentStep = ctx.get().node(Step.class);
    final var next = ctx.next(pointer);
    final var condition = ExpressionFactory.builder().body(pointer.getWhen()).tree(next).build().getValue();
    final var conclusion = visitPointer(pointer.getThen(), next);
    return ImmutableFlPointerSpec.builder()
        .value(code -> {
          ThenPointer then = (ThenPointer) pointer.getThen();
          
          final var traceBody = CodeBlock.builder()
              .add("$T.builder().id($S).src($S).build()", 
                  ImmutableMatchedCondition.class, then.getId().getValue(), pointer.getWhen().getSrc()).build();
          
          final var parent = CodeBlock.builder()
              .add("$T.builder().id($S).parent(parent).body($L).step()",
                  ImmutableTrace.class, parentStep.getId().getValue(), traceBody).build();
          
          code.beginControlFlow("if($L)", condition);
          code.addStatement("parent = $L", parent);
          conclusion.getValue().accept(code);
          code.endControlFlow();
        })
        .build();
  }

  @Override
  public FlPointerSpec visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    return ImmutableFlPointerSpec.builder()
        .value(code -> code.addStatement("return $L(parent)", visitMethodName(pointer.getStep())))
        .build();
  }

  @Override
  public FlPointerSpec visitEndPointer(EndPointer pointer, HdesTree ctx) {
    final var unit = ctx.get().node(FlowUnit.class);
    final var returnType = FlowMappingFactory.from(pointer, ctx);
    final var trace = CodeBlock.builder();
    
    final var insideIteration = ctx.find().ctx(IterateAction.class);
    if(insideIteration.isPresent()) {
      final var stepId = ctx.get().node(Step.class).getId().getValue();
      trace.addStatement(
          "return $T.builder().id($S).parent(parent).body($L).end()",
          ImmutableTrace.class, stepId, returnType);
    } else {
      final var flowId = ctx.get().node(FlowBody.class).getId().getValue();
      trace.addStatement(
          "return $T.builder().id($S).time(System.currentTimeMillis()).parent(parent).body($L).build()",
          ImmutableSpec.from(unit.getType().getReturnType().getName()), flowId, returnType);
    }
    
    return ImmutableFlPointerSpec.builder()
        .value(code -> code.add(trace.build()))
        .build();
  }

  public static String visitMethodName(Step step) {
    String name = step.getId().getValue();
    return new StringBuilder()
        .append("visit")
        .append(name.substring(0, 1).toUpperCase())
        .append(name.length() == 1 ? "" : name.substring(1))
        .toString();
  }

  @Override
  public FlPointerSpec visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    return ImmutableFlPointerSpec.builder()
        .value(code -> code.addStatement("return ($T) parent", TraceEnd.class))
        .build();
  }
}
