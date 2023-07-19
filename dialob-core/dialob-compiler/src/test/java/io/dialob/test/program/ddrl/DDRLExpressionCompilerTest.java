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
package io.dialob.test.program.ddrl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ImmutableItemRef;
import io.dialob.program.EvalContext;
import io.dialob.program.ddrl.DDRLExpressionCompiler;
import io.dialob.program.expr.DDRLOperatorFactory;
import io.dialob.program.model.Expression;
import io.dialob.rule.parser.api.RuleExpressionCompilerError;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableFinder;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import io.dialob.rule.parser.function.FunctionRegistry;

public class DDRLExpressionCompilerTest {
  @Nonnull
  private DDRLExpressionCompiler createDdrlExpressionCompiler() {
    FunctionRegistry functionRegistry = mock(FunctionRegistry.class);
    when(functionRegistry.isAsyncFunction(anyString())).thenReturn(false);
    return new DDRLExpressionCompiler(new DDRLOperatorFactory());
  }


  @Test
  public void constantIntegerShouldReturnAsInteger() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = mock(VariableFinder.class);
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "0", errorConsumer).get();

    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();

    assertEquals(0, expression.eval(evalContext));

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }

  @Test
  public void todayShouldReturnCurrentDate() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineToday(variableFinder, evalContext, testClock);

    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "today", errorConsumer).get();

    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();

    Object eval = expression.eval(evalContext);
    assertEquals(LocalDate.from(now.atZone(here)), eval);

    verifyToday(variableFinder);
    verify(evalContext).today();

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }

  @Test
  public void nowShouldReturnCurrentTime() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineNow(variableFinder, evalContext, testClock);

    //when
    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "now", errorConsumer).get();

    // then
    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();
    Object eval = expression.eval(evalContext);
    assertEquals(LocalTime.from(now.atZone(here)), eval);

    verifyNow(variableFinder);
    verify(evalContext).now();

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }


  @Test
  public void dateDifferenceShouldReturnPeriod() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineToday(variableFinder, evalContext, testClock);
    defineVariable(variableFinder, evalContext, "yesterday", ValueType.DATE, LocalDate.now(testClock).minusDays(1));

    //when
    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "today - yesterday", errorConsumer).get();

    // then
    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();
    Object eval = expression.eval(evalContext);
    assertEquals(Period.of(0,0, 1), eval);

    verifyToday(variableFinder);
    verifyVariable(variableFinder, "yesterday");
    verify(evalContext).getItemValue((ImmutableItemRef) IdUtils.toId("yesterday"));
    verify(evalContext).today();

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }

  @Test
  public void datePlusPeriodShouldReturnDate() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineToday(variableFinder, evalContext, testClock);

    //when
    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "today + 1 day", errorConsumer).get();

    // then
    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();
    Object eval = expression.eval(evalContext);
    assertEquals(LocalDate.now(testClock).plusDays(1), eval);

    verifyToday(variableFinder);
    verify(evalContext).today();

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }

  @Test
  public void dateMinusPeriodShouldReturnDate() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineToday(variableFinder, evalContext, testClock);

    // when
    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "today - 1 day", errorConsumer).get();

    // then
    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();
    Object eval = expression.eval(evalContext);
    assertEquals(LocalDate.now(testClock).minusDays(1), eval);

    verifyToday(variableFinder);
    verify(evalContext).today();

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }

  @Test
  public void timeDifferenceShouldReturnDuration() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.parse("2017-11-25T15:05:12.025Z");
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineNow(variableFinder, evalContext, testClock);
    defineVariable(variableFinder, evalContext, "hourAgo", ValueType.TIME, LocalTime.now(testClock).minusHours(1));

    // when
    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "now - hourAgo", errorConsumer).get();

    // then
    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();
    Object eval = expression.eval(evalContext);
    assertEquals(Duration.ofHours(1), eval);

    verifyNow(variableFinder);
    verifyVariable(variableFinder, "hourAgo");
    verify(evalContext).now();
    verify(evalContext).getItemValue((ImmutableItemRef) IdUtils.toId("hourAgo"));

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }
  @Test
  public void timePlusDurationShouldReturnTime() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineNow(variableFinder, evalContext, testClock);

    // when
    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "now + 1 hour", errorConsumer).get();

    // then
    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();
    Object eval = expression.eval(evalContext);
    assertEquals(LocalTime.now(testClock).plusHours(1), eval);

    verifyNow(variableFinder);
    verify(evalContext).now();

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }
  @Test
  public void timeMinusDurationShouldReturnTime() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);

    defineNow(variableFinder, evalContext, testClock);

    // when
    Expression expression = ddrlExpressionCompiler.compile(variableFinder, "now - 1 hour", errorConsumer).get();

    // then
    assertThat(expression.getEvalRequiredConditions()).doesNotContainNull();
    Object eval = expression.eval(evalContext);
    assertEquals(LocalTime.now(testClock).minusHours(1), eval);

    verifyNow(variableFinder);
    verify(evalContext).now();

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }

  @Test
  public void periodPlusAndMinusPeriodShouldReturnPeriod() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);


    // when
    Expression expressionPlus = ddrlExpressionCompiler.compile(variableFinder, "1 year + 1 month", errorConsumer).get();
    Expression expressionMinus = ddrlExpressionCompiler.compile(variableFinder, "1 year - 1 month", errorConsumer).get();

    // then
    Object eval = expressionPlus.eval(evalContext);
    assertEquals(Period.of(1,1,0), eval);

    eval = expressionMinus.eval(evalContext);
    Period period = (Period) eval;
    assertEquals(Period.of(0,11,0), period);

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }

  @Test
  public void durationPlusAndMinusDurationShouldReturnDuration() throws Exception {
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);

    Instant now = Instant.now();
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);


    // when
    Expression expressionPlus = ddrlExpressionCompiler.compile(variableFinder, "1 hour + 1 minute", errorConsumer).get();
    Expression expressionMinus = ddrlExpressionCompiler.compile(variableFinder, "1 hour - 1 minute", errorConsumer).get();

    // then
    Object eval = expressionPlus.eval(evalContext);
    assertEquals(Duration.parse("PT1H1M"), eval);

    eval = expressionMinus.eval(evalContext);
    assertEquals(Duration.parse("PT59M"), eval);

    verifyNoMoreInteractions(variableFinder, evalContext, errorConsumer);
  }


  @Test
  public void compareDurations() throws Exception {
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);

    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 hour <= 1 minute", errorConsumer).get().eval(evalContext));
    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 hour < 1 minute", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "30 minutes + 30 minutes = 1 hour", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 hour >= 1 minute", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 hour > 1 minute", errorConsumer).get().eval(evalContext));
  }


  @Test
  public void comparePeriods() throws Exception {
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);

    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 year <= 1 month", errorConsumer).get().eval(evalContext));
    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 year < 1 month", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "6 months + 6 months = 1 year", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 year >= 1 month", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "1 year > 1 month", errorConsumer).get().eval(evalContext));
  }

  @Test
  public void compareDates() throws Exception {
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);

    Instant now = Instant.parse("2017-09-16T00:00:00.000Z");
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);
    defineToday(variableFinder, evalContext, testClock);
    defineVariable(variableFinder, evalContext, "firstDate", ValueType.DATE, LocalDate.parse("2017-01-01"));
    defineVariable(variableFinder, evalContext, "years18ago", ValueType.DATE, LocalDate.parse("1999-09-15"));
    defineVariable(variableFinder, evalContext, "years18agotoday", ValueType.DATE, LocalDate.parse("1999-09-16"));

    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "today <= firstDate", errorConsumer).get().eval(evalContext));
    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "today < firstDate", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "today = today", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "today >= firstDate", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "today > firstDate", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "today - years18ago > 18 years", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "today - years18agotoday = 18 years", errorConsumer).get().eval(evalContext));
  }

  @Test
  public void compareTimes() throws Exception {
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);

    Instant now = Instant.parse("2017-09-16T09:00:00.000Z");
    ZoneId here = tzHere();
    Clock testClock = Clock.fixed(now, here);
    defineNow(variableFinder, evalContext, testClock);
    defineVariable(variableFinder, evalContext, "startOfDay", ValueType.TIME, LocalTime.parse("00:00:00.000"));
    defineVariable(variableFinder, evalContext, "midDay", ValueType.TIME, LocalTime.parse("12:00:00.000"));
    defineVariable(variableFinder, evalContext, "endOfDay", ValueType.TIME, LocalTime.parse("23:59:59.999"));

    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "now <= startOfDay", errorConsumer).get().eval(evalContext));
    assertFalse((Boolean) ddrlExpressionCompiler.compile(variableFinder, "now < startOfDay", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "now = midDay", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "now >= startOfDay", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "now > startOfDay", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "endOfDay - now > (5 hours + 59 minutes + 59 seconds)", errorConsumer).get().eval(evalContext));
    assertTrue((Boolean) ddrlExpressionCompiler.compile(variableFinder, "endOfDay - now < 12 hours", errorConsumer).get().eval(evalContext));
  }

  @Test
  public void shouldNotAcceptArrayOnLhsForInOperator() throws Exception {
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);
    DDRLExpressionCompiler ddrlExpressionCompiler = createDdrlExpressionCompiler();
    final VariableFinder variableFinder = variableFinderNoAliases();
    final EvalContext evalContext = mock(EvalContext.class);

    defineVariable(variableFinder, evalContext, "a", ValueType.arrayOf(ValueType.STRING), new String[]{});
    defineVariable(variableFinder, evalContext, "s", ValueType.STRING, "");

    assertTrue(ddrlExpressionCompiler.compile(variableFinder, "s in ('a','b')", errorConsumer).isPresent());
    verifyNoMoreInteractions(errorConsumer);
    reset(errorConsumer);
    assertTrue(ddrlExpressionCompiler.compile(variableFinder, "s in a", errorConsumer).isPresent());
    verifyNoMoreInteractions(errorConsumer);
    reset(errorConsumer);

    assertFalse(ddrlExpressionCompiler.compile(variableFinder, "a in a", errorConsumer).isPresent());
    verify(errorConsumer).accept(argThat(argument -> argument.getErrorCode().equals("ARRAY_TYPE_UNEXPECTED")));
    verifyNoMoreInteractions(errorConsumer);
    reset(errorConsumer);

    assertFalse(ddrlExpressionCompiler.compile(variableFinder, "a in s", errorConsumer).isPresent());
    verify(errorConsumer).accept(argThat(argument -> argument.getErrorCode().equals("ARRAY_TYPE_EXPECTED")));
    verify(errorConsumer).accept(argThat(argument -> argument.getErrorCode().equals("ARRAY_TYPE_UNEXPECTED")));
    verifyNoMoreInteractions(errorConsumer);
    reset(errorConsumer);

    assertFalse(ddrlExpressionCompiler.compile(variableFinder, "s in s", errorConsumer).isPresent());
    verify(errorConsumer).accept(argThat(argument -> argument.getErrorCode().equals("ARRAY_TYPE_EXPECTED")));
    verifyNoMoreInteractions(errorConsumer);
    reset(errorConsumer);

  }

  @Test
  public void shouldExtractAsyncFunctionCalls() throws Exception {
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);
    DDRLExpressionCompiler ddrlExpressionCompiler = new DDRLExpressionCompiler(new DDRLOperatorFactory());
    final VariableFinder variableFinder = variableFinderNoAliases();


    when(variableFinder.isAsync("f1")).thenReturn(true);
    when(variableFinder.typeOf("a")).thenReturn(ValueType.INTEGER);
    when(variableFinder.returnTypeOf("f1", ValueType.INTEGER, ValueType.INTEGER)).thenReturn(ValueType.INTEGER);


    Optional<Expression> expression = ddrlExpressionCompiler.compile(variableFinder, "10 + f1(1,a) + f1(1,a)", errorConsumer);

    assertTrue(expression.isPresent());
    Map<String, Expression> replacements = ddrlExpressionCompiler.getAsyncFunctionVariableExpressions();
    assertEquals(1, replacements.size());
    assertNotNull(replacements.get("$$f1_1"));
    verifyNoMoreInteractions(errorConsumer);
  }

  @Test
  public void shouldExtractNestedAsyncFunctionCalls() throws Exception {
    final Consumer<RuleExpressionCompilerError> errorConsumer = mock(Consumer.class);
    DDRLExpressionCompiler ddrlExpressionCompiler = new DDRLExpressionCompiler(new DDRLOperatorFactory());
    final VariableFinder variableFinder = variableFinderNoAliases();


    when(variableFinder.isAsync("f1")).thenReturn(true);
    when(variableFinder.typeOf("a")).thenReturn(ValueType.INTEGER);
    when(variableFinder.returnTypeOf("f1", ValueType.INTEGER, ValueType.INTEGER)).thenReturn(ValueType.INTEGER);


    Optional<Expression> expression = ddrlExpressionCompiler.compile(variableFinder, "10 + f1(1,f1(1,a))", errorConsumer);

    assertTrue(expression.isPresent());
    Map<String, Expression> replacements = ddrlExpressionCompiler.getAsyncFunctionVariableExpressions();
    assertEquals(2, replacements.size());

    assertNotNull(replacements.get("$$f1_1"));
    assertNotNull(replacements.get("$$f1_2"));


    verifyNoMoreInteractions(errorConsumer);
  }



  public ZoneId tzHere() {
    // Europe/Helsinki + day light save
    return ZoneId.of("+03:00");
  }

  private VariableFinder variableFinderNoAliases() {
    final VariableFinder variableFinder = mock(VariableFinder.class);
    when(variableFinder.mapAlias(any())).then(returnsFirstArg());
    when(variableFinder.findVariableScope(anyString())).thenReturn(Optional.empty());
    return variableFinder;
  }

  private void verifyToday(VariableFinder variableFinder) throws VariableNotDefinedException {
    verify(variableFinder, atLeastOnce()).mapAlias("today");
    verify(variableFinder, atLeastOnce()).typeOf("today");
    verify(variableFinder, atLeastOnce()).findVariableScope("today");
  }

  private void defineToday(VariableFinder variableFinder, EvalContext evalContext, Clock testClock) throws VariableNotDefinedException {
    when(variableFinder.typeOf("today")).thenReturn(ValueType.DATE);
    when(variableFinder.findVariableScope("today")).thenReturn(Optional.empty());
    when(evalContext.today()).thenReturn(LocalDate.now(testClock));
  }


  private void verifyNow(VariableFinder variableFinder) throws VariableNotDefinedException {
    verify(variableFinder, atLeastOnce()).mapAlias("now");
    verify(variableFinder, atLeastOnce()).typeOf("now");
    verify(variableFinder, atLeastOnce()).findVariableScope("now");
  }

  private void verifyVariable(VariableFinder variableFinder, String varName) throws VariableNotDefinedException {
    verify(variableFinder, atLeastOnce()).mapAlias(varName);
    verify(variableFinder, atLeastOnce()).typeOf(varName);
    verify(variableFinder, atLeastOnce()).findVariableScope(varName);
  }


  private void defineNow(VariableFinder variableFinder, EvalContext evalContext, Clock testClock) throws VariableNotDefinedException {
    when(variableFinder.typeOf("now")).thenReturn(ValueType.TIME);
    when(variableFinder.findVariableScope("now")).thenReturn(Optional.empty());
    when(evalContext.now()).thenReturn(LocalTime.now(testClock));
  }

  private void defineVariable(VariableFinder variableFinder, EvalContext evalContext, String varName, ValueType valueType, Object value) throws VariableNotDefinedException {
    when(variableFinder.typeOf(varName)).thenReturn(valueType);
    when(variableFinder.findVariableScope(varName)).thenReturn(Optional.empty());
    when(evalContext.getItemValue((ImmutableItemRef) IdUtils.toId(varName))).thenReturn(value);
  }

}
