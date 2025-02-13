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
package io.dialob.test.program.expr.arith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.dialob.executor.model.ErrorState;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ItemId;
import io.dialob.executor.model.ItemState;
import io.dialob.program.EvalContext;
import io.dialob.program.expr.arith.ImmutableIsInvalidAnswersOnActivePage;

class IsInvalidAnswersOnActivePageTest {

  @Test
  public void shouldReturnFalseIfNoActiveErrorFound() {
    EvalContext context = mock(EvalContext.class);
    final ImmutableIsInvalidAnswersOnActivePage isInvalidAnswersOnActivePage = ImmutableIsInvalidAnswersOnActivePage.builder().pageContainerId(IdUtils.QUESTIONNAIRE_ID).build();

    when(context.getErrorStates()).thenReturn(Collections.emptyList());

    assertFalse(isInvalidAnswersOnActivePage.eval(context));

    verify(context).getErrorStates();
    verifyNoMoreInteractions(context);
  }

  @Test
  public void shouldReturnTrueIfErrorFoundOnPage() {
    final ItemId page1Id = IdUtils.toId("page1");

    EvalContext context = mock(EvalContext.class);
    ItemState questionnaire = mock(ItemState.class);
    ItemState page1 = mock(ItemState.class);
    ItemState q1 = mock(ItemState.class);

    final List<ErrorState> errorStates = Arrays.asList(errorState("q1", true));
    when(context.getErrorStates()).thenReturn(errorStates);
    when(context.getItemState(IdUtils.QUESTIONNAIRE_ID)).thenReturn(Optional.of(questionnaire));
    when(questionnaire.getActivePage()).thenReturn(Optional.of(page1Id));
    when(context.getItemState(page1Id)).thenReturn(Optional.of(page1));
    when(page1.getItems()).thenReturn(Arrays.asList(IdUtils.toId("q1")));
    when(context.getItemState(IdUtils.toId("q1"))).thenReturn(Optional.of(q1));
    when(q1.getId()).thenReturn(IdUtils.toId("q1"));
    when(q1.getType()).thenReturn("text");


    final ImmutableIsInvalidAnswersOnActivePage isInvalidAnswersOnActivePage = ImmutableIsInvalidAnswersOnActivePage.builder().pageContainerId(IdUtils.QUESTIONNAIRE_ID).build();
    assertTrue(isInvalidAnswersOnActivePage.eval(context));

    verify(context).getErrorStates();
    verify(context).getItemState(IdUtils.QUESTIONNAIRE_ID);
    verify(context).getItemState(IdUtils.toId("q1"));
    verify(context).getItemState(page1Id);
    verify(questionnaire).getActivePage();
    verifyNoMoreInteractions(context, questionnaire);
  }

  @Test
  public void shouldReturnTrueIfErrorFoundOnPageNestedGroup() {
    final ItemId page1Id = IdUtils.toId("page1");
    final ItemId group1Id = IdUtils.toId("group1");

    EvalContext context = mock(EvalContext.class);
    ItemState questionnaire = mock(ItemState.class);
    ItemState page1 = mock(ItemState.class);
    ItemState group1 = mock(ItemState.class);
    ItemState q1 = mock(ItemState.class);

    final List<ErrorState> errorStates = Arrays.asList(errorState("q1", true));
    when(context.getErrorStates()).thenReturn(errorStates);
    when(context.getItemState(IdUtils.QUESTIONNAIRE_ID)).thenReturn(Optional.of(questionnaire));
    when(questionnaire.getActivePage()).thenReturn(Optional.of(page1Id));
    when(context.getItemState(page1Id)).thenReturn(Optional.of(page1));
    when(context.getItemState(group1Id)).thenReturn(Optional.of(group1));
    when(page1.getItems()).thenReturn(Arrays.asList(group1Id));
    when(group1.getItems()).thenReturn(Arrays.asList(IdUtils.toId("q1")));
    when(context.getItemState(IdUtils.toId("q1"))).thenReturn(Optional.of(q1));
    when(context.getItemState(group1Id)).thenReturn(Optional.of(group1));
    when(q1.getId()).thenReturn(IdUtils.toId("q1"));
    when(q1.getType()).thenReturn("text");
    when(group1.getId()).thenReturn(group1Id);
    when(group1.getType()).thenReturn("group");


    final ImmutableIsInvalidAnswersOnActivePage isInvalidAnswersOnActivePage = ImmutableIsInvalidAnswersOnActivePage.builder().pageContainerId(IdUtils.QUESTIONNAIRE_ID).build();
    assertTrue(isInvalidAnswersOnActivePage.eval(context));

    verify(context).getErrorStates();
    verify(context).getItemState(IdUtils.QUESTIONNAIRE_ID);
    verify(context).getItemState(IdUtils.toId("q1"));
    verify(context).getItemState(group1Id);
    verify(context).getItemState(page1Id);
    verify(questionnaire).getActivePage();
    verifyNoMoreInteractions(context, questionnaire);
  }

  private ErrorState errorState(String itemId, boolean active) {
    ErrorState errorState = mock(ErrorState.class);
    when(errorState.isActive()).thenReturn(active);
    when(errorState.getItemId()).thenReturn(IdUtils.toId(itemId));
    return errorState;
  }

}
