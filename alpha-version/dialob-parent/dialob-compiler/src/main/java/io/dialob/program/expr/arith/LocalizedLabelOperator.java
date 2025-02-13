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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.dialob.executor.command.EventMatcher;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableValueSetId;
import io.dialob.executor.model.ItemId;
import io.dialob.program.EvalContext;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.model.Expression;
import io.dialob.program.model.Label;
import io.dialob.rule.parser.api.ValueType;


@Value.Immutable
public interface LocalizedLabelOperator extends Expression {

  Pattern EXPRESSION_PATTERN = Pattern.compile("\\{([\\w]*?)(:.*?)?}");

  static LocalizedLabelOperator createLocalizedLabelOperator(@Nonnull ProgramBuilder programBuilder, @Nonnull Label label) {
    Map<String, Expression> value = Maps.newHashMap();
    label.getLabels().forEach((key, labelString) -> {
      int i = 0;
      final Matcher matcher = EXPRESSION_PATTERN.matcher(labelString);
      final List<Expression> expressions = new ArrayList<>();
      while (matcher.find()) {
        expressions.add(ImmutableConstant.builder().value(labelString.substring(i, matcher.start())).valueType(ValueType.STRING).build());
        if (matcher.group(1) == null) {
          expressions.add(ImmutableConstant.builder().value(matcher.group(0)).valueType(ValueType.STRING).build());
        } else {
          String itemId = matcher.group(1);
          VariableReference variableReference = Operators.var(itemId, ValueType.STRING);
          String format = matcher.group(2);
          if (StringUtils.isNotBlank(format)) {
            // Drop leading ':'
            format = format.substring(1);
            switch (format) {
              case "key":
                expressions.add(ImmutableToStringOperator.of(variableReference));
                break;
              case "lowercase":
                expressions.add(ImmutableToLowerCaseOperator.lowerCaseOf(toStringExpression(programBuilder, IdUtils.toId(itemId), variableReference)));
                break;
              case "uppercase":
                expressions.add(ImmutableToUpperCaseOperator.upperCaseOf(toStringExpression(programBuilder, IdUtils.toId(itemId), variableReference)));
                break;
              default:
                expressions.add(ImmutableFormatOperator.of(variableReference, format));
            }
          } else {
            expressions.add(toStringExpression(programBuilder, IdUtils.toId(itemId), variableReference));
          }
        }
        i = matcher.end();
      }
      String ending = labelString.substring(i);
      if (StringUtils.isNotEmpty(ending)) {
        expressions.add(ImmutableConstant.builder().value(ending).valueType(ValueType.STRING).build());
      }
      if (!expressions.isEmpty()) {
        value.put(key, expressions.size() > 1 ? ImmutableConcatOperator.builder().addAllExpressions(expressions).build() : expressions.iterator().next());
      }
    });

    return ImmutableLocalizedLabelOperator.of(value);
  }

  static Expression toStringExpression(@Nonnull ProgramBuilder programBuilder, ItemId itemId, VariableReference variableReference) {
    return programBuilder.findValueSetIdForItem(itemId)
      .<Expression>map(valueSetId -> ImmutableValueSetEntryToStringOperator.of(ImmutableValueSetId.of(valueSetId), variableReference))
      .orElseGet(() -> ImmutableToStringOperator.of(variableReference));
  }

  @Nonnull
  @Value.Parameter
  Map<String, Expression> getValue();

  @Override
  default String eval(@Nonnull EvalContext evalContext) {
    Expression expression = getValue().get(evalContext.getLanguage());
    if (expression == null) {
      return null;
    }
    return (String) expression.eval(evalContext);
  }

  @Nonnull
  @Override
  default Set<EventMatcher> getEvalRequiredConditions() {
    return getValue()
      .values()
      .stream()
      .map(Expression::getEvalRequiredConditions)
      .reduce(Sets::union)
      .orElse(Collections.emptySet());
  }

  @Nonnull
  @Override
  default ValueType getValueType() {
    return ValueType.STRING;
  }

}
