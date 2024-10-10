/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.rule.parser.analyze;

import io.dialob.rule.parser.AstMatcher;
import io.dialob.rule.parser.api.CompilerErrorCode;
import io.dialob.rule.parser.api.ImmutableRuleExpressionCompilerError;
import io.dialob.rule.parser.api.RuleExpressionCompilerError;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.node.NodeOperator;

import java.util.ArrayList;
import java.util.List;

public class ValidateExpressionVisitor extends AstMatcher {

  private List<RuleExpressionCompilerError> errors = new ArrayList<>();

  public ValidateExpressionVisitor() {
    whenMatches(parent(isNull()).and(idNode().or(constNode().and(valueType(not(is(ValueType.BOOLEAN)))))), node -> {
      errors.add(ImmutableRuleExpressionCompilerError.builder().errorCode(CompilerErrorCode.INCOMPLETE_EXPRESSION).span(node.getSpan()).build());
      return node;
    });

    whenMatches(parent(isNull())
        .and(callNode(operCategory(not(is(NodeOperator.Category.RELATION)))))
        .and(callNode(operCategory(not(is(NodeOperator.Category.LOGICAL)))))
        .and(callNode(operCategory(not(is(NodeOperator.Category.FUNCTION)))).or(valueType(not(is(ValueType.BOOLEAN))))),
      node -> {
        errors.add(ImmutableRuleExpressionCompilerError.builder().errorCode(CompilerErrorCode.INCOMPLETE_EXPRESSION).span(node.getSpan()).build());
        return node;
      });
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public List<RuleExpressionCompilerError> getErrors() {
    return errors;
  }
}
