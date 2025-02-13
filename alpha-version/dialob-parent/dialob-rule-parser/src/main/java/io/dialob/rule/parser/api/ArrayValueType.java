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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;

public class ArrayValueType implements ValueType {

  private final ValueType valueType;

  private static final ConcurrentMap<ValueType,ValueType> ARRAY_VALUE_TYPES = new ConcurrentHashMap<>();


  ArrayValueType(ValueType valueType) {
    Objects.requireNonNull(valueType, "valueType may not be null");
    this.valueType = valueType;
  }

  private static <T> Class<? extends T[]> getArrayClass(Class<T> clazz) {
    return (Class<? extends T[]>) Array.newInstance(clazz, 0).getClass();
  }

  static ValueType arrayOf(@NotNull ValueType valueType) {
    return ARRAY_VALUE_TYPES.computeIfAbsent(valueType, ArrayValueType::new);
  }


  @Override
  public <T> Comparator<T> getComparator() {
    return null;
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public Class<?> getTypeClass() {
    return getArrayClass(valueType.getTypeClass());
  }

  @Override
  public Object parseFromString(String string) {
    if (!string.startsWith("[") && string.endsWith("]")) {
      throw new RuntimeException("No an array.");
    }
    string = string.substring(1);
    string = string.substring(0,string.length() - 1);

    // TODO list of strings?
    String[] values = StringUtils.split(string,",");
    Object[] objects = (Object[]) Array.newInstance(valueType.getTypeClass(), values.length);
    for(int i = 0; i < values.length; i++) {
      objects[i] = valueType.parseFromString(values[i]);
    }
    return objects;

  }

  @Override
  public boolean isNegateable() {
    return false;
  }

  public Object negate(Object value) {
    throw new UnsupportedOperationException();
  }

  public Object not(Object value) {
    throw new UnsupportedOperationException();
  }

  public <T> BinaryOperator<T> sumOp() {
    throw new UnsupportedOperationException();
  }

  public <T> BinaryOperator<T> multOp() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ValueType plusType(ValueType rhs) {
    return null;
  }

  @Override
  public ValueType minusType(ValueType rhs) {
    return null;
  }

  @Override
  public ValueType multiplyType(ValueType rhs) {
    return null;
  }

  @Override
  public ValueType divideByType(ValueType rhs) {
    return null;
  }

  @Override
  public boolean canEqualWith(ValueType rhs) {
    return equals(rhs);
  }

  @Override
  public boolean canOrderWith(ValueType rhs) {
    return false;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public Object parseFromStringWithUnit(String value, String unit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return "[" + valueType.getName() + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj instanceof ArrayValueType) {
      ArrayValueType other = (ArrayValueType) obj;
      return other.valueType.equals(this.valueType);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return valueType.hashCode() * 7;
  }

  @Override
  public String toString() {
    return "ARRAY";
  }

  @Override
  public byte getTypeCode() {
    return (byte) (valueType.getTypeCode() | 0x80);
  }

  @Override
  public ValueType getItemValueType() {
    return valueType;
  }

  @Override
  public void writeTo(CodedOutputStream output, Object value) throws IOException {
    boolean present = value != null;
    output.writeBoolNoTag(present);
    if (present) {
      List list = (List) value;
      output.write(valueType.getTypeCode());
      output.writeInt32NoTag(list.size());
      for (Object item : list) {
        valueType.writeTo(output, item);
      }
    }
  }

  @Override
  public Object readFrom(CodedInputStream input) throws IOException {
    if (input.readBool()) {
      byte typeCode = input.readRawByte();
      int count = input.readInt32();
      ValueType valueType;
      if ((0x80 & typeCode) != 0) {
        typeCode = (byte) (typeCode & 0x7f);
        valueType = ValueType.arrayOf(PrimitiveValueType.values()[typeCode]);
      } else {
        valueType = PrimitiveValueType.values()[typeCode];
      }
      ImmutableList.Builder<Object> objectBuilder = ImmutableList.builder();

      for(int i = 0; i < count; i++) {
        objectBuilder.add(valueType.readFrom(input));
      }
      return objectBuilder.build();
    }
    return null;
  }
}
