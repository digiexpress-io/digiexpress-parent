package io.resys.limaone.ast;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableFlow_AST.class)
@JsonDeserialize(as = ImmutableFlow_AST.class)
public interface Flow_AST extends Simple_AST, Serializable {
  
  FlowRoot getRoot();

  @Value.Immutable
  interface FlowInputType extends Serializable {
    String getName();
    String getValue();
    @Nullable String getRef();
  }

  interface FlowRoot extends AnyFlowNode {
    AnyFlowNode getId();
    AnyFlowNode getDescription();
    Collection<FlowInputType> getTypes();
    Map<String, FlowInputNode> getInputs();
    Map<String, FlowTaskNode> getTasks();
  }

  interface FlowTaskNode extends AnyFlowNode {
    AnyFlowNode getId();
    int getOrder();
    AnyFlowNode getThen();
    FlowRefNode getRef();
    FlowRefNode getUserTask();
    FlowRefNode getDecisionTable();
    FlowRefNode getService();
    FlowRefNode getReturns();
    
    Map<String, FlowSwitchNode> getSwitch();
  }
  

  interface FlowRefNode extends AnyFlowNode {
    AnyFlowNode getRef();
    AnyFlowNode getCollection();
    AnyFlowNode getInputsNode();
    Map<String, AnyFlowNode> getInputs();
    String getObjectInput();
  }

  interface FlowSwitchNode extends AnyFlowNode {
    int getOrder();
    AnyFlowNode getWhen();
    AnyFlowNode getThen();
  }

  interface FlowInputNode extends AnyFlowNode {
    AnyFlowNode getRequired();
    AnyFlowNode getType();
    AnyFlowNode getDebugValue();
  }

  interface AnyFlowNode extends Serializable, Comparable<AnyFlowNode> {
    AnyFlowNode getParent();
    String getKeyword();
    Map<String, AnyFlowNode> getChildren();
    AnyFlowNode get(String name);
    String getValue();
    boolean hasNonNull(String name);
    int getStart();
    int getEnd();
  }
}
