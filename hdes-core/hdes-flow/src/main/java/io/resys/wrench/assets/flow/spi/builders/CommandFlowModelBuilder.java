package io.resys.wrench.assets.flow.spi.builders;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.resys.hdes.client.api.HdesAstTypes;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowRoot;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowSwitchNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowTaskNode;
import io.resys.hdes.client.api.ast.AstFlow.FlowCommandMessageType;
import io.resys.hdes.client.api.exceptions.FlowAstException;
import io.resys.hdes.client.api.execution.FlowProgram;
import io.resys.hdes.client.api.execution.FlowProgram.FlowTaskType;
import io.resys.hdes.client.api.execution.FlowProgram.Step;
import io.resys.hdes.client.api.execution.FlowProgram.StepBody;
import io.resys.hdes.client.api.execution.ImmutableFlowProgram;
import io.resys.hdes.client.api.execution.ImmutableStepBody;
import io.resys.hdes.client.spi.flow.ast.NodeFlowAdapter;
import io.resys.hdes.client.spi.flow.ast.beans.NodeFlowBean;
import io.resys.hdes.client.spi.flow.program.ImmutableStep;
import io.resys.wrench.assets.flow.spi.FlowDefinitionException;
import io.resys.wrench.assets.flow.spi.expressions.ExpressionFactory;

public class CommandFlowModelBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandFlowModelBuilder.class);
  private static final ImmutableStep EMPTY = new ImmutableStep("empty", null, FlowTaskType.END);

  private final HdesAstTypes nodeRepository;
  private final ObjectMapper objectMapper;
  private final ExpressionFactory parser;
  private final String input;

  private final ImmutableStep endNode = new ImmutableStep("end", null, FlowTaskType.END);
  private final Map<String, ImmutableStep> taskModels = new HashMap<>();

  private List<AstFlowTaskNode> tasksByOrder;
  private Map<String, AstFlowTaskNode> tasksById;
  private String flowId;
  private Optional<String> rename;

  public CommandFlowModelBuilder(
      HdesAstTypes nodeRepository,
      ObjectMapper objectMapper,
      ExpressionFactory parser,
      String input, Optional<String> rename) {
    super();
    this.nodeRepository = nodeRepository;
    this.parser = parser;
    this.objectMapper = objectMapper;
    this.input = input;
    this.rename = rename;
  }

  private AstFlowRoot parseModel(ArrayNode src) throws IOException {
    final var ast = nodeRepository.flow().src(src).build();
    List<String> messages = ast.getMessages().stream()
        .filter(t -> t.getType() == FlowCommandMessageType.ERROR)
        .map(t -> t.getValue())
        .collect(Collectors.toList());
    
    if(!messages.isEmpty()) {
      throw new FlowAstException(messages.toString(), ast.getSrc().getValue());
    }
    return ast.getSrc();
  } 
  
  public Map.Entry<String, FlowProgram> build() {
    try {
      final AstFlowRoot data;
      final ArrayNode src;
      final String input;
      if(rename.isPresent()) {
        
        ArrayNode original = (ArrayNode) objectMapper.readTree(this.input);
        AstFlowRoot originalModel = parseModel(original);
        AstFlowNode idNode = originalModel.getId();
        
        ObjectNode renameNode = objectMapper.createObjectNode();
        renameNode.set("id", IntNode.valueOf(idNode.getStart()));
        renameNode.set("type", TextNode.valueOf(AstCommandValue.SET.name()));
        renameNode.set("value", TextNode.valueOf("id: " + rename.get()));
        original.add(renameNode);
        
        input = objectMapper.writeValueAsString(original);
        src = (ArrayNode) objectMapper.readTree(input);
        data = parseModel(src);        
      } else {
        input = this.input;
        src = (ArrayNode) objectMapper.readTree(input);
        data = parseModel(src);
      }
      
      tasksById = data.getTasks().values().stream()
          .collect(Collectors.toMap(n -> NodeFlowAdapter.getStringValue(n.getId()), n -> n));
      tasksByOrder = new ArrayList<>(data.getTasks().values());
      Collections.sort(tasksByOrder);
      flowId = NodeFlowAdapter.getStringValue(data.getId());

      AstFlowTaskNode firstTask = data.getTasks().values().stream()
          .filter(task -> task.getOrder() == 0)
          .findFirst().orElse(null);

      final ImmutableStep task = tasksById.isEmpty() ? EMPTY: createNode(firstTask);
      final FlowProgram model = ImmutableFlowProgram.builder()
          .id(flowId)
          .rev(src.size())
          .step(task)
          .tasks(getTasks(task))
          .acceptDefs(NodeFlowAdapter.getInputs(data, nodeRepository))
          .build();
      
      return new AbstractMap.SimpleEntry<String, FlowProgram>(input, model);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  private Collection<Step> getTasks(Step node) {
    Map<String, Step> result = new HashMap<>();
    getServices(result, node);
    return result.values();
  }
  private void getServices(Map<String, Step> visited, Step node) {
    if(visited.containsKey(node.getId())) {
      return;
    }
    visited.put(node.getId(), node);
    node.getNext().forEach(n -> getServices(visited, n));
  }

  protected ImmutableStep createNode(AstFlowTaskNode task) {
    String taskId = NodeFlowAdapter.getStringValue(task.getId());
    if(taskModels.containsKey(taskId)) {
      return taskModels.get(taskId);
    }

    FlowTaskType type = NodeFlowAdapter.getTaskType(task);
    StepBody taskValue = createFlowTaskValue(task, type);
    final ImmutableStep result = new ImmutableStep(taskId, taskValue, type);
    taskModels.put(taskId, result);

    final ImmutableStep intermediate;

    if(type != FlowTaskType.EMPTY) {
      intermediate = new ImmutableStep(taskId + "-" + FlowTaskType.MERGE, null, FlowTaskType.MERGE);
      result.addNext(intermediate);
    } else {
      intermediate = result;
    }

    boolean isEnd = endNode.getId().equals(NodeFlowAdapter.getStringValue(task.getThen()));
    String then = NodeFlowAdapter.getStringValue(task.getThen());

    // Add next
    if(then != null && !isEnd) {
      intermediate.addNext(createNode(tasksById.get(getThenTaskId(task, then, type))));
    }

    if(isEnd) {
      intermediate.addNext(endNode);
    }

    // Only exclusive decisions have next in here
    if(task.getSwitch().isEmpty()) {
      return result;
    }

    // Exclusive decision gateway
    ImmutableStep exclusive = new ImmutableStep(taskId + "-" + FlowTaskType.EXCLUSIVE, null, FlowTaskType.EXCLUSIVE);
    intermediate.addNext(exclusive);

    List<AstFlowSwitchNode> decisions = new ArrayList<>(task.getSwitch().values());
    Collections.sort(decisions, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));

    for(AstFlowSwitchNode decision : decisions) {
      String decisionId = decision.getKeyword();

      try {
        String when = NodeFlowAdapter.getStringValue(decision.getWhen());
        String thenValue = NodeFlowAdapter.getStringValue(decision.getThen());

        ImmutableStep next = new ImmutableStep(
            decisionId,
            parser.get(when),
            FlowTaskType.DECISION);
        exclusive.addNext(next);

        if(thenValue != null && !endNode.getId().equals(thenValue)) {
          next.addNext(createNode(tasksById.get(getThenTaskId(task, thenValue, type))));
        }

      } catch(Exception e) {
        ImmutableStep errorModel = new ImmutableStep(decisionId, null, FlowTaskType.DECISION);
        String message = "Failed to evaluate expression: \"" + taskId + "\" in flow: " + flowId + ", decision: " + errorModel.getId() + "!" + System.lineSeparator() + e.getMessage();
        LOGGER.error(message, e);
        throw new FlowDefinitionException(message, errorModel, e);
      }
    }

    return result;
  }

  private String getThenTaskId(AstFlowTaskNode task, String then, FlowTaskType type) {
    if(!NodeFlowBean.VALUE_NEXT.equalsIgnoreCase(then)) {
      return then;
    }

    AstFlowTaskNode next = null;
    for(AstFlowTaskNode node : tasksByOrder) {
      if(node.getStart() > task.getStart()) {
        next = node;
      }
    }

    if(next == null) {
      String taskId = NodeFlowAdapter.getStringValue(task.getId());
      ImmutableStep errorModel = new ImmutableStep(taskId, null, type);
      String message = "There are no next task after: \"" + taskId + "\" in flow: " + flowId + ", decision: " + errorModel.getId() + "!";
      throw new FlowDefinitionException(message, errorModel);
    }
    return NodeFlowAdapter.getStringValue(next.getId());
  }

  public StepBody createFlowTaskValue(AstFlowTaskNode task, FlowTaskType type) {
    if(type == FlowTaskType.SERVICE || type == FlowTaskType.DT || type == FlowTaskType.USER_TASK) {

      boolean collection = NodeFlowAdapter.getBooleanValue(task.getRef().getCollection());
      String ref = NodeFlowAdapter.getStringValue(task.getRef().getRef());
      Map<String, String> inputs = new HashMap<>();
      for(Map.Entry<String, AstFlowNode> entry : task.getRef().getInputs().entrySet()) {
        inputs.put(entry.getKey(), NodeFlowAdapter.getStringValue(entry.getValue()));
      }
      return ImmutableStepBody.builder()
          .isCollection(collection)
          .ref(ref)
          .putAllInputs(inputs)
          .build();
    } else if(type == FlowTaskType.EMPTY) {
      return ImmutableStepBody.builder().isCollection(false).build();
    }

    throw new IllegalArgumentException("Can't create task value from type: " + type + "!");
  }
}
