package io.resys.hdes.ast.api.nodes;

/*-
 * #%L
 * hdes-ast
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;

  
public interface HdesTree {
  RootNode getRoot();
  HdesNode getValue();
  Optional<HdesTree> getParent();

  // creates new context with this as parent
  HdesTree next(HdesNode next);
  
  // tree related operations
  NodeGetQuery get();
  NodeFindQuery find();
  
  // step related operations 
  StepQuery step();
  
  // type registry
  TypeDefAnyQuery any();
  TypeDefReturnsQuery returns();

  
  interface RootTree extends HdesTree {
    RootNode getValue();
  }

  interface DecisionTableTree extends HdesTree {
    DecisionTableBody getValue();
  }

  interface FlowTree extends HdesTree {
    FlowBody getValue();
  }

  interface ServiceTree extends HdesTree {
    ServiceBody getValue();
  }
  
  interface StepQuery {
    List<InvocationNode> getWakeUps(Optional<Step> start);
    ObjectDef getDef(Step target); // find def for the step
    Optional<ObjectDef> getDefAs(Step target); // find def for the step
    Optional<Step> findStep(String id, Optional<Step> children); // get step with id from the children
    Optional<ObjectDef> findEnd(Optional<Step> step); // find end-as from the given steps
  }
  
  interface NodeGetQuery {
    // get the closest body
    BodyNode body();
    String bodyId();
    <T extends HdesNode> HdesTree ctx(Class<T> type);
    <T extends HdesNode> T node(Class<T> type);
  }
  
  interface NodeFindQuery {
    // get the closest body
    <T extends HdesNode> Optional<HdesTree> ctx(Class<T> type);
    
    <T extends HdesNode> NodeFindQuery limit(Class<T> type);
    <T extends HdesNode> Optional<T> node(Class<T> type);
  }
  
  interface TypeDefAnyQuery {
    TypeDef build(InvocationNode name);
  }
  
  interface TypeDefReturnsQuery {
    TypeDefReturns build(HdesNode src);
  }
  
  @Value.Immutable
  interface TypeDefReturns {
    List<TypeDefAccepts> getAccepts();
    TypeDef getReturns();
  }
  
  @Value.Immutable
  interface TypeDefAccepts {
    TypeDef getNode();
    InvocationNode getInvocation();
  }
}
