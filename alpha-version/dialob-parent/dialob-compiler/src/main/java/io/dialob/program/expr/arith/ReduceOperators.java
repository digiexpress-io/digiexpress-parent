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
package io.dialob.program.expr.arith;

import io.dialob.executor.model.ItemId;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

public interface ReduceOperators {

  static ItemId extractPrototypeId(Expression expression) {
    if (expression instanceof VariableReference) {
      VariableReference variableReference = (VariableReference) expression;
      return variableReference.getItemId();
    }
    throw new IllegalStateException("Only id expressions supported for now");
  }


  static Expression sumOf(Expression expression) {
    ItemId protoTypeId = extractPrototypeId(expression);
    ValueType valueType = expression.getValueType();
    return ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.sumOp(valueType),
      ImmutableCollectRowFieldsOperator.of(protoTypeId, valueType)
    );
  }

  static Expression minOf(Expression expression) {
    ItemId protoTypeId = extractPrototypeId(expression);
    ValueType valueType = expression.getValueType();
    return ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.minOp(valueType),
      ImmutableCollectRowFieldsOperator.of(protoTypeId, valueType)
    );
  }

  static Expression maxOf(Expression expression) {
    ItemId protoTypeId = extractPrototypeId(expression);
    ValueType valueType = expression.getValueType();
    return ImmutableArrayReducerOperator.of(
      ArrayReducerOperator.maxOp(valueType),
      ImmutableCollectRowFieldsOperator.of(protoTypeId, valueType)
    );
  }


}
