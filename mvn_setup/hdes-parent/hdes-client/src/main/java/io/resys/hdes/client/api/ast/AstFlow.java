package io.resys.hdes.client.api.ast;

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
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@Value.Immutable
@JsonSerialize(as = ImmutableAstFlow.class)
@JsonDeserialize(as = ImmutableAstFlow.class)
public interface AstFlow extends AstBody, Serializable {
  
  AstFlowRoot getSrc();
  List<FlowAstAutocomplete> getAutocomplete();

  @Value.Immutable
  interface FlowAstAutocomplete extends Serializable {
    String getId();
    List<AstCommandRange> getRange();
    List<String> getValue();
  }


  //v.name(), null, v.name()
  //public ImmutableNodeInputType(String name, String ref, String value) {
  
  @Value.Immutable
  interface AstFlowInputType extends Serializable {
    String getName();
    String getValue();
    @Nullable
    String getRef();
  }

  interface AstFlowRoot extends AstFlowNode {
    AstFlowNode getId();
    AstFlowNode getDescription();
    Collection<AstFlowInputType> getTypes();
    Map<String, AstFlowInputNode> getInputs();
    Map<String, AstFlowTaskNode> getTasks();
  }

  interface AstFlowTaskNode extends AstFlowNode {
    AstFlowNode getId();
    int getOrder();
    AstFlowNode getThen();
    AstFlowRefNode getRef();
    AstFlowRefNode getUserTask();
    AstFlowRefNode getDecisionTable();
    AstFlowRefNode getService();
    Map<String, AstFlowSwitchNode> getSwitch();
  }

  interface AstFlowRefNode extends AstFlowNode {
    AstFlowNode getRef();
    AstFlowNode getCollection();
    AstFlowNode getInputsNode();
    Map<String, AstFlowNode> getInputs();
    String getObjectInput();
  }

  interface AstFlowSwitchNode extends AstFlowNode {
    int getOrder();
    AstFlowNode getWhen();
    AstFlowNode getThen();
  }

  interface AstFlowInputNode extends AstFlowNode {
    AstFlowNode getRequired();
    AstFlowNode getType();
    AstFlowNode getDebugValue();
  }

  interface AstFlowNode extends Serializable, Comparable<AstFlowNode> {
    AstFlowNode getParent();
    String getKeyword();
    Map<String, AstFlowNode> getChildren();
    AstFlowNode get(String name);
    String getValue();
    AstChangeset getSource();
    boolean hasNonNull(String name);
    int getStart();
    int getEnd();
  }
}
