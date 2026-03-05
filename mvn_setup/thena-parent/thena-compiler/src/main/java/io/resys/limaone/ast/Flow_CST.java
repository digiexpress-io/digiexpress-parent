package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.Map;

import io.resys.limaone.ast.Yaml_CST.Yaml;
import jakarta.annotation.Nullable;

public interface Flow_CST extends Serializable {


  interface YamlFlow extends Yaml {
    Yaml getId();
    Yaml getDescription();
    Map<String, YamlInput> getInputs();
    Map<String, YamlTask> getTasks();
  }

  interface YamlTask extends Yaml {
    Yaml getId();
    int getOrder(); // 0 = first task
    Yaml getThen();
    @Nullable YamlTaskBody getRef();
    @Nullable YamlTaskBody getUserTask();
    @Nullable YamlTaskBody getDecisionTable();
    @Nullable YamlTaskBody getService();
    @Nullable YamlTaskBody getReturns();
    Map<String, YamlSwitch> getSwitch();
  }
  
  interface YamlTaskBody extends Yaml {
    Yaml getRef();
    Yaml getCollection();
    Yaml getInputsNode();
    Map<String, Yaml> getInputs();
    @Nullable String getObjectInput();
  }

  interface YamlSwitch extends Yaml {
    int getOrder();
    Yaml getWhen();
    Yaml getThen();
  }

  interface YamlInput extends Yaml {
    /*
     * valid input types: 
     *   - ValueType.STRING, 
     *   - ValueType.BOOLEAN, 
     *   - ValueType.INTEGER, 
     *   - ValueType.LONG, 
     *   - ValueType.DECIMAL, 
     *   - ValueType.DATE, 
     *   - ValueType.DATE_TIME
     */
    Yaml getRequired();
    Yaml getType();
    Yaml getDebugValue();
  }
}
