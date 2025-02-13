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

import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;

public interface FlowNode extends HdesNode {
  
  @Value.Immutable
  interface FlowBody extends FlowNode, BodyNode { 
    Optional<Step> getStep();
  }
  
  @Value.Immutable
  interface Step extends FlowNode {
    SimpleInvocation getId();
    StepAction getAction();
    StepPointer getPointer();
    Boolean getAwait();
    Optional<StepAs> getAs();
  }
  
  @Value.Immutable
  interface StepAs extends FlowNode {
    ObjectMappingDef getMapping();
  }
  
  
  /*
   * Step actions iterator, call, suspend
   */
  interface StepAction extends FlowNode {}

  @Value.Immutable
  interface EmptyAction extends StepAction {
  }
  
  @Value.Immutable
  interface CallAction extends StepAction {
    List<CallDef> getCalls();
  }
  
  @Value.Immutable
  interface CallDef extends FlowNode {
    Optional<Integer> getIndex();
    SimpleInvocation getId();
    ObjectMappingDef getMapping();
    Boolean getAwait();
  }

  @Value.Immutable
  interface IterateAction extends StepAction {
    InvocationNode getOver();
    Optional<Step> getStep();
    Boolean getNested();
  }
  
  /**
   * Step pointer types: when/then, then, then end as
   */
  interface StepPointer extends FlowNode {}

  @Value.Immutable
  interface SplitPointer extends StepPointer {
    List<StepPointer> getValues();
  }

  @Value.Immutable
  interface WhenPointer extends StepPointer {
    ExpressionBody getWhen();
    StepPointer getThen();
  }
  
  @Value.Immutable
  interface ThenPointer extends StepPointer {
    SimpleInvocation getId();
    Step getStep();
  }
  
  @Value.Immutable
  interface EndPointer extends StepPointer {
    ObjectMappingDef getMapping();
  }

  @Value.Immutable
  interface IterationEndPointer extends StepPointer {
  }
  
  @Value.Immutable
  interface StepCallDef extends TypeDef {
    int getIndex();
    CallDef getCallDef();
    List<TypeDef> getValues();
  }
}
