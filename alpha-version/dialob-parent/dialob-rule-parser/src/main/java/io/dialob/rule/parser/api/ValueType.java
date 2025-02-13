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
package io.dialob.rule.parser.api;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.Comparator;
import java.util.function.BinaryOperator;

@SuppressWarnings("unchecked")
public interface ValueType extends Serializable {

  ValueType TIME = PrimitiveValueType.TIME;

  ValueType DATE = PrimitiveValueType.DATE;

  ValueType STRING = PrimitiveValueType.STRING;

  ValueType PERIOD = PrimitiveValueType.PERIOD;

  ValueType INTEGER = PrimitiveValueType.INTEGER;

  ValueType DECIMAL = PrimitiveValueType.DECIMAL;

  ValueType BOOLEAN = PrimitiveValueType.BOOLEAN;

  ValueType DURATION = PrimitiveValueType.DURATION;

  ValueType PERCENT = PrimitiveValueType.PERCENT;

  <T> Comparator<T> getComparator();

  Class<?> getTypeClass();

  Object parseFromString(String string);

  boolean isNegateable();

  Object negate(Object value);

  Object not(Object value);

  <T> BinaryOperator<T> sumOp();

  <T> BinaryOperator<T> multOp();

  ValueType plusType(ValueType rhs);

  ValueType minusType(ValueType rhs);

  ValueType multiplyType(ValueType rhs);

  ValueType divideByType(ValueType rhs);

  boolean canEqualWith(ValueType rhs);

  boolean canOrderWith(ValueType rhs);

  Object parseFromStringWithUnit(String value, String unit);

  String getName();

  default boolean isArray() {
    return false;
  }

  boolean isPrimitive();

  static ValueType arrayOf(@NotNull ValueType valueType) {
    return ArrayValueType.arrayOf(valueType);
  }

  @Nullable
  static ValueType valueTypeOf(final @NotNull Class<?> returnType) {
    if (returnType == String.class) {
      return STRING;
    }
    if (returnType == LocalDate.class) {
      return DATE;
    }
    if (returnType == LocalTime.class) {
      return TIME;
    }
    if (returnType == Period.class) {
      return PERIOD;
    }
    if (returnType == Integer.class || returnType == int.class ) {
      return INTEGER;
    }
    if (returnType == BigDecimal.class) {
      return DECIMAL;
    }
    if (returnType == Boolean.class || returnType == boolean.class) {
      return BOOLEAN;
    }
    if (returnType == Duration.class) {
      return DURATION;
    }
    if (returnType.isArray()) {
      ValueType valueType = valueTypeOf(returnType.getComponentType());
      if (valueType == null) {
        throw new RuntimeException("Cannot find ValueType of type " + returnType.getComponentType());
      }
      return arrayOf(valueType);
    }
    return null;
  }

  default Object coerseFrom(Object value) {
    if (getTypeClass().isAssignableFrom(value.getClass())) {
      return value;
    }
    return null;
  }

  byte getTypeCode();

  default ValueType getItemValueType() {
    throw new IllegalStateException("Not an array value type");
  }

  void writeTo(CodedOutputStream output, Object value) throws IOException;

  Object readFrom(CodedInputStream input) throws IOException;
}
