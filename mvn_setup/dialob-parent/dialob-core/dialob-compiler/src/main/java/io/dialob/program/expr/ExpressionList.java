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
package io.dialob.program.expr;

import com.google.common.collect.ImmutableSet;

import io.dialob.executor.command.EventMatcher;
import io.dialob.program.EvalContext;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.ValueType;

import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public interface ExpressionList extends Expression {

  List<Expression> getExpressions();

  @Override
  default Object eval(@Nonnull EvalContext evalContext) {
    return getExpressions().stream()
      .map(expression -> expression.eval(evalContext))
      .filter(Objects::nonNull)
      .flatMap(o -> {
        if (o instanceof Collection) {
          return ((Collection)o).stream();
        }
        return Stream.of(o);
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  @Nonnull
  @Override
  default ValueType getValueType() {
    return ValueType.arrayOf(ValueType.STRING);
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEvalRequiredConditions() {
    final ImmutableSet.Builder<EventMatcher> deps = ImmutableSet.builder();
    getExpressions().forEach(arg -> deps.addAll(arg.getEvalRequiredConditions()));
    return deps.build();
  }

}
