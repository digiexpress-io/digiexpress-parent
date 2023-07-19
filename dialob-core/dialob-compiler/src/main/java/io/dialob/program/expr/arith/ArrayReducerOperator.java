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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

@Value.Immutable
public interface ArrayReducerOperator<T> extends Expression {


  @Value.Parameter
  BinaryOperator<T> getReducer();

  @Value.Parameter
  Expression getArrayExpression();

  @Value.Default
  @Nullable
  default Object getPlaceholderValue() {
    return null;
  }

  @Nullable
  @Override
  default Object eval(@NotNull EvalContext evalContext) {
    final List<T> values = (List<T>) getArrayExpression().eval(evalContext);
    return values
      .stream()
      .filter(Objects::nonNull)
      .reduce(getReducer())
      .orElse((T) getPlaceholderValue());
  }

  @NotNull
  @Override
  default ValueType getValueType() {
    return getArrayExpression().getValueType().getItemValueType();
  }

  default Set<EventMatcher> getEvalRequiredConditions() {
    return getArrayExpression().getEvalRequiredConditions();
  }

  BinaryOperator<Object> ANSWER_COUNT = (result, element) -> {
    if (element != null) {
      return result == null ? 1 : ((Integer)result) + 1;
    }
    return result;
  };
  BinaryOperator<Integer> INTEGER_SUM = (result, element) -> {
    if (element != null) {
      return result == null ? element : result + element;
    }
    return result;
  };
  BinaryOperator<BigDecimal> DECIMAL_SUM = (result, element) -> {
    if (element != null) {
      return result == null ? element : result.add(element);
    }
    return result;
  };
  BinaryOperator<Integer> INTEGER_MIN = (result, element) -> {
    if (element != null) {
      return result == null ? element : Math.min(result, element);
    }
    return result;
  };
  BinaryOperator<BigDecimal> DECIMAL_MIN = (result, element) -> {
    if (element != null) {
      return result == null ? element : result.min(element);
    }
    return result;
  };
  BinaryOperator<Integer> INTEGER_MAX = (result, element) -> {
    if (element != null) {
      return result == null ? element : Math.max(result, element);
    }
    return result;
  };
  BinaryOperator<BigDecimal> DECIMAL_MAX = (result, element) -> {
    if (element != null) {
      return result == null ? element : result.max(element);
    }
    return result;
  };

  BinaryOperator<Boolean> ANY = (result, element) -> {
    if (element != null) {
      return result == null ? element : result || element;
    }
    return result;
  };

  BinaryOperator<Boolean> ALL = (result, element) -> {
    if (element != null) {
      return result == null ? element : result && element;
    }
    return result;
  };



  static BinaryOperator<? extends Number> sumOp(ValueType valueType) {
    if (valueType == ValueType.INTEGER) {
      return INTEGER_SUM;
    }
    if (valueType == ValueType.DECIMAL) {
      return DECIMAL_SUM;
    }
    return null;
  }

  static BinaryOperator<? extends Number> minOp(ValueType valueType) {
    if (valueType == ValueType.INTEGER) {
      return INTEGER_MIN;
    }
    if (valueType == ValueType.DECIMAL) {
      return DECIMAL_MIN;
    }
    return null;
  }

  static BinaryOperator<? extends Number> maxOp(ValueType valueType) {
    if (valueType == ValueType.INTEGER) {
      return INTEGER_MAX;
    }
    if (valueType == ValueType.DECIMAL) {
      return DECIMAL_MAX;
    }
    return null;
  }


  static BinaryOperator<Boolean> allOp(ValueType valueType) {
    if (valueType == ValueType.BOOLEAN) {
      return ALL;
    }
    return null;
  }

  static BinaryOperator<Boolean> anyOp(ValueType valueType) {
    if (valueType == ValueType.BOOLEAN) {
      return ANY;
    }
    return null;
  }



}
