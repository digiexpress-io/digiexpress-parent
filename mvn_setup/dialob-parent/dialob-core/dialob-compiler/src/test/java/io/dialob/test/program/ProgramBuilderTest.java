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
package io.dialob.test.program;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Maps;

import io.dialob.api.form.FormValidationError;
import io.dialob.executor.CreateDialobSessionProgramVisitor;
import io.dialob.executor.DialobSessionUpdater;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.DialobSessionVisitor;
import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemState;
import io.dialob.executor.model.ValueSetState;
import io.dialob.program.DialobProgram;
import io.dialob.program.DialobSessionEvalContextFactory;
import io.dialob.program.EvalContext;
import io.dialob.program.ProgramBuilder;
import io.dialob.program.expr.arith.ArrayReducerOperator;
import io.dialob.program.expr.arith.BinaryOperator;
import io.dialob.program.expr.arith.CollectRowFieldsOperator;
import io.dialob.program.expr.arith.GtOperator;
import io.dialob.program.model.FormItem;
import io.dialob.program.model.Program;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.function.FunctionRegistry;
import io.dialob.test.executor.AbstractDialobProgramTest;

public class ProgramBuilderTest extends AbstractDialobProgramTest {

  FunctionRegistry functionRegistry = Mockito.mock(FunctionRegistry.class);

  DialobSessionEvalContextFactory sessionContextFactory = new DialobSessionEvalContextFactory(functionRegistry, Clock.systemDefaultZone(), null);

  public Program buildProgram() {
    return newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .addItem("question2")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("boolean")
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .setActiveWhen("question1 = true")
      .build()
      .addQuestion("question3")
      .setLabel("fi", "Kysymys 3")
      .setType("text")
      .setActiveWhen("question1 is answered")
      .build()
      .build();
  }

  protected ProgramBuilder newProgramBuilder() {
    return new ProgramBuilder(functionRegistry);
  }

  @BeforeEach
  public void resetMocks() {
    Mockito.reset(functionRegistry);
  }

  @Test
  public void shouldBeVisitable() {
    Program program = buildProgram();

    DialobSessionEvalContextFactory sessionContextFactory = new DialobSessionEvalContextFactory(functionRegistry, Clock.systemDefaultZone(), null);
    final CreateDialobSessionProgramVisitor createDialobSessionProgramVisitor = new CreateDialobSessionProgramVisitor("tenant", "session1", "fi", null, (id, item) -> Optional.empty(), valueSetId -> Collections.emptyList(), Maps.newHashMap(), null, null, null);
    program.accept(createDialobSessionProgramVisitor);

    final DialobSession session = createDialobSessionProgramVisitor.getDialobSession();
    assertNotNull(session);

    final DialobSessionVisitor visitor = mock(DialobSessionVisitor.class);
    final DialobSessionVisitor.ItemVisitor visitItemStatesVisitor = mock(DialobSessionVisitor.ItemVisitor.class);
    final DialobSessionVisitor.ValueSetVisitor valueSetVisitor = mock(DialobSessionVisitor.ValueSetVisitor.class);
    final DialobSessionVisitor.ErrorVisitor errorVisitor = mock(DialobSessionVisitor.ErrorVisitor.class);

    when(visitor.visitItemStates()).thenReturn(Optional.of(visitItemStatesVisitor));
    when(visitor.visitValueSetStates()).thenReturn(Optional.of(valueSetVisitor));
    when(visitor.visitErrorStates()).thenReturn(Optional.of(errorVisitor));

    session.accept(visitor);

    InOrder inOrder = inOrder(visitor, visitItemStatesVisitor, valueSetVisitor, errorVisitor);
    inOrder.verify(visitor).start();
    inOrder.verify(visitor).visitItemStates();
    inOrder.verify(visitItemStatesVisitor, times(6)).visitItemState(anyItem());
    inOrder.verify(visitItemStatesVisitor).end();

    inOrder.verify(visitor).visitValueSetStates();
    inOrder.verify(valueSetVisitor, times(0)).visitValueSetState(any(ValueSetState.class));
    inOrder.verify(valueSetVisitor).end();

    inOrder.verify(visitor).visitErrorStates();
    inOrder.verify(errorVisitor, times(0)).visitErrorState(any(ErrorState.class));
    inOrder.verify(errorVisitor).end();

    inOrder.verify(visitor).end();
    inOrder.verifyNoMoreInteractions();
    verifyNoMoreInteractions(visitor);
  }


  @Test
  public void shouldNotUpdateInactiveQuestion() {
    Program program = buildProgram();

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    DialobSession dialobSession = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, dialobSession);

    assertNull(dialobSession.getItemState(toRef("question2")).get().getAnswer());
    assertNull(dialobSession.getItemState(toRef("question2")).get().getValue());
    dialobSessionUpdater.dispatchActions(answer(toRef("question2"), "vastee"));
    assertNull(dialobSession.getItemState(toRef("question2")).get().getAnswer());
    assertNull(dialobSession.getItemState(toRef("question2")).get().getValue());
  }

  @Test
  public void shouldUpdateActiveQuestion() {
    Program program = buildProgram();

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    DialobSession dialobSession = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, dialobSession);

    assertNull(dialobSession.getItemState(toRef("question2")).get().getAnswer());
    assertNull(dialobSession.getItemState(toRef("question2")).get().getValue());
    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "true")); // activates question2
    dialobSessionUpdater.dispatchActions(answer(toRef("question2"), "vastee"));
    assertEquals("vastee", dialobSession.getItemState(toRef("question2")).get().getValue());
    assertEquals("vastee", dialobSession.getItemState(toRef("question2")).get().getAnswer());
  }

  @Test
  public void shouldChangeStateBasedOnActivatinRule() {
    Program program = buildProgram();

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    DialobSession dialobSession = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, dialobSession);

    assertNull(dialobSession.getItemState(toRef("question1")).get().getAnswer());
    assertNull(dialobSession.getItemState(toRef("question1")).get().getValue());

    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "false"));
    assertEquals(Boolean.FALSE, dialobSession.getItemState(toRef("question1")).get().getValue());
    assertEquals("false", dialobSession.getItemState(toRef("question1")).get().getAnswer());
    assertInactive(dialobSession, toRef("question2"));
    assertActive(dialobSession, toRef("question3"));

    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "true"));
    assertEquals(Boolean.TRUE, dialobSession.getItemState(toRef("question1")).get().getValue());
    assertEquals("true", dialobSession.getItemState(toRef("question1")).get().getAnswer());
    assertActive(dialobSession, toRef("question2"));
    assertActive(dialobSession, toRef("question3"));

    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), null));
    assertNull(dialobSession.getItemState(toRef("question1")).get().getValue());
    assertNull(dialobSession.getItemState(toRef("question1")).get().getAnswer());
    assertInactive(dialobSession, toRef("question2"));
    assertInactive(dialobSession, toRef("question3"));

    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), ""));
    assertNull(dialobSession.getItemState(toRef("question1")).get().getValue());
    assertEquals("", dialobSession.getItemState(toRef("question1")).get().getAnswer());
    assertInactive(dialobSession, toRef("question2"));
    assertInactive(dialobSession, toRef("question3"));

    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "crap"));
    assertEquals(Boolean.FALSE, dialobSession.getItemState(toRef("question1")).get().getValue());
    assertEquals("crap", dialobSession.getItemState(toRef("question1")).get().getAnswer());
    assertInactive(dialobSession, toRef("question2"));
    assertActive(dialobSession, toRef("question3"));
  }


  @Test
  public void testPageNavigation() {
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .addItem("page2")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addPage("page2")
      .addItem("group2")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addGroup("group2")
      .addItem("question2")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("text")
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .build()
      .build();

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    assertNotNull(session);
    assertEnabled(session, toRef("page1"));
    assertEnabled(session, toRef("group1"));
    assertEnabled(session, toRef("question1"));
    assertDisabled(session, toRef("page2"));
    assertDisabled(session, toRef("group2"));
    assertDisabled(session, toRef("question2"));

    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);

    dialobSessionUpdater.dispatchActions(gotoPage("page2"));
    assertDisabled(session, toRef("page1"));
    assertDisabled(session, toRef("group1"));
    assertDisabled(session, toRef("question1"));
    assertEnabled(session, toRef("page2"));
    assertEnabled(session, toRef("group2"));
    assertEnabled(session, toRef("question2"));

    dialobSessionUpdater.dispatchActions(gotoPage("page1"));
    assertEnabled(session, toRef("page1"));
    assertEnabled(session, toRef("group1"));
    assertEnabled(session, toRef("question1"));
    assertDisabled(session, toRef("page2"));
    assertDisabled(session, toRef("group2"));
    assertDisabled(session, toRef("question2"));

    dialobSessionUpdater.dispatchActions(gotoPage("page3"));
    assertEnabled(session, toRef("page1"));
    assertEnabled(session, toRef("group1"));
    assertEnabled(session, toRef("question1"));
    assertDisabled(session, toRef("page2"));
    assertDisabled(session, toRef("group2"));
    assertDisabled(session, toRef("question2"));

    dialobSessionUpdater.dispatchActions(nextPage());
    assertDisabled(session, toRef("page1"));
    assertDisabled(session, toRef("group1"));
    assertDisabled(session, toRef("question1"));
    assertEnabled(session, toRef("page2"));
    assertEnabled(session, toRef("group2"));
    assertEnabled(session, toRef("question2"));

    dialobSessionUpdater.dispatchActions(nextPage());
    assertDisabled(session, toRef("page1"));
    assertDisabled(session, toRef("group1"));
    assertDisabled(session, toRef("question1"));
    assertEnabled(session, toRef("page2"));
    assertEnabled(session, toRef("group2"));
    assertEnabled(session, toRef("question2"));

    dialobSessionUpdater.dispatchActions(previousPage());
    assertEnabled(session, toRef("page1"));
    assertEnabled(session, toRef("group1"));
    assertEnabled(session, toRef("question1"));
    assertDisabled(session, toRef("page2"));
    assertDisabled(session, toRef("group2"));
    assertDisabled(session, toRef("question2"));

    dialobSessionUpdater.dispatchActions(previousPage());
    assertEnabled(session, toRef("page1"));
    assertEnabled(session, toRef("group1"));
    assertEnabled(session, toRef("question1"));
    assertDisabled(session, toRef("page2"));
    assertDisabled(session, toRef("group2"));
    assertDisabled(session, toRef("question2"));
  }

  @Test
  public void testErrorTriggering() {
    // @formatter:off
    Program program = newProgramBuilder()
      .startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("text")
      .addValidation("ERROR1")
      .setActiveWhen("question1 is answered")
      .setLabel("fi", "Error!")
      .build()
      .build()
      .build();
    // @formatter:on
    System.out.println(program);

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);
    Collection<ErrorState> errorStates = session.getErrorStates().values();
    assertEquals(1, errorStates.size());
    assertErrorInactive(session, toRef("question1"), "ERROR1");
    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "wrong"));
    assertErrorActive(session, toRef("question1"), "ERROR1");
  }

  @Test
  public void testErrorRequired() {
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setRequired(true)
      .setType("text")
      .build()
      .build();

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);
    Collection<ErrorState> errorStates = session.getErrorStates().values();
    assertEquals(1, errorStates.size());
    assertErrorActive(session, toRef("question1"), "REQUIRED");
    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "answer"));
    assertErrorInactive(session, toRef("question1"), "REQUIRED");
  }

  @Test
  public void testErrorWhenRequired() {
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setRequiredWhen("true")
      .setType("text")
      .build()
      .build();

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    final DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);
    Collection<ErrorState> errorStates = session.getErrorStates().values();
    assertEquals(1, errorStates.size());
    assertErrorActive(session, toRef("question1"), "REQUIRED");
    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "answer"));
    assertErrorInactive(session, toRef("question1"), "REQUIRED");
  }

  @Test
  public void shouldNotifyAboutUpdatedItems() {
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("text")
      .build()
      .build();

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    final DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);

    final EvalContext.UpdatedItemsVisitor visitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.class);
    final EvalContext.UpdatedItemsVisitor.UpdatedErrorStateVisitor errorVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedErrorStateVisitor.class);
    final EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor itemVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor.class);
    final EvalContext.UpdatedItemsVisitor.UpdatedValueSetVisitor valueSetVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedValueSetVisitor.class);

    when(visitor.visitUpdatedItems()).thenReturn(Optional.of(itemVisitor));
    when(visitor.visitUpdatedErrorStates()).thenReturn(Optional.of(errorVisitor));
    when(visitor.visitUpdatedValueSets()).thenReturn(Optional.of(valueSetVisitor));

    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "answer"))
      .accept(visitor);
    InOrder order = inOrder(visitor, errorVisitor, itemVisitor, valueSetVisitor);

    order.verify(visitor).start();
    order.verify(visitor).visitUpdatedItems();
//    order.verify(visitor).visitUpdatedItemState(argThat(unansweredItem(null)), argThat(answeredItem(null)));
    order.verify(itemVisitor).end();
    order.verify(visitor).visitUpdatedErrorStates();
    order.verify(errorVisitor).end();
    order.verify(visitor).end();
    order.verifyNoMoreInteractions();
  }

  @Test
  public void shouldNotifyAboutUpdatedErrors() {
    // @formatter:off
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("text")
      .addValidation("err1")
      .setActiveWhen("answer = 'answer'")
      .build()
      .build()
      .build();
    // @formatter:on

    DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);
    final DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);

    final EvalContext.UpdatedItemsVisitor visitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.class);
    final EvalContext.UpdatedItemsVisitor.UpdatedErrorStateVisitor errorVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedErrorStateVisitor.class);
    final EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor itemVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedItemStateVisitor.class);
    final EvalContext.UpdatedItemsVisitor.UpdatedValueSetVisitor valueSetVisitor = Mockito.mock(EvalContext.UpdatedItemsVisitor.UpdatedValueSetVisitor.class);

    when(visitor.visitUpdatedItems()).thenReturn(Optional.of(itemVisitor));
    when(visitor.visitUpdatedErrorStates()).thenReturn(Optional.of(errorVisitor));
    when(visitor.visitUpdatedValueSets()).thenReturn(Optional.of(valueSetVisitor));

    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "answer"))
      .accept(visitor);
    InOrder order = inOrder(visitor, errorVisitor, itemVisitor, valueSetVisitor);

    order.verify(visitor).start();
    order.verify(visitor).visitUpdatedItems();
    order.verify(itemVisitor).end();
    order.verify(visitor).visitUpdatedErrorStates();
    order.verify(errorVisitor).visitUpdatedErrorState(argThat(inactiveError()), argThat(activeError()));
    order.verify(errorVisitor).end();
    order.verify(visitor).end();
    order.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEvaluateVariableValueExpression() {
    // @formatter:off
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("number")
      .build()
      .addVariable("var1")
      .setValueExpression("question1 + 1")
      .build()
      .build();
    // @formatter:on

    final DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);

    final DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);

    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);
    Collection<ErrorState> errorStates = session.getErrorStates().values();
    assertValueEquals(session, toRef("question1"), null);
    assertValueEquals(session, toRef("var1"), null);
    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "1"));
    assertValueEquals(session, toRef("question1"), 1);
    assertValueEquals(session, toRef("var1"), 2);
  }

  @Test
  public void shouldCompileValidationsWithLocalAliasAnswer() {
    // @formatter:off
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("text")
      .addValidation("err1")
      .setActiveWhen("answer = 'a'")
      .setLabel("fi", "virhe")
      .build()
      .build()
      .build();
    // @formatter:on

    final DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);

    final DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);

    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);
    Collection<ErrorState> errorStates = session.getErrorStates().values();
    assertValueEquals(session, toRef("question1"), null);
    assertErrorInactive(session, toRef("question"), "err1");
    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "a"));
    assertValueEquals(session, toRef("question1"), "a");
    assertErrorActive(session, toRef("question1"), "err1");
  }


  @Test
  public void shouldCompileRequiredExpressions() {
    // @formatter:off
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("text")
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .setRequiredWhen("question1 = 'y'")
      .build()
      .build();
    // @formatter:on

    final DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);

    final DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);

    assertNotNull(session);
    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);
    Collection<ErrorState> errorStates = session.getErrorStates().values();
    assertValueEquals(session, toRef("question1"), null);
    assertNotRequired(session, toRef("question2"));
    dialobSessionUpdater.dispatchActions(answer(toRef("question1"), "y"));
    assertValueEquals(session, toRef("question1"), "y");
    assertRequired(session, toRef("question2"));
  }

  @Test
  public void shouldAddVariablesForAsyncFunctionInvocations() throws Exception {
    when(functionRegistry.isAsyncFunction("f1")).thenReturn(true);
    when(functionRegistry.returnTypeOf("f1", ValueType.STRING)).thenReturn(ValueType.BOOLEAN);

    // @formatter:off
    Program program = newProgramBuilder().startProgram()
      .setId("matches")
      .addRoot()
      .addItem("page1")
      .build()
      .addPage("page1")
      .addItem("group1")
      .build()
      .addGroup("group1")
      .addItem("question1")
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 1")
      .setType("text")
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .setRequiredWhen("f1(question1) = true")
      .build()
      .build();
    // @formatter:on

    final DialobProgram dialobProgram = DialobProgram.createDialobProgram(program);

    final DialobSession session = dialobProgram.createSession(sessionContextFactory, null, null, "fi", null);
    assertNotNull(session);

    ItemState asyncVariable = session.getItemState(IdUtils.toId("$$f1_1")).get();
    Assertions.assertNull(asyncVariable.getValue());

    DialobSessionUpdater dialobSessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, session);
    Collection<ErrorState> errorStates = session.getErrorStates().values();
    assertValueEquals(session, toRef("question1"), null);
    assertNotRequired(session, toRef("question2"));
    dialobSessionUpdater.dispatchActions(setValue(toRef("$$f1_1"), true));
    assertValueEquals(session, toRef("$$f1_1"), true);
    assertRequired(session, toRef("question2"));
  }

  @Test
  public void shouldReportCompilationErrorOnVisibility() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
        .build()
      .addQuestion("question2")
        .setLabel("fi", "Kysymys 2")
        .setType("text")
        .setActiveWhen("question1 XX true")
        .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.VISIBILITY, error.getType());

  }

  @Test
  public void shouldReportCompilationErrorOnRequirement() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .setRequiredWhen("question1 XX true")
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.REQUIREMENT, error.getType());

  }

  @Test
  public void shouldReportCompilationErrorOnValidation() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .addValidation("error1")
        .setActiveWhen("answer XX")
      .build()
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.VALIDATION, error.getType());
  }

  @Test
  public void shouldReportCompilationErrorOnClassname() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .addClassname("answer XX", "error")

      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.CLASSNAME, error.getType());
  }

  @Test
  public void shouldReportCompilationErrorOnValueEntry() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
       .build()
      .addValueSet("vset1")
        .addValue("XX XX")
          .setActiveWhen("e1")
          .setLabel(new HashMap<>())
        .build()
        .addValue("XX YY")
          .setActiveWhen("e2")
          .setLabel(new HashMap<>())
        .build()
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
//    Assertions.assertEquals(2, errors.size());
    Iterator<FormValidationError> iterator = errors.iterator();
    FormValidationError error = iterator.next();
    assertEquals(FormValidationError.Type.VALUESET_ENTRY, error.getType());
    assertEquals("UNKNOWN_VARIABLE", error.getMessage());
    assertEquals(0, error.getIndex().get());

    error = iterator.next();
    assertEquals(FormValidationError.Type.VALUESET_ENTRY, error.getType());
    assertEquals("UNKNOWN_VARIABLE", error.getMessage());
    assertEquals(1, error.getIndex().get());
  }

  @Test
  public void shouldReportInvalidExpressionErrorOnValueEntry() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addValueSet("vset1")
      .addValue("XX 1")
      .setActiveWhen("true")
      .setLabel(new HashMap<>())
      .build()
      .addValue("XX 2")
      .setActiveWhen("1")
      .setLabel(new HashMap<>())
      .build()
      .addValue("XX 3")
      .setActiveWhen("'x'")
      .setLabel(new HashMap<>())
      .build()
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
//    Assertions.assertEquals(2, errors.size());
    Iterator<FormValidationError> iterator = errors.iterator();
    FormValidationError error = iterator.next();
    assertEquals(FormValidationError.Type.VALUESET_ENTRY, error.getType());
    assertEquals("BOOLEAN_EXPRESSION_EXPECTED", error.getMessage());
    assertEquals(1, error.getIndex().get());

    error = iterator.next();
    assertEquals(FormValidationError.Type.VALUESET_ENTRY, error.getType());
    assertEquals("BOOLEAN_EXPRESSION_EXPECTED", error.getMessage());
    assertEquals(2, error.getIndex().get());
  }


  @Test
  public void shouldReportCompilationErrorOnVariable() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addVariable("var1")
        .setValueExpression("q1 XX")
        .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.VARIABLE, error.getType());
  }

  @Test
  public void shouldNotAcceptNonBooleanExpressionToActiveWhenCondition() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .setActiveWhen("question1")
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.VISIBILITY, error.getType());
  }

  @Test
  public void shouldNotAcceptNonBooleanExpressionToRequiredWhenCondition() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question1")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .setRequiredWhen("question1")
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.REQUIREMENT, error.getType());
  }

  @Test
  public void shouldNotAcceptNonBooleanExpressionToValidationCondition() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .addValidation("error1")
      .setActiveWhen("answer")
      .build()
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.VALIDATION, error.getType());
  }

  @Test
  public void shouldReportErrorsCorrectly() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .addValidation("error1")
      .setActiveWhen("'text' + 'text'")
      .build()
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.VALIDATION, error.getType());
    assertEquals("NO_ARITMETHIC_FOR_TYPE", error.getMessage());
  }

  @Test
  public void shouldReportCoercionErrorsCorrectly() {
    // formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    programBuilder.startProgram()
      .setId("matches")
      .addRoot()
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("text")
      .addValidation("error1")
      .setActiveWhen("'test' + 1")
      .build()
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError error = errors.iterator().next();
    assertEquals(FormValidationError.Type.VALIDATION, error.getType());
    assertEquals("CANNOT_COERCE_TYPE", error.getMessage());
  }

  @Test
  public void shouldParseReducerExpressions() {
    // @formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    Program program = programBuilder
      .startProgram()
        .setId("matches")
        .addRoot()
          .addItem("rg1")
          .addItem("note1")
          .build()
        .addRowGroup("rg1")
          .addItem("question2")
          .addItem("question3")
          .build()
        .addQuestion("question2")
          .setLabel("fi", "Kysymys 2")
          .setType("number")
          .build()
        .addQuestion("question3")
          .setLabel("fi", "Kysymys 2")
          .setType("number")
          .setRequiredWhen("question2 is answered")
          .build()
        .addQuestion("note1")
          .setType("note")
          .setActiveWhen("sum of question2 > 0")
          .build()
        .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(0, errors.size());

    FormItem note = (FormItem) program.getItem(IdUtils.toId("note1")).get();

    BinaryOperator expression = (BinaryOperator) note.getActiveExpression().get();
    GtOperator gtOperator = (GtOperator)expression.getNodes().get(1);
    ArrayReducerOperator arrayReducerOperator = (ArrayReducerOperator)gtOperator.getLhs();
    CollectRowFieldsOperator collectRowFieldsOperator = (CollectRowFieldsOperator) arrayReducerOperator.getArrayExpression();
    assertEquals(IdUtils.toId("rg1.*.question2"), collectRowFieldsOperator.getItemId());
  }

  @Test
  public void shouldParseReducerExpressions2() {
    // @formatter:off
    final ProgramBuilder programBuilder = newProgramBuilder();
    Program program = programBuilder
      .startProgram()
      .setId("matches")
      .addRoot()
      .addItem("rg1")
      .addItem("note1")
      .build()
      .addRowGroup("rg1")
      .addItem("question2")
      .addItem("question3")
      .build()
      .addQuestion("question2")
      .setLabel("fi", "Kysymys 2")
      .setType("number")
      .build()
      .addVariable("var")
        .setValueExpression("sum of d")
      .build()
      .build();
    // @formatter:on
    final List<FormValidationError> errors = programBuilder.getErrors();
    Assertions.assertEquals(1, errors.size());
    FormValidationError ve = errors.get(0);
    Assertions.assertEquals("UNKNOWN_VARIABLE", ve.getMessage());
  }


}
