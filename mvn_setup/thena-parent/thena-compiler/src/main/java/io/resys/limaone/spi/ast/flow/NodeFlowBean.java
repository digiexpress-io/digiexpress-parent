package io.resys.limaone.spi.ast.flow;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.resys.limaone.ast.Flow_AST.FlowInputNode;
import io.resys.limaone.ast.Flow_AST.FlowInputType;
import io.resys.limaone.ast.Flow_AST.AnyFlowNode;
import io.resys.limaone.ast.Flow_AST.FlowRefNode;
import io.resys.limaone.ast.Flow_AST.FlowRoot;
import io.resys.limaone.ast.Flow_AST.FlowSwitchNode;
import io.resys.limaone.ast.Flow_AST.FlowTaskNode;
import io.resys.limaone.spi.ast.AST_Exception;



public class NodeFlowBean extends NodeBean implements FlowRoot {
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


  private final Collection<FlowInputType> inputTypes;
  private NodeInputs inputs;
  private NodeTasks tasks;
  private String value;

  public NodeFlowBean(Collection<FlowInputType> inputTypes) {
    super(null, -2, null, null, null);
    this.inputTypes = inputTypes;
  }
  @Override
  public AnyFlowNode getId() {
    return get(KEY_ID);
  }
  @Override
  public AnyFlowNode getDescription() {
    return get(KEY_DESC);
  }
  @Override
  public Map<String, FlowInputNode> getInputs() {
    return inputs == null ? Collections.emptyMap() : inputs.getInputs();
  }
  @Override
  public Map<String, FlowTaskNode> getTasks() {
    return tasks == null ? Collections.emptyMap() : tasks.getTasks();
  }
  @Override
  public Collection<FlowInputType> getTypes() {
    return inputTypes;
  }
  @Override
  public String getValue() {
    return value;
  }
  public NodeFlowBean setValue(String value) {
    this.value = value;
    return this;
  }
  @Override
  public NodeFlowBean setEnd(int value) {
    super.setEnd(value);
    return this;
  }
  @Override
  public NodeBean addChild(NodeSource source, int indent, String keyword, String value) {
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

  private static class NodeInputs extends NodeBean {
    private static final long serialVersionUID = 8989618439864849749L;
    private final Map<String, FlowInputNode> inputs = new HashMap<>();
    public NodeInputs(NodeSource source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }
    public Map<String, FlowInputNode> getInputs() {
      return Collections.unmodifiableMap(inputs);
    }
    @Override
    public NodeBean addChild(NodeSource source, int indent, String keyword, String value) {
      NodeInputBean result = new NodeInputBean(source, indent, keyword, value, this);
      if(inputs.containsKey(result.getKeyword())) {
        String message = String.format("Duplicate input: %s!", result.getKeyword());
        throw new AST_Exception(message);
      }
      inputs.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class NodeTasks extends NodeBean {
    private static final long serialVersionUID = 2001644047832806256L;
    private final Map<String, FlowTaskNode> tasks = new HashMap<>();
    private int order = 0;
    public NodeTasks(NodeSource source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }

    public Map<String, FlowTaskNode> getTasks() {
      return Collections.unmodifiableMap(tasks);
    }
    @Override
    public NodeBean addChild(NodeSource source, int indent, String keyword, String value) {
      NodeTaskBean result = new NodeTaskBean(source, order++, indent, keyword, value, this);
      tasks.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class NodeInputBean extends NodeBean implements FlowInputNode {
    private static final long serialVersionUID = 8910489078429824772L;
    public NodeInputBean(NodeSource source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }
    @Override
    public AnyFlowNode getRequired() {
      return get(KEY_REQ);
    }

    @Override
    public AnyFlowNode getType() {
      return get(KEY_TYPE);
    }
    @Override
    public AnyFlowNode getDebugValue() {
      return get(KEY_DEBUG_VALUE);
    }
  }

  private static class NodeSwitchBean extends NodeBean implements FlowSwitchNode {
    private static final long serialVersionUID = 8910489078429824772L;
    private final int order;

    public NodeSwitchBean(NodeSource source, int order, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
      this.order = order;
    }
    @Override
    public AnyFlowNode getThen() {
      return get(KEY_THEN);
    }
    @Override
    public AnyFlowNode getWhen() {
      return get(KEY_WHEN);
    }
    @Override
    public int getOrder() {
      return order;
    }
  }

  private static class NodeCasesBean extends NodeBean {
    private static final long serialVersionUID = 2001644047832806256L;
    private final Map<String, FlowSwitchNode> cases = new HashMap<>();
    private int order = 0;
    public NodeCasesBean(NodeSource source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }

    public Map<String, FlowSwitchNode> getValues() {
      return Collections.unmodifiableMap(cases);
    }
    @Override
    public NodeBean addChild(NodeSource source, int indent, String keyword, String value) {
      NodeSwitchBean result = new NodeSwitchBean(source, order++, indent, keyword, value, this);
      cases.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class NodeTaskBean extends NodeBean implements FlowTaskNode {
    private static final long serialVersionUID = 8910489078429824772L;
    private final int order;
    private NodeRefBean decisionTable;
    private NodeRefBean userTask;
    private NodeRefBean service;
    private NodeRefBean returns;
    private NodeCasesBean cases;

    public NodeTaskBean(NodeSource source, int order, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
      this.order = order;
    }
    @Override
    public NodeBean addChild(NodeSource source, int indent, String keyword, String value) {
      if(KEY_SWITCH.equals(keyword)) {
        if(cases == null) {
          cases = new NodeCasesBean(source, indent, keyword, value, this);
          addChild(cases);
        }
        return cases;

      } else if(KEY_USER_TASK.equals(keyword)) {
        if(userTask == null) {
          userTask = new NodeRefBean(source, indent, keyword, value, this);
          addChild(userTask);
        }
        return userTask;
      } else if(KEY_DT.equals(keyword)) {
        if(decisionTable == null) {
          decisionTable = new NodeRefBean(source, indent, keyword, value, this);
          addChild(decisionTable);
        }
        return decisionTable;
      } else if(KEY_SERVICE.equals(keyword)) {
        if(service == null) {
          service = new NodeRefBean(source, indent, keyword, value, this);
          addChild(service);
        }
        return service;
      } else if(KEY_RETURNS.equals(keyword)) {
        if(returns == null) {
          returns = new NodeRefBean(source, indent, keyword, value, this);
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
    public AnyFlowNode getId() {
      return get(KEY_ID);
    }
    @Override
    public AnyFlowNode getThen() {
      return get(KEY_THEN);
    }
    @Override
    public Map<String, FlowSwitchNode> getSwitch() {
      return cases == null ? Collections.emptyMap() : cases.getValues();
    }
    @Override
    public FlowRefNode getDecisionTable() {
      return decisionTable;
    }
    @Override
    public FlowRefNode getService() {
      return service;
    }
    @Override
    public FlowRefNode getUserTask() {
      return userTask;
    }
    @Override
    public FlowRefNode getReturns() {
      return returns;
    }
    @Override
    public FlowRefNode getRef() {
      if(userTask != null) {
        return userTask;
      } else if(service != null) {
        return service;
      }
      return decisionTable;
    }
    @Override
    public int getOrder() {
      return order;
    }
  }

  private static class NodeRefBean extends NodeBean implements FlowRefNode {

    private static final long serialVersionUID = -3601531710393434419L;

    public NodeRefBean(NodeSource source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }
    @Override
    public AnyFlowNode getRef() {
      return get(KEY_REF);
    }
    @Override
    public AnyFlowNode getCollection() {
      return get(KEY_COLLECTION);
    }
    @Override
    public Map<String, AnyFlowNode> getInputs() {
      AnyFlowNode inputs = getInputsNode();
      if(inputs == null) {
        return Collections.emptyMap();
      }
      return inputs.getChildren();
    }
    @Override
    public String getObjectInput() {
      AnyFlowNode inputs = getInputsNode();
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
    public AnyFlowNode getInputsNode() {
      return get(KEY_INPUTS);
    }
  }
}
