package io.resys.hdes.ast.spi.returntypes;

import java.util.ArrayList;
import java.util.Collections;

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

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
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
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.ast.api.nodes.ImmutableStepCallDef;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;



public class ReturnTypeFlStepDefVisitor implements FlowStepVisitor<List<TypeDef>, ObjectDef> {

  public interface FlowStepDefVisitorSpec {}
  
  @Value.Immutable
  public interface MultipleFlowStepDefVisitorSpec extends FlowStepDefVisitorSpec {
    List<TypeDef> getValues();
  }
  
  @Value.Immutable
  public interface SingleFlowStepDefVisitorSpec extends FlowStepDefVisitorSpec {
    Optional<TypeDef> getValue();
  }
  
  @Override
  public ObjectDef visitBody(Step step, HdesTree ctx) {
    final var next = ctx.next(step);
    final var action = visitAction(step.getAction(), next);
    
    return ImmutableObjectDef.builder()
      .token(step.getToken())
      .name(step.getId().getValue())
      .required(true)
      .array(false)
      .context(ContextTypeDef.STEP_RETURNS)
      .addAllValues(action)
      .build();
  }

  @Override
  public List<TypeDef> visitAction(StepAction action, HdesTree ctx) {
    if(action instanceof CallAction) {
      return visitCallAction((CallAction) action, ctx);
    } else if(action instanceof IterateAction) {
      return visitIterateAction((IterateAction) action, ctx);
    }
    return Collections.emptyList(); 
  }

  @Override
  public List<TypeDef> visitCallAction(CallAction action, HdesTree ctx) {
    final var next = ctx.next(action);
    final List<TypeDef> result = new ArrayList<>();
    
    if (action.getCalls().size() > 1) {
      int index = 0;
      for (var call : action.getCalls()) {
        result.add(ImmutableStepCallDef.builder().name("_" + index).index(index).callDef(call)
            .token(call.getToken()).required(false).array(false).context(ContextTypeDef.STEP_CALL)
            .values(visitCallDef(call, next)).build());
        index++;
      }
    } else if (action.getCalls().size() == 1) {
      CallDef call = action.getCalls().get(0);
      result.add(
          ImmutableStepCallDef.builder().token(call.getToken()).name("_").index(0).callDef(call).required(false)
              .array(false).context(ContextTypeDef.STEP_CALL).values(visitCallDef(call, next)).build());
    }
    
    return result;
  }

  @Override
  public List<TypeDef> visitCallDef(CallDef def, HdesTree ctx) {
    final var dependency = ctx.getRoot().getBody(def.getId().getValue());
    final List<TypeDef> result = new ArrayList<>();
    if(dependency instanceof DecisionTableBody) {
      DecisionTableBody dt = (DecisionTableBody) dependency;
      if(dt.getHitPolicy() instanceof HitPolicyAll) {
        result.add(ImmutableObjectDef.builder()
            .name("_")
            .array(true)
            .values(dependency.getHeaders().getReturnDefs())
            .build());
      }
      result.addAll(dependency.getHeaders().getReturnDefs());      
    } else {
      result.addAll(dependency.getHeaders().getReturnDefs());
    }
    return result;
  }

  @Override
  public List<TypeDef> visitIterateAction(IterateAction action, HdesTree ctx) {
    TypeDef iterator = ctx.returns().build(action.getOver()).getReturns();
    
    if(!iterator.getArray()) {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId(ctx.get().body().getId().getValue())
          .target(action.getOver())
          .message("Type def: '" + iterator.getName() + "' is NOT ARRAY, can't iterate over non array!")
          .build());
    }

    // Add iterator 
    final List<TypeDef> result = new ArrayList<>();
    if(iterator instanceof ObjectDef) {
      result.add(ImmutableObjectDef.builder().from(iterator).context(ContextTypeDef.STEP_ITERATOR).array(false).name("_").build());
    } else if(iterator instanceof ScalarDef) {
      result.add(ImmutableScalarDef.builder().from(iterator).context(ContextTypeDef.STEP_ITERATOR).array(false).name("_").build());
    }
    
    // Add how the iteration ends
    Optional<ObjectDef> endAs = ctx.step().findEnd(action.getStep());
    endAs.ifPresent(e -> result.add(e));
    
    return result;
  }

  @Override
  public List<TypeDef> visitPointer(StepPointer pointer, HdesTree ctx) {
    throw new HdesException(unknownAst(pointer));
  }

  @Override
  public List<TypeDef> visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    throw new HdesException(unknownAst(pointer));
  }

  @Override
  public List<TypeDef> visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    throw new HdesException(unknownAst(pointer));
  }

  @Override
  public List<TypeDef> visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    throw new HdesException(unknownAst(pointer));
  }

  @Override
  public List<TypeDef> visitEndPointer(EndPointer pointer, HdesTree ctx) {
    throw new HdesException(unknownAst(pointer));
  }
  
  @Override
  public List<TypeDef> visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    throw new HdesException(unknownAst(pointer));
  }
  
  @Override
  public List<TypeDef> visitStepAs(StepAs stepAs, HdesTree ctx) {
    throw new HdesException(unknownAst(stepAs));
  }
  
  private String unknownAst(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown AST: ").append(ast.getClass())
        .append("  - ").append(ast).append(System.lineSeparator())
        .toString();
  }
}