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

import javax.annotation.Nullable;

public enum OperatorSymbol {
  PLUS("+"),
  MINUS("-"),
  MULT("*"),
  DIV("/"),
  NEG("neg"),
  NOT("not", true),
  AND("and"),
  OR("or"),
  NE("!="),
  EQ("="),
  LT("<"),
  LE("<="),
  GE(">="),
  GT(">"),
  NOT_IN("notIn", true),
  IN("in"),
  NOT_MATCHES("notMatches", true),
  MATCHES("matches"),
  NOT_ANSWERED("isNotAnswered", true),
  ANSWERED("isAnswered"),
  NOT_BLANK("isNotBlank", true),
  BLANK("isBlank"),
  NOT_NULL("isNotNull", true),
  NULL("isNull"),
  COUNT("count"),
  NOT_VALID("isNotValid", true),
  VALID("isValid"),
  SUM("sumOf"),
  MIN("minOf"),
  MAX("maxOf"),
  ALL("allOf"),
  ANY("anyOf")
  ;

  @Nullable
  public static OperatorSymbol mapOp(String op) {
    for (OperatorSymbol operator : values()) {
      if (operator.symbol.equals(op)) {
        return operator;
      }
    }
    return null;
  }

  private final String symbol;

  private final boolean not;

  OperatorSymbol(String symbol) {
    this(symbol, false);
  }

  OperatorSymbol(String symbol, boolean not) {
    this.symbol = symbol;
    this.not = not;
  }

  public boolean isNot() {
    return not;
  }
}
