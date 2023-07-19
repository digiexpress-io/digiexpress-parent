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

import com.google.common.collect.Maps;
import io.dialob.rule.parser.api.RuleExpressionCompilerError;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableFinder;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ExpressionTest {

  @Test
  public void testRenameId() {
    assertEquals("b", Expression.createExpression("a").renameId("a", "b").toString());
    assertEquals("1 + r", Expression.createExpression("1 + a").renameId("a", "r").toString());
    assertEquals("1 + abc - 5", Expression.createExpression("1 + f - 5").renameId("f", "abc").toString());
    assertEquals("1 + abc - abc", Expression.createExpression("1 + f - f").renameId("f", "abc").toString());
    assertEquals("1 + f - f", Expression.createExpression("1 + abc - abc").renameId("abc", "f").toString());
    assertEquals("1 + a - ab >= abc", Expression.createExpression("1 + a - f >= abc").renameId("f", "ab").toString());
    assertEquals("1 + a - f >= abc", Expression.createExpression("1 + a - ab >= abc").renameId("ab", "f").toString());
    assertEquals("1 + a - f >=    abc ", Expression.createExpression("1 + a - ab >=    abc ").renameId("ab", "f").toString());
    assertEquals("1 + a - \"ab\" >=    abc ", Expression.createExpression("1 + a - \"ab\" >=    abc ").renameId("ab", "f").toString());
    assertEquals("abc + ab + abc + ab +abc", Expression.createExpression("a + ab + abc + ab +a").renameId("a", "abc").toString());
    assertEquals("a + ab + a + ab +a", Expression.createExpression("a + ab + abc + ab +a").renameId("abc", "a").toString());
  }

  @Test
  public void testIdCollecting() {
    assertThat(Expression.createExpression("a").getAllIds()).containsExactly("a");
    assertThat(Expression.createExpression("1 + r").getAllIds()).containsExactly("r");
    assertThat(Expression.createExpression("a + b").getAllIds()).containsExactly("a", "b");
    assertThat(Expression.createExpression("a + ab + a + ab +a").getAllIds()).containsExactly("a", "ab");
  }

  @Test
  public void shouldEmptyStringGeneratesEmptyAst() {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "");
    assertEquals(0, expression.getErrors().size());
    assertNull(expression.getAst());
  }

  @Test
  public void shouldBlankInputGeneratesEmptyAst() {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "   ");
    assertEquals(0, expression.getErrors().size());
    assertNull(expression.getAst());
  }

  @Test
  public void shouldReportSyntaxError() {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "a a");
    assertEquals(1, expression.getErrors().size());
    assertThat(expression.getErrors())
      .extracting(RuleExpressionCompilerError::getErrorCode)
      .containsOnly("SYNTAX_ERROR");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").containsOnly(2);
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").containsOnly(2);
    assertNull(expression.getAst());
  }

  @Test
  public void cannotSumBooleanAndInteger() {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "1+true");
    assertEquals(1, expression.getErrors().size());
    assertThat(expression.getErrors())
      .extracting(RuleExpressionCompilerError::getErrorCode)
      .containsOnly("CANNOT_ADD_TYPES");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").containsOnly(0);
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").containsOnly(5);
  }

  @Test
  public void cannotSumDateAndInteger() throws VariableNotDefinedException {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    when(variableFinder.typeOf("date")).thenReturn(ValueType.DATE);
    when(variableFinder.mapAlias(any())).then(AdditionalAnswers.returnsFirstArg());
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "date + 1");
    assertEquals(1, expression.getErrors().size());
    assertThat(expression.getErrors())
      .extracting(RuleExpressionCompilerError::getErrorCode)
      .containsOnly("CANNOT_ADD_TYPES");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").containsOnly(0);
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").containsOnly(7);
  }

  @Test
  public void cannotComparePeriodAndInteger() {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "1 day > 1");
    assertEquals(1, expression.getErrors().size());
    assertThat(expression.getErrors())
      .extracting(RuleExpressionCompilerError::getErrorCode)
      .containsOnly("NO_ORDER_RELATION_BETWEEN_TYPES");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").containsOnly(0);
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").containsOnly(8);
  }

  @Test
  public void cannotEqualPeriodAndInteger() {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "1 day = 1");
    assertEquals(1, expression.getErrors().size());
    assertThat(expression.getErrors())
      .extracting(RuleExpressionCompilerError::getErrorCode)
      .containsOnly("NO_EQUALITY_RELATION_BETWEEN_TYPES");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").containsOnly(0);
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").containsOnly(8);
  }

  @Test
  public void brokenStringShouldNotKillParser() {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Expression expression;
    expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "a = 'x ");
    assertEquals(2, expression.getErrors().size());
    assertThat(expression.getErrors())
      .extracting(RuleExpressionCompilerError::getErrorCode)
      .containsOnly("SYNTAX_ERROR");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").containsExactly(4, 7);
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").containsExactly(4, 7);
    assertNull(expression.getAst());
  }

  @Test
  public void brokenExpectStringForIsBlank() throws Exception {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    when(variableFinder.typeOf("a")).thenReturn(ValueType.DATE);
    when(variableFinder.mapAlias(any())).then(AdditionalAnswers.returnsFirstArg());
    Expression expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "a is blank");
    assertEquals(1, expression.getErrors().size());
    assertThat(expression.getErrors())
      .extracting(RuleExpressionCompilerError::getErrorCode)
      .containsOnly("STRING_VALUE_EXPECTED");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").containsExactly(0);
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").containsExactly(0);
    assertEquals("(isBlank a)", expression.getAst().toString());
  }

  @Test
  public void isNullOperator() throws Exception {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    when(variableFinder.typeOf("a")).thenReturn(ValueType.DATE);
    when(variableFinder.mapAlias(any())).then(AdditionalAnswers.returnsFirstArg());
    Expression expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "a is null");
    assertThat(expression.getErrors())
      .extracting("span.startIndex").isEmpty();
    assertThat(expression.getErrors())
      .extracting("span.stopIndex").isEmpty();
    assertEquals("(isNull a)", expression.getAst().toString());
  }

}
