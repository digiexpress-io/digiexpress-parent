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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

import io.resys.hdes.client.api.ast.TypeDef.ValueType;

public class OperationDate {
  private final static String AFTER = "after";
  private final static String BEFORE = "before";
  private final static String BETWEEN = "between";
  private final static String EQUALS = "equals";
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public Operation<?> build(String value, ValueType valueType, Consumer<String> constants) {
      if(value.startsWith(BETWEEN)) {
        String[] segments = value.substring(7).split("and");

        String start = segments[0].trim();
        String end = segments[1].trim();
        constants.accept(start);
        constants.accept(end);

        return Operation.builder().and(
            Operation.builder().not(isBefore(valueType, start)),
            Operation.builder().not(isAfter(valueType, end)));
      }

      if(value.startsWith(EQUALS)) {
        String date = value.substring(EQUALS.length()).trim();
        constants.accept(date);
        return isEqual(valueType, date);
      } else if(value.startsWith(BEFORE)) {
        String date = value.substring(BEFORE.length()).trim();
        constants.accept(date);
        return isBefore(valueType, date);
      } else if(value.startsWith(AFTER)) {
        String date = value.substring(AFTER.length()).trim();
        constants.accept(date);
        return isAfter(valueType, date);
      } else if(value.trim().isEmpty()) {
        return isEmpty(valueType, value);
      }
      throw new IllegalArgumentException(String.format("unknown value type: %s, '%s'", valueType, value));
    }
  }

  private static Operation<?> isEmpty(ValueType valueType, String constant) {
    switch(valueType) {
    case DATE_TIME: return (LocalDateTime parameter) -> true;
    case DATE: return (LocalDate parameter) -> true;
    default: throw new IllegalArgumentException(String.format("unknown value type: {} '{}'", valueType, constant));
    }
  }
  
  private static Operation<?> isEqual(ValueType valueType, String constant) {
    switch(valueType) {
    case DATE_TIME: return isEqual(parseLocalDateTime(constant));
    case DATE: return isEqual(parseLocalDate(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: {} '{}'", valueType, constant));
    }
  }
  
  private static Operation<LocalDateTime> isEqual(LocalDateTime constant) {
    return (LocalDateTime parameter) -> parameter.isEqual(constant);
  }
  private static Operation<LocalDate> isEqual(LocalDate constant) {
    return (LocalDate parameter) -> parameter.isEqual(constant);
  }
  
  
  private static Operation<?> isBefore(ValueType valueType, String constant) {
    switch(valueType) {
    case DATE_TIME: return isBefore(parseLocalDateTime(constant));
    case DATE: return isBefore(parseLocalDate(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: {} '{}'", valueType, constant));
    }
  }
  private static Operation<LocalDateTime> isBefore(LocalDateTime constant) {
    return (LocalDateTime parameter) -> parameter.isBefore(constant);
  }
  private static Operation<LocalDate> isBefore(LocalDate constant) {
    return (LocalDate parameter) -> parameter.isBefore(constant);
  }
  
  private static Operation<?> isAfter(ValueType valueType, String constant) {
    switch(valueType) {
    case DATE_TIME: return isAfter(parseLocalDateTime(constant));
    case DATE: return isAfter(parseLocalDate(constant));
    default: throw new IllegalArgumentException(String.format("unknown value type: {} '{}'", valueType, constant));
    }
  }
  private static Operation<LocalDateTime> isAfter(LocalDateTime constant) {
    return (LocalDateTime parameter) -> parameter.isAfter(constant);
  }
  private static Operation<LocalDate> isAfter(LocalDate constant) {
    return (LocalDate parameter) -> parameter.isAfter(constant);
  }


  public static LocalDateTime parseLocalDateTime(String date) {
    try {
      return LocalDateTime.ofInstant(ZonedDateTime.parse(date).toInstant(), ZoneId.systemDefault());
    } catch(Exception e) {
      throw new IllegalArgumentException("Incorrect date time: '" + date + "', correct format: YYYY-MM-DDThh:mm:ssTZD, example: 2017-07-03T00:00:00Z!");
    }
  }

  public static LocalDate parseLocalDate(String date) {
    try {
      if(date.length() > 10) {
        return LocalDate.parse(date.substring(0, 10));
      }
      return LocalDate.parse(date);
    } catch(Exception e) {
      throw new IllegalArgumentException("Incorrect date: '" + date + "', correct format: YYYY-MM-DD, example: 2017-07-03!");
    }
  }
}
