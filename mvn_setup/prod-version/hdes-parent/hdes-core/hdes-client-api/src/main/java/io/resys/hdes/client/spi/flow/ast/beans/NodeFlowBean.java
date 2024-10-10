package io.resys.hdes.client.spi.flow.ast.beans;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2024 Copyright 2020 ReSys OÜ
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


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.resys.hdes.client.api.ast.AstChangeset;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowInputNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowInputType;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowRefNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowSwitchNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowTaskNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowRoot;
import io.resys.hdes.client.api.exceptions.FlowAstException;

public class NodeFlowBean extends NodeBean implements AstFlowRoot {
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
  public static final String KEY_USER_TASK = "userTask";
  public static final String KEY_REF = "ref";
  public static final String KEY_COLLECTION = "collection";
  public static final String KEY_SERVICE = "service";
  public static final String VALUE_NEXT = "next";
  public static final String VALUE_END = "end";
  public static final String KEY_DEBUG_VALUE = "debugValue";


  private final Collection<AstFlowInputType> inputTypes;
  private NodeInputs inputs;
  private NodeTasks tasks;
  private String value;

  public NodeFlowBean(Collection<AstFlowInputType> inputTypes) {
    super(null, -2, null, null, null);
    this.inputTypes = inputTypes;
  }
  @Override
  public AstFlowNode getId() {
    return get(KEY_ID);
  }
  @Override
  public AstFlowNode getDescription() {
    return get(KEY_DESC);
  }
  @Override
  public Map<String, AstFlowInputNode> getInputs() {
    return inputs == null ? Collections.emptyMap() : inputs.getInputs();
  }
  @Override
  public Map<String, AstFlowTaskNode> getTasks() {
    return tasks == null ? Collections.emptyMap() : tasks.getTasks();
  }
  @Override
  public Collection<AstFlowInputType> getTypes() {
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
  public NodeBean addChild(AstChangeset source, int indent, String keyword, String value) {
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
    private final Map<String, AstFlowInputNode> inputs = new HashMap<>();
    public NodeInputs(AstChangeset source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }
    public Map<String, AstFlowInputNode> getInputs() {
      return Collections.unmodifiableMap(inputs);
    }
    @Override
    public NodeBean addChild(AstChangeset source, int indent, String keyword, String value) {
      NodeInputBean result = new NodeInputBean(source, indent, keyword, value, this);
      if(inputs.containsKey(result.getKeyword())) {
        String message = String.format("Duplicate input: %s!", result.getKeyword());
        throw new FlowAstException(message);
      }
      inputs.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class NodeTasks extends NodeBean {
    private static final long serialVersionUID = 2001644047832806256L;
    private final Map<String, AstFlowTaskNode> tasks = new HashMap<>();
    private int order = 0;
    public NodeTasks(AstChangeset source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }

    public Map<String, AstFlowTaskNode> getTasks() {
      return Collections.unmodifiableMap(tasks);
    }
    @Override
    public NodeBean addChild(AstChangeset source, int indent, String keyword, String value) {
      NodeTaskBean result = new NodeTaskBean(source, order++, indent, keyword, value, this);
      tasks.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class NodeInputBean extends NodeBean implements AstFlowInputNode {
    private static final long serialVersionUID = 8910489078429824772L;
    public NodeInputBean(AstChangeset source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }
    @Override
    public AstFlowNode getRequired() {
      return get(KEY_REQ);
    }

    @Override
    public AstFlowNode getType() {
      return get(KEY_TYPE);
    }
    @Override
    public AstFlowNode getDebugValue() {
      return get(KEY_DEBUG_VALUE);
    }
  }

  private static class NodeSwitchBean extends NodeBean implements AstFlowSwitchNode {
    private static final long serialVersionUID = 8910489078429824772L;
    private final int order;

    public NodeSwitchBean(AstChangeset source, int order, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
      this.order = order;
    }
    @Override
    public AstFlowNode getThen() {
      return get(KEY_THEN);
    }
    @Override
    public AstFlowNode getWhen() {
      return get(KEY_WHEN);
    }
    @Override
    public int getOrder() {
      return order;
    }
  }

  private static class NodeCasesBean extends NodeBean {
    private static final long serialVersionUID = 2001644047832806256L;
    private final Map<String, AstFlowSwitchNode> cases = new HashMap<>();
    private int order = 0;
    public NodeCasesBean(AstChangeset source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }

    public Map<String, AstFlowSwitchNode> getValues() {
      return Collections.unmodifiableMap(cases);
    }
    @Override
    public NodeBean addChild(AstChangeset source, int indent, String keyword, String value) {
      NodeSwitchBean result = new NodeSwitchBean(source, order++, indent, keyword, value, this);
      cases.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  private static class NodeTaskBean extends NodeBean implements AstFlowTaskNode {
    private static final long serialVersionUID = 8910489078429824772L;
    private final int order;
    private NodeRefBean decisionTable;
    private NodeRefBean userTask;
    private NodeRefBean service;
    private NodeCasesBean cases;

    public NodeTaskBean(AstChangeset source, int order, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
      this.order = order;
    }
    @Override
    public NodeBean addChild(AstChangeset source, int indent, String keyword, String value) {
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
      } else if(KEY_ID.equals(keyword)) {
        if(VALUE_END.equalsIgnoreCase(value) || VALUE_NEXT.equalsIgnoreCase(value) ) {
          throw new FlowAstException(String.format("Value: %s is reserved and can't be used!", value));
        }
      }
      return super.addChild(source, indent, keyword, value);
    }
    @Override
    public AstFlowNode getId() {
      return get(KEY_ID);
    }
    @Override
    public AstFlowNode getThen() {
      return get(KEY_THEN);
    }
    @Override
    public Map<String, AstFlowSwitchNode> getSwitch() {
      return cases == null ? Collections.emptyMap() : cases.getValues();
    }
    @Override
    public AstFlowRefNode getDecisionTable() {
      return decisionTable;
    }
    @Override
    public AstFlowRefNode getService() {
      return service;
    }
    @Override
    public AstFlowRefNode getUserTask() {
      return userTask;
    }
    @Override
    public AstFlowRefNode getRef() {
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

  private static class NodeRefBean extends NodeBean implements AstFlowRefNode {

    private static final long serialVersionUID = -3601531710393434419L;

    public NodeRefBean(AstChangeset source, int indent, String keyword, String value, NodeBean parent) {
      super(source, indent, keyword, value, parent);
    }
    @Override
    public AstFlowNode getRef() {
      return get(KEY_REF);
    }
    @Override
    public AstFlowNode getCollection() {
      return get(KEY_COLLECTION);
    }
    @Override
    public Map<String, AstFlowNode> getInputs() {
      AstFlowNode inputs = getInputsNode();
      if(inputs == null) {
        return Collections.emptyMap();
      }
      return inputs.getChildren();
    }
    @Override
    public String getObjectInput() {
      AstFlowNode inputs = getInputsNode();
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
    public AstFlowNode getInputsNode() {
      return get(KEY_INPUTS);
    }
  }
}
