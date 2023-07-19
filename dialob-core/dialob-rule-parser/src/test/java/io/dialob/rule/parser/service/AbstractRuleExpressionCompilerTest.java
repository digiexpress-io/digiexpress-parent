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
package io.dialob.rule.parser.service;

import io.dialob.rule.parser.api.RuleExpressionCompiler;
import io.dialob.rule.parser.api.RuleExpressionCompilerCallback;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public abstract class AbstractRuleExpressionCompilerTest {

  private RuleExpressionCompiler compiler;

  @Mock
  private VariableFinder variableFinder;

  @Mock
  private RuleExpressionCompilerCallback callback;

  protected abstract RuleExpressionCompiler createRuleExpressionCompiler();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.initMocks(this);
    compiler = createRuleExpressionCompiler();

    when(variableFinder.mapAlias(anyString())).thenAnswer(answer -> {
      return answer.getArguments()[0];
    });
  }

  @Test
  public void missingArgumentThrowsNullPointerException() {
    assertThatThrownBy(() -> compiler.compile("x", null, null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> compiler.compile("x", mock(VariableFinder.class), null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> compiler.compile("x", null, mock(RuleExpressionCompilerCallback.class))).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> compiler.compile(null, mock(VariableFinder.class), mock(RuleExpressionCompilerCallback.class))).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldNotAcceptIncompleteExpressions() throws Exception {
    when(variableFinder.typeOf("a")).thenReturn(ValueType.TIME);
    compiler.compile("a", variableFinder, callback);
    verify(variableFinder).typeOf("a");
    verify(variableFinder, atLeast(0)).mapAlias("a");
    verify(variableFinder).findVariableScope("a");
    verify(callback).failed(anyList());
    verifyNoMoreInteractions(variableFinder, callback);
  }

  @Test
  public void booleanTypeVariablesAreNotCompleteExpressions() throws Exception {
    when(variableFinder.typeOf("a")).thenReturn(ValueType.BOOLEAN);
    compiler.compile("a", variableFinder, callback);
    verify(variableFinder).typeOf("a");
    verify(variableFinder, atLeast(0)).mapAlias("a");
    verify(variableFinder).findVariableScope("a");
    verify(callback).failed(anyList());
    verifyNoMoreInteractions(variableFinder, callback);
  }

  @Test
  public void expressionShouldEvaluateToBoolean() throws Exception {
    when(variableFinder.typeOf("a")).thenReturn(ValueType.INTEGER);
    compiler.compile("a + 1", variableFinder, callback);
    verify(variableFinder).typeOf("a");
    verify(variableFinder, atLeast(0)).mapAlias("a");
    verify(variableFinder).findVariableScope("a");
    verify(callback).failed(anyList());
    verifyNoMoreInteractions(variableFinder, callback);
  }


  @Test
  @Disabled
  // functions are not fully implemented yet
  public void functionsEvaluatingToNonBooleanAreNotAccepted() throws Exception {
    when(variableFinder.typeOf("a")).thenReturn(ValueType.INTEGER);
    when(variableFinder.returnTypeOf("value")).thenReturn(ValueType.INTEGER);
    compiler.compile("value(a)", variableFinder, callback);
    verify(variableFinder).typeOf("a");
    verify(variableFinder, atLeast(0)).mapAlias("a");
    verify(variableFinder).findVariableScope("a");
    verify(callback).failed(anyList());
    verifyNoMoreInteractions(variableFinder, callback);
  }

  @Test
  public void constantNonBooleanExpressionsAreNotAccepted() throws Exception {
    compiler.compile("1", variableFinder, callback);
    compiler.compile("1.0", variableFinder, callback);
    compiler.compile("\"a\"", variableFinder, callback);
    verify(callback, times(3)).failed(anyList());
    verifyNoMoreInteractions(variableFinder, callback);
  }

  @Test
  public void shouldRenameId() {
    assertEquals("x + 1", compiler.createIdRenamer("x", "x").apply("x + 1"));
    assertEquals("y + 1", compiler.createIdRenamer("x", "y").apply("x + 1"));
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionOnBlankIds() {
    assertThatThrownBy(() -> compiler.createIdRenamer("", "x")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldNotHandleInvalidExpressions() {
    assertEquals("x + ", compiler.createIdRenamer("x", "y").apply("x + "));
  }

}
