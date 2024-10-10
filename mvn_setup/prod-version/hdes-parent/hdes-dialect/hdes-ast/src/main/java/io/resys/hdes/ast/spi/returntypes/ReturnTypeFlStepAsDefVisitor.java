package io.resys.hdes.ast.spi.returntypes;

import java.util.ArrayList;

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

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
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
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;
import io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep.FlowStepMappingDef;



public class ReturnTypeFlStepAsDefVisitor implements FlowStepVisitor<List<TypeDef>, Optional<ObjectDef>> {

  private final FlowStepMappingDef mappingDef = new FlowStepMappingDef();
  
  
  @Override
  public Optional<ObjectDef> visitBody(Step step, HdesTree ctx) {
    if(step.getAs().isEmpty()) {
      return Optional.empty();
    }
    
    final var next = ctx.next(step);
    final var stepAs = visitStepAs(step.getAs().get(), next);
    
    return Optional.of(ImmutableObjectDef.builder().name("_")
        .token(step.getAs().get().getToken()).required(false).array(false)
        .context(ContextTypeDef.STEP_AS)
        .addAllValues(stepAs)
        .build());
  }

  @Override
  public List<TypeDef> visitStepAs(StepAs stepAs, HdesTree ctx) {
    final var next = ctx.next(stepAs);    
    final List<TypeDef> result = new ArrayList<>();
    
    for(var def : stepAs.getMapping().getValues()) {
      result.add(mappingDef.visitMappingDef(def, next));
    }
    
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
  public List<TypeDef> visitAction(StepAction action, HdesTree ctx) {
    throw new HdesException(unknownAst(action));
  }
  @Override
  public List<TypeDef> visitCallAction(CallAction action, HdesTree ctx) {
    throw new HdesException(unknownAst(action));
  }
  @Override
  public List<TypeDef> visitCallDef(CallDef def, HdesTree ctx) {
    throw new HdesException(unknownAst(def));
  }
  @Override
  public List<TypeDef> visitIterateAction(IterateAction action, HdesTree ctx) {
    throw new HdesException(unknownAst(action));
  }
  private String unknownAst(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown AST: ").append(ast.getClass())
        .append("  - ").append(ast).append(System.lineSeparator())
        .toString();
  }
}
