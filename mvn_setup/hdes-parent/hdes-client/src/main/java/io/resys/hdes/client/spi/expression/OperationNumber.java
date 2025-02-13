package io.resys.hdes.client.spi.expression;

/*-
 * #%L
 * wrench-assets-dt
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

import java.math.BigDecimal;
import java.util.function.Consumer;

import io.resys.hdes.client.api.ast.TypeDef.ValueType;

public class OperationNumber {


  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public Operation<?> build(String value, ValueType valueType, Consumer<String> constants) {
      // Comparison
      if(!(value.startsWith("[") || value.startsWith("("))) {
        String number = parseNumber(value);
        constants.accept(number);
        if(value.startsWith("<=")) {
          return lteThen(valueType, number);  
        } else if(value.startsWith("<")) {
          return ltThen(valueType, number);
        } else if(value.startsWith(">=")) {
          return gteThen(valueType, number);
        } else if(value.startsWith(">")) {
          return gtThen(valueType, number);
        }
        return eq(valueType, number);
      }

      // Range
      String[] segments = value.substring(1, value.length()-1).split("\\..");
      String start = segments[0];
      String end = segments[1];
      boolean startInclude = value.startsWith("[");
      boolean endInclude = value.endsWith("]");

      constants.accept(start);
      constants.accept(end);

      return Operation.builder().and(
          startInclude ? gteThen(valueType, start) : gtThen(valueType, start),
          endInclude ? lteThen(valueType, end) : ltThen(valueType, end));
    } 
  }

  public static String parseNumber(String value) {
    try {
      String possibleNumber = null;
      if(value.startsWith("<=") || value.startsWith(">=")) {
        possibleNumber = value.substring(2);
      } else if(value.startsWith("<") || value.startsWith(">")) {
        possibleNumber = value.substring(1);
      } else if(value.startsWith("=")) {
        possibleNumber = value.substring(1);
      }
      if(possibleNumber.length() > 0) {
        return possibleNumber.trim();
      }
    } catch(Exception e) {
    }
    return null;
  }

  
  private static Operation<?> gtThen(ValueType valueType, String constant) {
    switch(valueType) {
    case DECIMAL: return gtThen(new BigDecimal(constant));
    case LONG: return gtThen(Long.parseLong(constant));
    case INTEGER: return gtThen(Integer.parseInt(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: %s", valueType));
    }
  }
  private static Operation<?> gteThen(ValueType valueType, String constant) {
    switch(valueType) {
    case DECIMAL: return gteThen(new BigDecimal(constant));
    case LONG: return gteThen(Long.parseLong(constant));
    case INTEGER: return gteThen(Integer.parseInt(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: %s", valueType));
    }
  }
  private static Operation<?> ltThen(ValueType valueType, String constant) {
    switch(valueType) {
    case DECIMAL: return ltThen(new BigDecimal(constant));
    case LONG: return ltThen(Long.parseLong(constant));
    case INTEGER: return ltThen(Integer.parseInt(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: %s", valueType));
    }   
  }
  private static Operation<?> lteThen(ValueType valueType, String constant) {
    switch(valueType) {
    case DECIMAL: return lteThen(new BigDecimal(constant));
    case LONG: return lteThen(Long.parseLong(constant));
    case INTEGER: return lteThen(Integer.parseInt(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: %s", valueType));
    }
  }
  private static Operation<?> eq(ValueType valueType, String constant) {
    switch(valueType) {
    case DECIMAL: return eq(new BigDecimal(constant));
    case LONG: return eq(Long.parseLong(constant));
    case INTEGER: return eq(Integer.parseInt(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: %s", valueType));
    }
  }


  private static Operation<Long> gtThen(long constant) {
    return (Long parameter) -> parameter > constant;      
  }
  private static Operation<Long> gteThen(long constant) {
    return (Long parameter) -> parameter >= constant;
  }
  private static Operation<Long> ltThen(long constant) {
    return (Long parameter) -> parameter < constant;      
  }
  private static Operation<Long> lteThen(long constant) {
    return (Long parameter) -> parameter <= constant;
  }
  private static Operation<Long> eq(long constant) {
    return (Long parameter) -> parameter == constant;
  }

  private static Operation<Integer> gtThen(int constant) {
    return (Integer parameter) -> parameter > constant;      
  }
  private static Operation<Integer> gteThen(int constant) {
    return (Integer parameter) -> parameter >= constant;
  }
  private static Operation<Integer> ltThen(int constant) {
    return (Integer parameter) -> parameter < constant;      
  }
  private static Operation<Integer> lteThen(int constant) {
    return (Integer parameter) -> parameter <= constant;
  }
  private static Operation<Integer> eq(int constant) {
    return (Integer parameter) -> parameter == constant;
  }

  private static Operation<BigDecimal> gtThen(BigDecimal constant) {
    return (BigDecimal parameter) -> parameter.compareTo(constant) > 0;      
  }
  private static Operation<BigDecimal> gteThen(BigDecimal constant) {
    return (BigDecimal parameter) -> parameter.compareTo(constant) >= 0;
  }
  private static Operation<BigDecimal> ltThen(BigDecimal constant) {
    return (BigDecimal parameter) -> parameter.compareTo(constant) < 0;      
  }
  private static Operation<BigDecimal> lteThen(BigDecimal constant) {
    return (BigDecimal parameter) -> parameter.compareTo(constant) <= 0;
  }
  private static Operation<BigDecimal> eq(BigDecimal constant) {
    return (BigDecimal parameter) -> parameter.compareTo(constant) == 0;
  }  
}
