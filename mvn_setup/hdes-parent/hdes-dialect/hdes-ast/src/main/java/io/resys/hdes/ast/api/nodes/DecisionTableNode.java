package io.resys.hdes.ast.api.nodes;

import java.util.List;

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

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;

public interface DecisionTableNode extends HdesNode {
  
  interface HitPolicy extends DecisionTableNode {}
  
  @Value.Immutable
  interface DecisionTableBody extends DecisionTableNode, BodyNode {
    HitPolicy getHitPolicy();
    ObjectDef getConstants();
    ObjectDef getMatched();
  }
  
  @Value.Immutable
  interface HitPolicyFirst extends HitPolicy {
    List<RuleRow> getRows();
  }
 
  @Value.Immutable
  interface HitPolicyAll extends HitPolicy {
    List<RuleRow> getRows();
  }

  @Value.Immutable
  interface RuleRow extends DecisionTableNode {
    WhenRuleRow getWhen();
    ThenRuleRow getThen();
    String getText();
  }
  
  @Value.Immutable
  interface WhenRuleRow extends DecisionTableNode {
    List<ExpressionBody> getValues();
  }

  @Value.Immutable
  interface ThenRuleRow extends DecisionTableNode {
    List<Literal> getValues();
  }
  
  @Value.Immutable
  interface HitPolicyMapping extends HitPolicy {
    ScalarType getDefFrom();
    ScalarType getDefTo();
    
    WhenRuleRow getWhen();
    List<MappingRow> getMapsTo();
  }

  @Value.Immutable
  interface MappingRow extends DecisionTableNode {
    SimpleInvocation getAccepts();
    ThenRuleRow getThen();
  }
}