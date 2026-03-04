package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.Map;

import io.resys.limaone.ast.Yaml_CST.Yaml;

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
    YamlTaskBody getRef();
    YamlTaskBody getUserTask();
    YamlTaskBody getDecisionTable();
    YamlTaskBody getService();
    YamlTaskBody getReturns();
    Map<String, YamlSwitch> getSwitch();
  }
  
  interface YamlTaskBody extends Yaml {
    Yaml getRef();
    Yaml getCollection();
    Yaml getInputsNode();
    Map<String, Yaml> getInputs();
    String getObjectInput();
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
