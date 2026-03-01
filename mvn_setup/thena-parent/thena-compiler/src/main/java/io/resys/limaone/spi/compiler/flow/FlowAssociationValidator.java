package io.resys.limaone.spi.compiler.flow;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_AST.Yaml;
import io.resys.limaone.ast.Flow_AST.YamlInput;
import io.resys.limaone.ast.Flow_AST.YamlTask;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.FlowProgram.FlowProgramStep;
import io.resys.limaone.spi.ast.flow.YamlMapper;
import io.resys.limaone.spi.ast.flow.MutableYamlParseTree;

public class FlowAssociationValidator {

  private final Flow_AST ast;
  private final Map<String, Parameter> allParams = new HashMap<>();
  private final List<TaskStepToValidate> toValidate = new ArrayList<>();
  
  public FlowAssociationValidator(Flow_AST ast) {
    this.ast = ast;
  }

  public void visitStep(FlowProgramStep step, Simple_AST wrapper) {
    final var taskModel = ast.getParseTree().getTasks().values().stream()
        .filter(t -> t.getId() != null && t.getId().getValue().equals(step.getId()))
        .findFirst().get();
    
    for(Parameter param : wrapper.getHeaders().getReturnDefs()) {
      if(param.getDirection() == Direction.OUT) {
        String name = YamlMapper.getStringValue(taskModel.getId()) + "." + param.getName();
        HdesAssert.isTrue(!allParams.containsKey(name), () -> "Can't have duplicate param: " + name + "!");
        allParams.put(name, param);
      }
    }
    toValidate.add(new TaskStepToValidate(step, wrapper, taskModel));
  }
  
  public List<TaskStepToValidate> build() {    
    final var node = ast.getParseTree();
    Map<String, YamlInput> unusedInputs = new HashMap<>(node.getInputs());
    for(final var entry : toValidate) {

      // Validate inputs
      final var taskInputs = getTaskServiceInput(entry);
      final var taskModel = entry.getTaskNode();

      if (taskInputs.size() == 1 && taskInputs.values().stream().findFirst().get().getNode().getKeyword().equals(MutableYamlParseTree.KEY_INPUTS)) {
        TaskInput taskInput = taskInputs.values().stream().findFirst().get();

        ValueType ref = taskInput.getDataType().getValueType();
        if (!taskInput.getDataType().getValueType().equals(ValueType.OBJECT)) {
          error(entry,
              taskInput.getNode().getStart(),
              taskInput.getNode().getSyntax().length(),
              "Task: " + taskModel.getKeyword() + ", input: '" + taskInput.getNode().getValue() + "', type has wrong type, expecting: 'OBJECT' but was: '" + ref + "'!");
        }
        taskInputs.remove(taskInput.getNode().getValue());
        unusedInputs.remove(taskInput.getNode().getValue());

      } else {
        for (final var input : entry.getWrapper().getHeaders().getAcceptDefs()) {

          if (taskInputs.containsKey(input.getName())) {
            TaskInput taskInput = taskInputs.get(input.getName());
            if (taskInput.getDataType() == null) {
              error(entry,
                  taskInput.getNode().getStart(),
                  taskInput.getNode().getSyntax().length(),
                  "Task: " + taskModel.getKeyword() + ", input: '" + input.getName() + "', type has unknown mapping:'" + taskInput.getNode().getValue() + "'!");
              continue;
            }
            ValueType ref = taskInput.getDataType().getValueType();
            if (input.getValueType() != ref) {
              error(entry,
                  taskInput.getNode().getStart(),
                  taskInput.getNode().getSyntax().length(),
                  "Task: " + taskModel.getKeyword() + ", input: '" + input.getName() + "', type has wrong type, expecting:'" + input.getValueType() + "' but was: '" + ref + "'!");
            }
            taskInputs.remove(input.getName());
            unusedInputs.remove(taskInput.getDataType().getName());
          } else if(input.isRequired()) {
            error(entry,
                taskModel.getRef().getInputsNode() == null ? taskModel.getRef().getStart() : taskModel.getRef().getInputsNode().getStart(),
                taskModel.getRef().getInputsNode() == null ? taskModel.getRef().getStart() : taskModel.getRef().getInputsNode().getSyntax().length(),
                "Task: " + taskModel.getKeyword() + ", is missing input: '" + input.getName() + "'!");
          }
        }
      }

      // Unused inputs on task
      for(TaskInput input : taskInputs.values()) {
        String inputName = input.getDataType() == null ? input.getNode().getKeyword() : input.getDataType().getName();
        error(entry,
            input.getNode().getStart(),
            input.getNode().getSyntax().length(),
            "Task: " + taskModel.getId().getValue() + ", has unused input: '" + inputName + "'!");
      }
    }
    
    return toValidate;
  }



  private Map<String, TaskInput> getTaskServiceInput(TaskStepToValidate toValidate) {

    final var taskModel = toValidate.getTaskNode();
    final var wrapper = toValidate.getWrapper();
    
    Map<String, Parameter> serviceTypes = wrapper.getHeaders()
        .getAcceptDefs().stream()
        .collect(Collectors.toMap(p -> p.getName(), p -> p));

    Map<String, TaskInput> result = new HashMap<>();
    final var objectInput = taskModel.getRef().getObjectInput();

    if (objectInput != null) {
      // see if object input matches any of the flow inputs
      Optional<Parameter> matchedInput = ast.getHeaders().getAcceptDefs().stream().filter(Parameter -> Parameter.getName().equals(objectInput)).findFirst();
      if (matchedInput.isPresent()) {
        result.put(objectInput, new TaskInput(taskModel.getRef().getInputsNode(), matchedInput.get()));
      } else {
        error(toValidate,
          taskModel.getRef().getRef().getStart(),
          taskModel.getRef().getRef().getSyntax().length(),
          "Task: " + taskModel.getKeyword() + ", has unknown object input: '" + objectInput + "'!");
      }
    } else {
      for (final var entry : taskModel.getRef().getInputs().entrySet()) {
        final var node = entry.getValue();
        String mappingName = YamlMapper.getStringValue(node);
        if (StringUtils.isEmpty(mappingName)) {
          error(toValidate,
              node.getStart(),
              node.getSyntax().length(),
              "Task: " + taskModel.getKeyword() + " mapping: '" + entry.getKey() + "' is missing value!");
        } else if (!serviceTypes.containsKey(entry.getKey())) {
          error(toValidate,
              node.getStart(),
              node.getSyntax().length(),
              "Task: " + taskModel.getKeyword() + ", has unknown input: '" + entry.getKey() + "'!");
        } else if (allParams.containsKey(mappingName)) {
          result.put(entry.getKey(), new TaskInput(node, allParams.get(mappingName)));
        } else {
          result.put(entry.getKey(), new TaskInput(node, serviceTypes.get(entry.getKey())));
        }
      }
    }
    return result;
  }
  
  private void error(TaskStepToValidate toValidate, int start, int range, String value) {
    toValidate.getMessages().add(ImmutableModelError.builder()
        .line(start)
        .range(YamlMapper.range().build(0, range))
        .type(CommandMessageType.ERROR)
        .value(value)
        .build());
  }


  public static class TaskStepToValidate {
    private final FlowProgramStep step; 
    private final Simple_AST wrapper;
    private final YamlTask taskNode;
    private final List<ModelError> messages = new ArrayList<>();
    private TaskStepToValidate(FlowProgramStep step, Simple_AST wrapper, YamlTask taskNode) {
      super();
      this.step = step;
      this.wrapper = wrapper;
      this.taskNode = taskNode;
    }
    public FlowProgramStep getStep() {
      return step;
    }
    public List<ModelError> getMessages() {
      return messages;
    }
    public Simple_AST getWrapper() {
      return wrapper;
    }
    public YamlTask getTaskNode() {
      return taskNode;
    }
  }
  
  private static class TaskInput {
    private final Yaml node;
    private final Parameter dataType;
    public TaskInput(Yaml node, Parameter dataType) {
      super();
      this.node = node;
      this.dataType = dataType;
    }
    public Yaml getNode() {
      return node;
    }
    public Parameter getDataType() {
      return dataType;
    }
  }
}
