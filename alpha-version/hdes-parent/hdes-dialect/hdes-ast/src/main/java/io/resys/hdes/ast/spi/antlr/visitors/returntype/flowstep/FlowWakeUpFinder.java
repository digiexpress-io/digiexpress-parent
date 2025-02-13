package io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep;

/*-
 * #%L
 * hdes-ast
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

import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.FlowNode.CallAction;
import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EmptyAction;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.IterateAction;
import io.resys.hdes.ast.api.nodes.FlowNode.IterationEndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.SplitPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAction;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAs;
import io.resys.hdes.ast.api.nodes.FlowNode.StepPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenPointer;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableNestedInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableSimpleInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;
import io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep.FlowWakeUpFinder.FlowWakeUpSpec;

public class FlowWakeUpFinder implements FlowStepVisitor<FlowWakeUpSpec, FlowWakeUpSpec> {

  private final List<String> visited = new ArrayList<>();
  
  @Value.Immutable
  public interface FlowWakeUpSpec {
    List<InvocationNode> getValues();
  }
  
  @Override
  public FlowWakeUpSpec visitBody(Step step, HdesTree ctx) {
    if(visited.contains(step.getId().getValue())) {
      return ImmutableFlowWakeUpSpec.builder().build();
    }
    visited.add(step.getId().getValue());
    final var next = ctx.next(step);
    
    FlowWakeUpSpec action = visitAction(step.getAction(), next);
    return ImmutableFlowWakeUpSpec.builder()
        .addAllValues(action.getValues())
        .addAllValues(visitPointer(step.getPointer(), next).getValues())
        .build(); 
  }

  @Override
  public FlowWakeUpSpec visitPointer(StepPointer pointer, HdesTree ctx) {
    if(pointer instanceof EndPointer) {
      return visitEndPointer((EndPointer) pointer, ctx);
    } else if(pointer instanceof SplitPointer) {
      return visitSplitPointer((SplitPointer) pointer, ctx);
    } else if(pointer instanceof WhenPointer) {
      return visitWhenPointer((WhenPointer) pointer, ctx);
    } else if(pointer instanceof ThenPointer) {
      return visitThenPointer((ThenPointer) pointer, ctx); 
    } else if(pointer instanceof IterationEndPointer) {
      return visitIterationEndPointer((IterationEndPointer) pointer, ctx);
    }
    throw new HdesException(unknownAst(pointer));
  }

  @Override
  public FlowWakeUpSpec visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    final var result = ImmutableFlowWakeUpSpec.builder();
    final var next = ctx.next(pointer);
    for(StepPointer child : pointer.getValues()) {
      final var nested = visitPointer(child, next);
      result.addAllValues(nested.getValues());
    }
    return result.build();
  }

  @Override
  public FlowWakeUpSpec visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    return visitPointer(pointer.getThen(), next);
  }

  @Override
  public FlowWakeUpSpec visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    return visitBody(pointer.getStep(), next);
  }

  @Override
  public FlowWakeUpSpec visitEndPointer(EndPointer pointer, HdesTree ctx) {
    return ImmutableFlowWakeUpSpec.builder().build();
  }
  
  @Override
  public FlowWakeUpSpec visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    return ImmutableFlowWakeUpSpec.builder().build();
  }

  @Override
  public FlowWakeUpSpec visitAction(StepAction action, HdesTree ctx) {
    if(action instanceof IterateAction) {
      return visitIterateAction((IterateAction) action, ctx);
    } else if(action instanceof CallAction) {
      return visitCallAction((CallAction) action, ctx);
    } else if(action instanceof EmptyAction) {
      return ImmutableFlowWakeUpSpec.builder().build(); 
    }
    throw new HdesException(unknownAst(action));
  }

  @Override
  public FlowWakeUpSpec visitIterateAction(IterateAction action, HdesTree ctx) {
    if(action.getStep().isEmpty()) {
      return ImmutableFlowWakeUpSpec.builder().build();
    }
    final var builder = ImmutableFlowWakeUpSpec.builder();
    final var next = ctx.next(action);
    final var step = ctx.get().node(Step.class);
    for(var invocation : visitBody(action.getStep().get(), next).getValues()) {
      builder.addValues(ImmutableNestedInvocation.builder().token(step.getToken()).path(step.getId()).value(invocation).build());
    }
    return builder.build();
  }
  
  @Override
  public FlowWakeUpSpec visitCallAction(CallAction action, HdesTree ctx) {
    final var builder = ImmutableFlowWakeUpSpec.builder();
    final var next = ctx.next(action);
    for(var callDef : action.getCalls()) {
      builder.addAllValues(visitCallDef(callDef, next).getValues());
    }
    return builder.build();
  }

  @Override
  public FlowWakeUpSpec visitCallDef(CallDef def, HdesTree ctx) {
    final var step = ctx.get().node(Step.class);
    if(def.getAwait()) {
      return ImmutableFlowWakeUpSpec.builder()
          .addValues(ImmutableSimpleInvocation.builder().token(def.getToken()).value(step.getId().getValue()).build())
          .build();
    }
    
    BodyNode dependency = ctx.getRoot().getBody(def.getId());
    if(!(dependency instanceof FlowBody)) {
      return ImmutableFlowWakeUpSpec.builder().build();      
    }

    final FlowBody flow = (FlowBody) dependency;
    final var next = ctx.next(flow);
    final var builder = ImmutableFlowWakeUpSpec.builder();
    
    for(var child : next.step().getWakeUps(flow.getStep())) {
      builder.addValues(ImmutableNestedInvocation.builder().token(step.getToken()).path(step.getId()).value(child).build());
    }
    
    return builder.build();
  }
  
  private String unknownAst(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown AST: ").append(ast.getClass())
        .append("  - ").append(ast).append(System.lineSeparator())
        .toString();
  }

  @Override
  public FlowWakeUpSpec visitStepAs(StepAs stepAs, HdesTree ctx) {
    throw new HdesException(unknownAst(stepAs));
  }
}
