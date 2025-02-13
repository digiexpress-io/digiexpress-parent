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

import com.google.common.collect.Sets;

import io.dialob.executor.command.EventMatcher;
import io.dialob.program.EvalContext;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public interface ConcatOperator extends Expression {

  List<Expression> getExpressions();

  @Override
  default String eval(@Nonnull EvalContext context) {
    return getExpressions().stream().map(expression -> (String) expression.eval(context)).collect(Collectors.joining());
  }

  @Nonnull
  @Override
  default ValueType getValueType() {
    return ValueType.STRING;
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEvalRequiredConditions() {
    return getExpressions().stream().map(Expression::getEvalRequiredConditions).reduce(Sets::union).orElse(Collections.emptySet());
  }

}
