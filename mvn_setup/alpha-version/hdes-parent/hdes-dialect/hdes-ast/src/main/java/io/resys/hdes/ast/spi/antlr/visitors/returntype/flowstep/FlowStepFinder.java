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
import java.util.Optional;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.FlowNode.CallAction;
import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
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
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;

public class FlowStepFinder implements FlowStepVisitor<Optional<Step>, Optional<Step>> {

  private final String target;
  private final List<String> visited = new ArrayList<>();
  
  public FlowStepFinder(String target) {
    super();
    this.target = target;
  }
  
  @Override
  public Optional<Step> visitBody(Step step, HdesTree ctx) {
    if(visited.contains(step.getId().getValue())) {
      return Optional.empty();
    }
    visited.add(step.getId().getValue());
    
    if(step.getId().getValue().equals(target)) {
      return Optional.of(step);
    }
    final var next = ctx.next(step);
    return visitPointer(step.getPointer(), next);
  }

  @Override
  public Optional<Step> visitPointer(StepPointer pointer, HdesTree ctx) {
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
  public Optional<Step> visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    return Optional.empty();
  }
  
  @Override
  public Optional<Step> visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    for(StepPointer child : pointer.getValues()) {
      final var result = visitPointer(child, next);
      if(result.isPresent()) {
        return result;
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<Step> visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    return visitPointer(pointer.getThen(), next);
  }

  @Override
  public Optional<Step> visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    return visitBody(pointer.getStep(), next);
  }

  @Override
  public Optional<Step> visitEndPointer(EndPointer pointer, HdesTree ctx) {
    return Optional.empty();
  }
  
  private String unknownAst(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown AST: ").append(ast.getClass())
        .append("  - ").append(ast).append(System.lineSeparator())
        .toString();
  }

  @Override
  public Optional<Step> visitAction(StepAction action, HdesTree ctx) {
    if(action instanceof IterateAction) {
      return visitIterateAction((IterateAction) action, ctx);
    }
    throw new HdesException(unknownAst(action));
  }

  @Override
  public Optional<Step> visitIterateAction(IterateAction action, HdesTree ctx) {
    if(action.getStep().isEmpty()) {
      return Optional.empty();
    }
    final var next = ctx.next(action);
    return visitBody(action.getStep().get(), next);
  }
  @Override
  public Optional<Step> visitCallAction(CallAction action, HdesTree ctx) {
    throw new HdesException(unknownAst(action));
  }

  @Override
  public Optional<Step> visitCallDef(CallDef def, HdesTree ctx) {
    throw new HdesException(unknownAst(def));
  }

  @Override
  public Optional<Step> visitStepAs(StepAs stepAs, HdesTree ctx) {
    throw new HdesException(unknownAst(stepAs));
  }
}
