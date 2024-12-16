package io.resys.hdes.client.spi.flow;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowInputNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowSwitchNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowTaskNode;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.exceptions.FlowAstException;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStep;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepBody;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepConditionalThenPointer;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepEndPointer;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepPointer;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepPointerType;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepRefType;
import io.resys.hdes.client.api.programs.ImmutableFlowProgram;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStep;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepBody;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepConditionalThenPointer;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepEndPointer;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepThenPointer;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepWhenThenPointer;
import io.resys.hdes.client.spi.flow.ast.AstFlowNodesFactory;

public class FlowProgramBuilder {
  private static final FlowProgramStepEndPointer END_STEP_POINTER = ImmutableFlowProgramStepEndPointer.builder().type(FlowProgramStepPointerType.END).build();
  private static final FlowProgramStep END_STEP = ImmutableFlowProgramStep.builder()
      .id("end")
      .pointer(END_STEP_POINTER)
      .build();
  public static final String OBJECT_INPUT_FLAG = "OBJECT_INPUT";

  private final HdesTypesMapper typesFactory;
  private final Map<String, FlowProgramStep> steps = new HashMap<>();
  private final Map<String, AstFlowTaskNode> tasksById = new HashMap<>();
  
  public FlowProgramBuilder(HdesTypesMapper typesFactory) {
    super();
    this.typesFactory = typesFactory;
  }

  public FlowProgram build(AstFlow ast) {
    final var firstTask = visitTasksById(ast);
    final var firstStep = firstTask == null ? END_STEP: visitTask(firstTask);
    return ImmutableFlowProgram.builder()
        .startStepId(firstStep.getId())
        .steps(steps)
        .acceptDefs(visitAcceptDefs(ast))
        .build();
  }
  

  private AstFlowTaskNode visitTasksById(AstFlow ast) {
    AstFlowTaskNode firstTask = null;
    final var data = ast.getSrc();
    for(final var task : data.getTasks().values()) {
      tasksById.put(AstFlowNodesFactory.getStringValue(task.getId()), task);
      if(task.getOrder() == 0) {
        firstTask = task;
      }
    }
    return firstTask;
  }

  private FlowProgramStep visitTask(AstFlowTaskNode task) {
    String taskId = AstFlowNodesFactory.getStringValue(task.getId());
    if(steps.containsKey(taskId)) {
      return steps.get(taskId);
    }
    steps.put(taskId, null);
    
    final var body = visitStepBody(task);
    final var pointer = visitStepPointer(task);
    
    final var step = ImmutableFlowProgramStep.builder()
        .id(taskId)
        .body(body)
        .pointer(pointer)
        .build();
    
    steps.put(step.getId(), step);
    return step;
  }

  public FlowProgramStepBody visitStepBody(AstFlowTaskNode task) {
    if(task.getDecisionTable() == null && task.getService() == null) {
      return null;
    }

    final var collection = AstFlowNodesFactory.getBooleanValue(task.getRef().getCollection());
    final var ref = AstFlowNodesFactory.getStringValue(task.getRef().getRef());
    final var inputs = new HashMap<String, String>();
    if (task.getRef().getObjectInput() != null) {
      inputs.put(OBJECT_INPUT_FLAG, task.getRef().getObjectInput());
    } else {
      for (Map.Entry<String, AstFlowNode> entry : task.getRef().getInputs().entrySet()) {
        inputs.put(entry.getKey(), AstFlowNodesFactory.getStringValue(entry.getValue()));
      }
    }
    
    final var refType = task.getDecisionTable() != null ? FlowProgramStepRefType.DT : FlowProgramStepRefType.SERVICE;
    return ImmutableFlowProgramStepBody.builder()
        .ref(ref).refType(refType)
        .collection(collection)
        .inputMapping(inputs)
        .build();
  }
  

  private FlowProgramStepPointer visitStepPointer(AstFlowTaskNode task) {
    if(!task.getSwitch().isEmpty()) {
      final var pointer = ImmutableFlowProgramStepWhenThenPointer.builder().type(FlowProgramStepPointerType.SWITCH);
      final var decisions = new ArrayList<AstFlowSwitchNode>(task.getSwitch().values());
      Collections.sort(decisions, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
      decisions.forEach(d -> {
        
        final var condition = visitSwitchNode(d);
        if(!condition.getStepId().equals(END_STEP.getId())) {
          visitTask(tasksById.get(condition.getStepId()));
        }
        pointer.addConditions(condition);
        
        
      });
      return pointer.build();
    }
    
    final var thenId = AstFlowNodesFactory.getStringValue(task.getThen());
    if(thenId != null && !thenId.equals(END_STEP.getId())) {
      visitTask(tasksById.get(thenId));
      return ImmutableFlowProgramStepThenPointer.builder()
          .type(FlowProgramStepPointerType.THEN)
          .stepId(thenId)
          .build();
    }
    
    return END_STEP_POINTER;
  }
  
  private FlowProgramStepConditionalThenPointer visitSwitchNode(AstFlowSwitchNode decision) {
    final var condition = ImmutableFlowProgramStepConditionalThenPointer.builder();
    final var decisionId = decision.getKeyword();
    final var when = AstFlowNodesFactory.getStringValue(decision.getWhen());
    final var thenValue = AstFlowNodesFactory.getStringValue(decision.getThen());    
    try {
      final var isTrue = when == null || when.isEmpty();
      final var expression = isTrue ? 
          typesFactory.expression(ValueType.FLOW_CONTEXT, "true") :
          typesFactory.expression(ValueType.FLOW_CONTEXT, when);
      condition.expression(expression).stepId(thenValue);
    } catch(Exception e) {
      final var message = "Failed to evaluate expression: \"" + when + "\" in flow decision: " + decisionId + "!" + System.lineSeparator() + e.getMessage();
      throw new FlowAstException(message, e);
    } 
    return condition.build();
  }
  
  private Collection<TypeDef> visitAcceptDefs(AstFlow ast) {
    Map<String, AstFlowInputNode> inputs = ast.getSrc().getInputs();

    int index = 0;
    Collection<TypeDef> result = new ArrayList<>();
    for (Map.Entry<String, AstFlowInputNode> entry : inputs.entrySet()) {
      if (entry.getValue().getType() == null) {
        continue;
      }
      try {
        ValueType valueType = ValueType.valueOf(entry.getValue().getType().getValue());
        boolean required = AstFlowNodesFactory.getBooleanValue(entry.getValue().getRequired());
        result.add(this.typesFactory.dataType()
            .id(entry.getValue().getStart() + "")
            .order(index++)
            .name(entry.getKey()).valueType(valueType).direction(Direction.IN).required(required)
            .values(AstFlowNodesFactory.getStringValue(entry.getValue().getDebugValue()))
            .build());
        
      } catch (Exception e) {
        final String msg = String.format("Failed to convert data type from: %s, error: %s", entry.getValue().getType().getValue(), e.getMessage());
        throw new FlowAstException(msg, e);
      }
    }
    return Collections.unmodifiableCollection(result);
  }
}
