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

import io.dialob.executor.command.EventMatcher;
import io.dialob.program.EvalContext;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.util.Set;

@Value.Immutable
@Value.Style(of="lowerCaseOf")
public interface ToLowerCaseOperator extends Expression {

  @Value.Parameter
  Expression getExpression();

  @Override
  default String eval(@Nonnull EvalContext context) {
    Object eval = getExpression().eval(context);
    if (eval == null) {
      return null;
    }
    return eval.toString().toLowerCase();
  }

  @Nonnull
  @Override
  default ValueType getValueType() {
    return ValueType.STRING;
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEvalRequiredConditions() {
    return getExpression().getEvalRequiredConditions();
  }

}
