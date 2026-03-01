package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

public interface Flow_CST extends Serializable {

  @Value.Immutable
  interface YamlInputType extends Flow_CST {
    String getName();
    String getValue();
    @Nullable String getRef();
  }

  interface YamlParseTree extends Yaml {
    Yaml getId();
    Yaml getDescription();
    Collection<YamlInputType> getTypes();
    Map<String, YamlInput> getInputs();
    Map<String, YamlTask> getTasks();
  }

  interface YamlTask extends Yaml {
    Yaml getId();
    int getOrder(); // 0 = first task
    Yaml getThen();
    YamlBody getRef();
    YamlBody getUserTask();
    YamlBody getDecisionTable();
    YamlBody getService();
    YamlBody getReturns();
    
    Map<String, YamlSwitch> getSwitch();
  }
  
  interface YamlBody extends Yaml {
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
    Yaml getRequired();
    Yaml getType();
    Yaml getDebugValue();
  }

  interface Yaml extends Flow_CST, Comparable<Yaml> {
    Yaml getParent();
    String getKeyword();
    Map<String, Yaml> getChildren();
    Yaml get(String name);
    String getValue();
    String getSyntax();
    boolean hasNonNull(String name);
    int getStart();
    int getEnd();
  }
}
