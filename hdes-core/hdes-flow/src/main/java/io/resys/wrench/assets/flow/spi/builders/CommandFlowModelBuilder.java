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

import io.resys.wrench.assets.datatype.api.DataTypeRepository;
import io.resys.wrench.assets.flow.api.FlowAstFactory;
import io.resys.wrench.assets.flow.api.FlowAstFactory.Node;
import io.resys.wrench.assets.flow.api.FlowAstFactory.NodeBuilder;
import io.resys.wrench.assets.flow.api.FlowAstFactory.NodeFlow;
import io.resys.wrench.assets.flow.api.FlowAstFactory.NodeSwitch;
import io.resys.wrench.assets.flow.api.FlowAstFactory.NodeTask;
import io.resys.wrench.assets.flow.api.model.FlowAst.FlowCommandMessageType;
import io.resys.wrench.assets.flow.api.model.FlowAst.FlowCommandType;
import io.resys.wrench.assets.flow.api.model.FlowModel;
import io.resys.wrench.assets.flow.api.model.FlowModel.FlowTaskModel;
import io.resys.wrench.assets.flow.api.model.FlowModel.FlowTaskType;
import io.resys.wrench.assets.flow.api.model.FlowModel.FlowTaskValue;
import io.resys.wrench.assets.flow.api.model.ImmutableFlowModel;
import io.resys.wrench.assets.flow.api.model.ImmutableFlowTaskValue;
import io.resys.wrench.assets.flow.spi.FlowException;
import io.resys.wrench.assets.flow.spi.exceptions.NodeFlowException;
import io.resys.wrench.assets.flow.spi.expressions.ExpressionFactory;
import io.resys.wrench.assets.flow.spi.model.ImmutableFlowTaskModel;
import io.resys.wrench.assets.flow.spi.model.NodeFlowBean;
import io.resys.wrench.assets.flow.spi.support.NodeFlowAdapter;

public class CommandFlowModelBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandFlowModelBuilder.class);
  private static final ImmutableFlowTaskModel EMPTY = new ImmutableFlowTaskModel("empty", null, FlowTaskType.END);

  private final FlowAstFactory nodeRepository;
  private final ObjectMapper objectMapper;
  private final DataTypeRepository dataTypeRepository;
  private final ExpressionFactory parser;
  private final String input;

  private final ImmutableFlowTaskModel endNode = new ImmutableFlowTaskModel("end", null, FlowTaskType.END);
  private final Map<String, ImmutableFlowTaskModel> taskModels = new HashMap<>();

  private List<NodeTask> tasksByOrder;
  private Map<String, NodeTask> tasksById;
  private String flowId;
  private Optional<String> rename;

  public CommandFlowModelBuilder(
      FlowAstFactory nodeRepository,
      ObjectMapper objectMapper,
      DataTypeRepository dataTypeRepository,
      ExpressionFactory parser,
      String input, Optional<String> rename) {
    super();
    this.dataTypeRepository = dataTypeRepository;
    this.nodeRepository = nodeRepository;
    this.parser = parser;
    this.objectMapper = objectMapper;
    this.input = input;
    this.rename = rename;
  }

  private NodeFlow parseModel(ArrayNode src) throws IOException {
    
    List<String> messages = new ArrayList<>();
    NodeBuilder nodeBuilder = nodeRepository.create(message -> {
      if(message.getType() == FlowCommandMessageType.ERROR) {
        messages.add(message.getValue());
      }
    });
    src.forEach(command -> create((ObjectNode) command, nodeBuilder));
    NodeFlow data = nodeBuilder.build();

    if(!messages.isEmpty()) {
      throw new NodeFlowException(messages.toString(), data.getValue());
    }
    
    return data;
  } 
  
  public Map.Entry<String, FlowModel> build() {
    try {
      final NodeFlow data;
      final ArrayNode src;
      final String input;
      if(rename.isPresent()) {
        
        ArrayNode original = (ArrayNode) objectMapper.readTree(this.input);
        NodeFlow originalModel = parseModel(original);
        Node idNode = originalModel.getId();
        
        ObjectNode renameNode = objectMapper.createObjectNode();
        renameNode.set("id", IntNode.valueOf(idNode.getStart()));
        renameNode.set("type", TextNode.valueOf(FlowCommandType.SET.name()));
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

      NodeTask firstTask = data.getTasks().values().stream()
          .filter(task -> task.getOrder() == 0)
          .findFirst().orElse(null);

      final ImmutableFlowTaskModel task = tasksById.isEmpty() ? EMPTY: createNode(firstTask);
      final FlowModel model = ImmutableFlowModel.builder()
          .id(flowId)
          .rev(src.size())
          .src(data.getValue())
          .description(NodeFlowAdapter.getStringValue(data.getDescription()))
          .task(task)
          .tasks(getTasks(task))
          .inputs(NodeFlowAdapter.getInputs(data, dataTypeRepository))
          .build();
      
      return new AbstractMap.SimpleEntry<String, FlowModel>(input, model);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  private Collection<FlowTaskModel> getTasks(FlowTaskModel node) {
    Map<String, FlowTaskModel> result = new HashMap<>();
    getServices(result, node);
    return result.values();
  }
  private void getServices(Map<String, FlowTaskModel> visited, FlowTaskModel node) {
    if(visited.containsKey(node.getId())) {
      return;
    }
    visited.put(node.getId(), node);
    node.getNext().forEach(n -> getServices(visited, n));
  }

  protected ImmutableFlowTaskModel createNode(NodeTask task) {
    String taskId = NodeFlowAdapter.getStringValue(task.getId());
    if(taskModels.containsKey(taskId)) {
      return taskModels.get(taskId);
    }

    FlowTaskType type = NodeFlowAdapter.getTaskType(task);
    FlowTaskValue taskValue = createFlowTaskValue(task, type);
    final ImmutableFlowTaskModel result = new ImmutableFlowTaskModel(taskId, taskValue, type);
    taskModels.put(taskId, result);

    final ImmutableFlowTaskModel intermediate;

    if(type != FlowTaskType.EMPTY) {
      intermediate = new ImmutableFlowTaskModel(taskId + "-" + FlowTaskType.MERGE, null, FlowTaskType.MERGE);
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
    ImmutableFlowTaskModel exclusive = new ImmutableFlowTaskModel(taskId + "-" + FlowTaskType.EXCLUSIVE, null, FlowTaskType.EXCLUSIVE);
    intermediate.addNext(exclusive);

    List<NodeSwitch> decisions = new ArrayList<>(task.getSwitch().values());
    Collections.sort(decisions, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));

    for(NodeSwitch decision : decisions) {
      String decisionId = decision.getKeyword();

      try {
        String when = NodeFlowAdapter.getStringValue(decision.getWhen());
        String thenValue = NodeFlowAdapter.getStringValue(decision.getThen());

        ImmutableFlowTaskModel next = new ImmutableFlowTaskModel(
            decisionId,
            parser.get(when),
            FlowTaskType.DECISION);
        exclusive.addNext(next);

        if(thenValue != null && !endNode.getId().equals(thenValue)) {
          next.addNext(createNode(tasksById.get(getThenTaskId(task, thenValue, type))));
        }

      } catch(Exception e) {
        ImmutableFlowTaskModel errorModel = new ImmutableFlowTaskModel(decisionId, null, FlowTaskType.DECISION);
        String message = "Failed to evaluate expression: \"" + taskId + "\" in flow: " + flowId + ", decision: " + errorModel.getId() + "!" + System.lineSeparator() + e.getMessage();
        LOGGER.error(message, e);
        throw new FlowException(message, errorModel, e);
      }
    }

    return result;
  }

  private String getThenTaskId(NodeTask task, String then, FlowTaskType type) {
    if(!NodeFlowBean.VALUE_NEXT.equalsIgnoreCase(then)) {
      return then;
    }

    NodeTask next = null;
    for(NodeTask node : tasksByOrder) {
      if(node.getStart() > task.getStart()) {
        next = node;
      }
    }

    if(next == null) {
      String taskId = NodeFlowAdapter.getStringValue(task.getId());
      ImmutableFlowTaskModel errorModel = new ImmutableFlowTaskModel(taskId, null, type);
      String message = "There are no next task after: \"" + taskId + "\" in flow: " + flowId + ", decision: " + errorModel.getId() + "!";
      throw new FlowException(message, errorModel);
    }
    return NodeFlowAdapter.getStringValue(next.getId());
  }

  public FlowTaskValue createFlowTaskValue(NodeTask task, FlowTaskType type) {
    if(type == FlowTaskType.SERVICE || type == FlowTaskType.DT || type == FlowTaskType.USER_TASK) {

      boolean collection = NodeFlowAdapter.getBooleanValue(task.getRef().getCollection());
      String ref = NodeFlowAdapter.getStringValue(task.getRef().getRef());
      Map<String, String> inputs = new HashMap<>();
      for(Map.Entry<String, Node> entry : task.getRef().getInputs().entrySet()) {
        inputs.put(entry.getKey(), NodeFlowAdapter.getStringValue(entry.getValue()));
      }
      return ImmutableFlowTaskValue.builder()
          .isCollection(collection)
          .ref(ref)
          .putAllInputs(inputs)
          .build();
    } else if(type == FlowTaskType.EMPTY) {
      return ImmutableFlowTaskValue.builder().isCollection(false).build();
    }

    throw new IllegalArgumentException("Can't create task value from type: " + type + "!");
  }


  private void create(ObjectNode command, NodeBuilder builder) {
    int line = command.get("id").asInt();
    FlowCommandType type = FlowCommandType.valueOf(command.get("type").asText());


    if(type == FlowCommandType.DELETE) {
      builder.delete(line, command.get("value").asInt());
    } else if(type == FlowCommandType.ADD) {
      builder.add(line, getText(command));
    } else {
      builder.set(line, getText(command));
    }
  }

  private String getText(ObjectNode command) {
    return command.hasNonNull("value") ? command.get("value").asText() : null;
  }
}
