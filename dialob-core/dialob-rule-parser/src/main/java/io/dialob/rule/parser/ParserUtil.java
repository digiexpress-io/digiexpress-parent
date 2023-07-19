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
package io.dialob.rule.parser;

import io.dialob.rule.parser.api.ValueType;
import org.jetbrains.annotations.NotNull;

public class ParserUtil {

  public static boolean isBuiltInFunction(@NotNull String function) {
    return "def".equals(function) || "ref".equals(function)
      || "isAnswered".equals(function)
      || "isNotAnswered".equals(function)
      || "in".equals(function)
      || "notIn".equals(function)
      || "matches".equals(function)
      || "notMatches".equals(function)
      || "not".equals(function)
      || "neg".equals(function)
      || isReducerOperator(function)
      ;
  }

  public static boolean isReducerOperator(@NotNull String reducer) {
    return "sumOf".equals(reducer)
      || "minOf".equals(reducer)
      || "maxOf".equals(reducer)
      || "allOf".equals(reducer)
      || "anyOf".equals(reducer);
  }

  public static ValueType getReducerOperatorReturnType(@NotNull String reducer, @NotNull ValueType inputType) {
    switch(reducer) {
      case "allOf":
      case "anyOf":
      case "sumOf":
      case "minOf":
      case "maxOf":
        return inputType;
    }
    return null;
  }

  public static ValueType itemTypeToValueType(@NotNull String itemType) {
    switch (itemType) {
      case "text":
      case "list":
      case "note":
      case "survey":
        return ValueType.STRING;
      case "boolean":
        return ValueType.BOOLEAN;
      case "date":
        return ValueType.DATE;
      case "time":
        return ValueType.TIME;
      case "number":
        return ValueType.INTEGER;
      case "decimal":
        return ValueType.DECIMAL;
      case "multichoice":
        return ValueType.arrayOf(ValueType.STRING);
      case "rowgroup":
        return ValueType.arrayOf(ValueType.INTEGER);
      case "questionnaire":
      case "context":
      case "variable":
      case "group":
      case "surveygroup":
      case "row":
        return null;
      default:
        throw new RuntimeException(String.format("Unsupported item type %s", itemType));
    }
  }

}
