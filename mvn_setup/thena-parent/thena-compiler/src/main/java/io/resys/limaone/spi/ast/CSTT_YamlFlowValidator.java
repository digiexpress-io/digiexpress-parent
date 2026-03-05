package io.resys.limaone.spi.ast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.resys.limaone.ast.Flow_CST.YamlFlow;
import io.resys.limaone.ast.Flow_CST.YamlInput;
import io.resys.limaone.ast.Flow_CST.YamlTask;
import io.resys.limaone.ast.Yaml_CST.Yaml;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.yaml.YamlMapper;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CSTT_YamlFlowValidator {

  private final YamlFlow yaml;
  private final List<ModelError> messages = new ArrayList<>();
  private final Map<String, PseudoParam> inputs = new HashMap<>();
  private final Map<String, YamlTask> tasks = new HashMap<>();
  
  public CSTT_YamlFlowValidator(YamlFlow yaml) {
    this.yaml = yaml;
  }
  
  public List<ModelError> validate() {
    
    for(final var input : yaml.getInputs().entrySet()) {
      validateInput(input.getKey(), input.getValue());
    }
    for(final var task : yaml.getTasks().values()) {
      final var id = YamlMapper.getStringValue(task.getId());
      if(id == null) {
        continue;
      }
      tasks.put(id, task);
    }
    for(final var task : yaml.getTasks().entrySet()) {
      validateInputs(task.getKey(), task.getValue());
    }
    return messages;
    
  }
  
  private void validateInput(String name, YamlInput input) {
    if(inputs.containsKey(name)) {
      error(input,
          input.getStart(),
          input.getSyntax().length(),
          "Can't have duplicate param: " + name + "!");
    
    } else {
      ValueType type = ValueType.UNKNOWN;
      try {
        type = ValueType.valueOf(input.getType().getValue());
      } catch(Exception e) {
        error(input,
            input.getStart(),
            input.getSyntax().length(),
            "Can't resolve input param: " + name + "!");
      }
      inputs.put(name, new PseudoParam(input, type, name));
    }
  }
  private Map<String, PseudoParam> validateInputs(String taskId, YamlTask taskModel) {    
    final var result = new HashMap<String, PseudoParam>();
    if(taskModel.getRef() == null) {
      return result;
    }
    
    final var objectInput = taskModel.getRef().getObjectInput();

    // see if object input matches any of the flow inputs
    if(objectInput != null) {
      final Optional<PseudoParam> matchedInput = Optional.ofNullable(this.inputs.get(objectInput));
      if (matchedInput.isEmpty()) {
        error(taskModel,
            taskModel.getRef().getRef().getStart(),
            taskModel.getRef().getRef().getSyntax().length(),
            "Task: " + taskId + ", has unknown object input: '" + objectInput + "'!");
      } else if(matchedInput.get().getDataType() != ValueType.OBJECT) {
        error(taskModel.getRef(),
            taskModel.getRef().getStart(),
            taskModel.getRef().getSyntax().length(),
            "Task: " + taskId + ", input: '" + objectInput + "', type has wrong type, expecting: 'OBJECT' but was: '" + matchedInput.get().getDataType().name() + "'!");
      } else {
        result.put(objectInput, new PseudoParam(taskModel.getRef().getInputsNode(), matchedInput.get().getDataType(), objectInput));
      }
    }
    
    for (final var entry : taskModel.getRef().getInputs().entrySet()) {
      final var node = entry.getValue();
      final var mapTo = node.getKeyword();
      final var mapFrom = Optional.ofNullable(YamlMapper.getStringValue(node)).orElse("")
          .split("\\.")[0];
      
      if (StringUtils.isEmpty(mapFrom) || StringUtils.isEmpty(mapTo) ) {
        error(taskModel,
            node.getStart(),
            node.getSyntax().length(),
            "Task: " + taskId + " mapping: '" + entry.getKey() + "' is missing value!");
        
      } else if (tasks.containsKey(mapFrom)) {
        result.put(entry.getKey(), new PseudoParam(node, ValueType.UNKNOWN, mapTo));

      } else if (this.inputs.containsKey(mapFrom)) {
        final var input = this.inputs.get(mapFrom);
        result.put(entry.getKey(), new PseudoParam(node, input.getDataType(), mapTo));
        
      } else {
        error(taskModel,
            node.getStart(),
            node.getSyntax().length(),
            "Task: " + taskId + ", has unknown input: '" + entry.getKey() + "'!");
        
      }
    }
    return result;
  }
  
  private void error(Yaml toValidate, int start, int range, String value) {
    messages.add(ImmutableModelError.builder().line(start).column(range).msg(value).build());
  }
  
  @Value
  private static class PseudoParam {
    Yaml node;
    ValueType dataType;
    String name;
  }
}
