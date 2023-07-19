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
package io.dialob.rule.parser.analyze;

import com.google.common.collect.Maps;
import io.dialob.rule.parser.Expression;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableFinder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ValidateExpressionVisitorTest {

  @Test
  public void functionReturningNonBooleanShouldTriggerError() throws Exception {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Mockito.when(variableFinder.returnTypeOf("today", new ValueType[0])).thenReturn(ValueType.DATE);
    Expression expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "today()");
    final ValidateExpressionVisitor validateExpressionVisitor = new ValidateExpressionVisitor();
    expression.accept(validateExpressionVisitor);
    assertTrue(validateExpressionVisitor.hasErrors());

    verify(variableFinder, times(2)).isAsync("today");
    verify(variableFinder).returnTypeOf("today");
    verifyNoMoreInteractions(variableFinder);
  }

  @Test
  public void functionReturningBooleanIsAccepted() throws Exception {
    VariableFinder variableFinder = Mockito.mock(VariableFinder.class);
    Mockito.when(variableFinder.returnTypeOf("booleanFunction", new ValueType[0])).thenReturn(ValueType.BOOLEAN);
    Expression expression = Expression.createExpression(variableFinder, Maps.newHashMap(), "booleanFunction()");
    final ValidateExpressionVisitor validateExpressionVisitor = new ValidateExpressionVisitor();
    expression.accept(validateExpressionVisitor);
    assertFalse(validateExpressionVisitor.hasErrors());

    verify(variableFinder, times(2)).isAsync("booleanFunction");
    verify(variableFinder).returnTypeOf("booleanFunction");
    verifyNoMoreInteractions(variableFinder);
  }

}
