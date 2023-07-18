package io.resys.hdes.ast.api.visitors;

import io.resys.hdes.ast.api.nodes.HdesTree;

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

import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMapping;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MappingRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ThenRuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.WhenRuleRow;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;

public interface DecisionTableVisitor<T, R> {
  R visitBody(DecisionTableTree ctx);
  
  T visitHeaders(Headers node, HdesTree ctx);
  T visitHeader(TypeDef node, HdesTree ctx);
  T visitHeader(ScalarDef node, HdesTree ctx);
  T visitHeader(ObjectDef node, HdesTree ctx);
  T visitFormula(ScalarDef node, HdesTree ctx);
  
  T visitHitPolicy(HitPolicy node, HdesTree ctx);
  T visitHitPolicyAll(HitPolicyAll node, HdesTree ctx);
  T visitHitPolicyMapping(HitPolicyMapping node, HdesTree ctx);
  T visitHitPolicyFirst(HitPolicyFirst node, HdesTree ctx);
  T visitRuleRow(RuleRow node, HdesTree ctx);
  T visitWhenRuleRow(WhenRuleRow node, HdesTree ctx);
  T visitThenRuleRow(ThenRuleRow node, HdesTree ctx);
  T visitMappingRow(MappingRow node, HdesTree ctx);
}
