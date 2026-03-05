package io.resys.limaone.spi.ast.flow;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.resys.limaone.ast.Flow_CST.YamlTaskBody;
import io.resys.limaone.ast.Flow_CST.YamlInput;
import io.resys.limaone.ast.Flow_CST.YamlFlow;
import io.resys.limaone.ast.Flow_CST.YamlSwitch;
import io.resys.limaone.ast.Flow_CST.YamlTask;
import io.resys.limaone.spi.ast.AST_Exception;
import io.resys.limaone.yaml.MutableYaml;



public class MutableYamlFlow extends MutableYaml implements YamlFlow {
  public static final long serialVersionUID = 8492235102091866790L;
  public static final String KEY_ID = "id";
  public static final String KEY_THEN = "then";
  public static final String KEY_WHEN = "when";
  public static final String KEY_SWITCH = "switch";
  public static final String KEY_DESC = "description";
  public static final String KEY_INPUTS = "inputs";
  public static final String KEY_TASKS = "tasks";
  public static final String KEY_REQ = "required";
  public static final String KEY_TYPE = "type";
  public static final String KEY_DT = "decisionTable";
  public static final String KEY_RETURNS = "returns";
  public static final String KEY_USER_TASK = "userTask";
  public static final String KEY_REF = "ref";
  public static final String KEY_COLLECTION = "collection";
  public static final String KEY_SERVICE = "service";
  
  public static final String VALUE_NEXT = "next";
  public static final String VALUE_END = "end";
  
  public static final String KEY_DEBUG_VALUE = "debugValue";
  public static final String OBJECT_INPUT_FLAG = "OBJECT_INPUT";

  private NodeInputs inputs;
  private NodeTasks tasks;
  private String value;

  public MutableYamlFlow() {
    super(null, -2, null, null, null);
  }
  @Override
  public Yaml getId() {
    return get(KEY_ID);
  }
  @Override
  public Yaml getDescription() {
    return get(KEY_DESC);
  }
  @Override
  public Map<String, YamlInput> getInputs() {
    return inputs == null ? Collections.emptyMap() : inputs.getInputs();
  }
  @Override
  public Map<String, YamlTask> getTasks() {
    return tasks == null ? Collections.emptyMap() : tasks.getTasks();
  }
  @Override
  public String getValue() {
    return value;
  }
  public MutableYamlFlow setValue(String value) {
    this.value = value;
    return this;
  }
  @Override
  public MutableYamlFlow setEnd(int value) {
    super.setEnd(value);
    return this;
  }
  @Override
  public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
    if(KEY_INPUTS.equals(keyword)) {
      if(inputs == null) {
        inputs = new NodeInputs(source, indent, keyword, value, this);
        addChild(inputs);
      }
      return inputs;
    } else if(KEY_TASKS.equals(keyword)) {
      if(tasks == null) {
        tasks = new NodeTasks(source, indent, keyword, value, this);
        addChild(tasks);
      }
      return tasks;
    }
    return super.addChild(source, indent, keyword, value);
  }

  private static class NodeInputs extends MutableYaml {
    private static final long serialVersionUID = 8989618439864849749L;
    private final Map<String, YamlInput> inputs = new HashMap<>();
    public NodeInputs(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
    }
    public Map<String, YamlInput> getInputs() {
      return Collections.unmodifiableMap(inputs);
    }
    @Override
    public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
      MutableYamlInput result = new MutableYamlInput(source, indent, keyword, value, this);
      if(inputs.containsKey(result.getKeyword())) {
        String message = String.format("Duplicate input: %s!", result.getKeyword());
        throw new AST_Exception(message);
      }
      inputs.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class NodeTasks extends MutableYaml {
    private static final long serialVersionUID = 2001644047832806256L;
    private final Map<String, YamlTask> tasks = new HashMap<>();
    private int order = 0;
    public NodeTasks(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
    }

    public Map<String, YamlTask> getTasks() {
      return Collections.unmodifiableMap(tasks);
    }
    @Override
    public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
      MutableYamlTask result = new MutableYamlTask(source, order++, indent, keyword, value, this);
      tasks.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class MutableYamlInput extends MutableYaml implements YamlInput {
    private static final long serialVersionUID = 8910489078429824772L;
    public MutableYamlInput(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
    }
    @Override
    public Yaml getRequired() {
      return get(KEY_REQ);
    }

    @Override
    public Yaml getType() {
      return get(KEY_TYPE);
    }
    @Override
    public Yaml getDebugValue() {
      return get(KEY_DEBUG_VALUE);
    }
  }

  private static class MutableYamlSwitch extends MutableYaml implements YamlSwitch {
    private static final long serialVersionUID = 8910489078429824772L;
    private final int order;

    public MutableYamlSwitch(NodeSource source, int order, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
      this.order = order;
    }
    @Override
    public Yaml getThen() {
      return get(KEY_THEN);
    }
    @Override
    public Yaml getWhen() {
      return get(KEY_WHEN);
    }
    @Override
    public int getOrder() {
      return order;
    }
  }

  private static class MutableYamlCases extends MutableYaml {
    private static final long serialVersionUID = 2001644047832806256L;
    private final Map<String, YamlSwitch> cases = new HashMap<>();
    private int order = 0;
    public MutableYamlCases(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
    }

    public Map<String, YamlSwitch> getValues() {
      return Collections.unmodifiableMap(cases);
    }
    @Override
    public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
      MutableYamlSwitch result = new MutableYamlSwitch(source, order++, indent, keyword, value, this);
      cases.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class MutableYamlTask extends MutableYaml implements YamlTask {
    private static final long serialVersionUID = 8910489078429824772L;
    private final int order;
    private MutableYamlTaskBody decisionTable;
    private MutableYamlTaskBody userTask;
    private MutableYamlTaskBody service;
    private MutableYamlTaskBody returns;
    private MutableYamlCases cases;

    public MutableYamlTask(NodeSource source, int order, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
      this.order = order;
    }
    @Override
    public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
      if(KEY_SWITCH.equals(keyword)) {
        if(cases == null) {
          cases = new MutableYamlCases(source, indent, keyword, value, this);
          addChild(cases);
        }
        return cases;

      } else if(KEY_USER_TASK.equals(keyword)) {
        if(userTask == null) {
          userTask = new MutableYamlTaskBody(source, indent, keyword, value, this);
          addChild(userTask);
        }
        return userTask;
      } else if(KEY_DT.equals(keyword)) {
        if(decisionTable == null) {
          decisionTable = new MutableYamlTaskBody(source, indent, keyword, value, this);
          addChild(decisionTable);
        }
        return decisionTable;
      } else if(KEY_SERVICE.equals(keyword)) {
        if(service == null) {
          service = new MutableYamlTaskBody(source, indent, keyword, value, this);
          addChild(service);
        }
        return service;
      } else if(KEY_RETURNS.equals(keyword)) {
        if(returns == null) {
          returns = new MutableYamlTaskBody(source, indent, keyword, value, this);
          addChild(returns);
        }
        return returns;
      } else if(KEY_ID.equals(keyword)) {
        if(VALUE_END.equalsIgnoreCase(value) || VALUE_NEXT.equalsIgnoreCase(value) ) {
          throw new AST_Exception(String.format("Value: %s is reserved and can't be used!", value));
        }
      }
      return super.addChild(source, indent, keyword, value);
    }
    @Override
    public Yaml getId() {
      return get(KEY_ID);
    }
    @Override
    public Yaml getThen() {
      return get(KEY_THEN);
    }
    @Override
    public Map<String, YamlSwitch> getSwitch() {
      return cases == null ? Collections.emptyMap() : cases.getValues();
    }
    @Override
    public YamlTaskBody getDecisionTable() {
      return decisionTable;
    }
    @Override
    public YamlTaskBody getService() {
      return service;
    }
    @Override
    public YamlTaskBody getUserTask() {
      return userTask;
    }
    @Override
    public YamlTaskBody getReturns() {
      return returns;
    }
    @Override
    public YamlTaskBody getRef() {
      if(userTask != null) {
        return userTask;
      } else if(service != null) {
        return service;
      } else if(returns != null) {
        return returns;
      }
      return decisionTable;
    }
    @Override
    public int getOrder() {
      return order;
    }
  }

  private static class MutableYamlTaskBody extends MutableYaml implements YamlTaskBody {

    private static final long serialVersionUID = -3601531710393434419L;

    public MutableYamlTaskBody(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
    }
    @Override
    public Yaml getRef() {
      return get(KEY_REF);
    }
    @Override
    public Yaml getCollection() {
      return get(KEY_COLLECTION);
    }
    @Override
    public Map<String, Yaml> getInputs() {
      Yaml inputs = getInputsNode();
      if(inputs == null) {
        return Collections.emptyMap();
      }
      return inputs.getChildren();
    }
    @Override
    public String getObjectInput() {
      Yaml inputs = getInputsNode();
      if(inputs == null) {
        return null;
      }
      if (inputs.getValue() == null) {
        return null;
      }
      if(inputs.getValue().isBlank() || inputs.getValue().equals("null")) {
        return null;
      }
      return inputs.getValue();
    }
    @Override
    public Yaml getInputsNode() {
      return get(KEY_INPUTS);
    }
  }
}
