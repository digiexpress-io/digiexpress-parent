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
package io.dialob.client.tests.fill;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ActionsFactory;
import io.dialob.api.proto.ImmutableAction;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.api.questionnaire.ImmutableQuestionnaire;
import io.dialob.api.questionnaire.ImmutableQuestionnaireMetadata;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient.ProgramEnvir;
import io.dialob.client.api.DialobClient.QuestionnaireExecutor;
import io.dialob.client.api.QuestionnaireSession;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.form.FormActions;
import io.dialob.client.spi.form.FormActionsUpdatesCallback;
import io.dialob.client.tests.client.DialobClientTestImpl;
import lombok.Data;

public class FillAssertionBuilder {

  private final DialobClientTestImpl client;
  private final ProgramEnvir envir;
  private final Questionnaire questionnaire;
  private final String formId;
  
  private boolean completed;
  // activation triggers fires first actions
  int expectedActionsEventCount = 0;
  private List<ValidationEntry> validationEntries =  new ArrayList<>();

  @Data
  static class ValidationEntry {
    private Consumer<QuestionnaireSession> sessionConsumer;
    private Action action;
    private Consumer<AbstractListAssert<?, ? extends List<? extends Action>, Action, ? extends AbstractAssert<?, Action>>> assertter;
    private Actions actions;
  }


  public FillAssertionBuilder(String formId, DialobClientTestImpl client, ProgramEnvir envir) {
    this.formId = formId;
    this.questionnaire = null;
    this.client = client;
    this.envir = envir;
  }
  public FillAssertionBuilder(Questionnaire questionnaire, DialobClientTestImpl client, ProgramEnvir envir) {
    this.questionnaire = questionnaire;
    this.formId = questionnaire.getMetadata().getFormId();
    this.client = client;
    this.envir = envir;
  }

  public FillAssertionBuilder assertState(Consumer<AbstractListAssert<?, ? extends List<? extends Action>, Action, ? extends AbstractAssert<?, Action>>> assertter) {
    ValidationEntry validationEntry = new ValidationEntry();
    validationEntry.setAssertter(assertter);
    validationEntries.add(validationEntry);
    return this;
  }

  public FillAssertionBuilder queueAction(Action action) {
    ValidationEntry validationEntry = new ValidationEntry();
    validationEntry.setAction(action);
    validationEntries.add(validationEntry);
    expectedActionsEventCount++;
    return this;
  }

  public FillAssertionBuilder answer(String questionId, Object answer) {
    return queueAction(ImmutableAction.builder()
      .type(Action.Type.ANSWER)
      .id(questionId)
      .answer(answer).build());
  }

  public FillAssertionBuilder checkSession(Consumer<QuestionnaireSession> sessionConsumer) {
    ValidationEntry validationEntry = new ValidationEntry();
    validationEntry.setSessionConsumer(sessionConsumer);
    validationEntries.add(validationEntry);
    return this;
  }

  public FillAssertionBuilder addRow(String id) {
    return queueAction(ImmutableAction.builder()
      .type(Action.Type.ADD_ROW)
      .id(id).build());
  }

  public FillAssertionBuilder setLocale(String locale) {
    return queueAction(ActionsFactory.setLocale(locale));
  }

  public FillAssertionBuilder deleteRow(String id) {
    return queueAction(ImmutableAction.builder()
      .type(Action.Type.DELETE_ROW)
      .id(id).build());
  }

  public FillAssertionBuilder nextPage() {
    return queueAction(ImmutableAction.builder()
      .type(Action.Type.NEXT).build());
  }

  public FillAssertionBuilder previousPage() {
    return queueAction(ImmutableAction.builder()
      .type(Action.Type.PREVIOUS).build());
  }

  public FillAssertionBuilder complete(boolean expected) {
    completed = expected;
    return queueAction(ImmutableAction.builder()
      .type(Action.Type.COMPLETE).build());
  }

  public FillAssertionBuilder complete() {
    return complete(true);
  }


  public FillAssertionBuilder assertThat(Consumer<AbstractListAssert<?, ? extends List<? extends Action>, Action, ? extends AbstractAssert<?, Action>>> consumer) {
    validationEntries.get(validationEntries.size() - 1).assertter = consumer;
    return this;
  }

  public void apply() {
    final var applicationEventPublisher = mock(QuestionnaireEventPublisher.class);
    final var client = this.client.with(applicationEventPublisher);    
    
    QuestionnaireExecutor executor;
    if (this.questionnaire == null) {
      executor = client.executor(envir).create(this.formId, (init) -> {
        init.id(UUID.randomUUID().toString())
        .rev(UUID.randomUUID().toString());
      });
    } else {
      final var meta = ImmutableQuestionnaireMetadata.builder().from(this.questionnaire.getMetadata()).language("en").build();
      final var restore = ImmutableQuestionnaire.builder().from(this.questionnaire).metadata(meta)
          .id(UUID.randomUUID().toString())
          .rev(UUID.randomUUID().toString())
          .build();
      executor = client.executor(envir).restore(restore);
    }
    QuestionnaireSession session = executor.toSession();

    for (final ValidationEntry validationEntry : validationEntries) {
      if (validationEntry.getSessionConsumer() != null) {
        validationEntry.getSessionConsumer().accept(session);
        validationEntry.setActions(ImmutableActions.builder().build());
      } else if (validationEntry.getAction() != null) {
        validationEntry.setActions(session.dispatchActions(Collections.singletonList(validationEntry.getAction())).getActions());
      } else {
        FormActions formActions =  new FormActions();
        session.buildFullForm(new FormActionsUpdatesCallback(formActions));
        validationEntry.setActions(ImmutableActions.builder().actions(formActions.getActions()).build());
      }
    }

    if (this.questionnaire == null) {
      verify(applicationEventPublisher).created(anyString());
    }
    verify(applicationEventPublisher).opened(anyString());
    if (this.completed) {
      verify(applicationEventPublisher).completed(anyString());

    }
    verify(applicationEventPublisher,times(expectedActionsEventCount)).actions(anyString(), any(Actions.class));
    assertEvents();
    Mockito.verifyNoMoreInteractions(applicationEventPublisher);
  }

  public void assertEvents() {
    Iterator<ValidationEntry> ai = validationEntries.iterator();
    while(ai.hasNext()) {
      final ValidationEntry validationEntry = ai.next();
      if ( validationEntry.getAssertter() != null) {
        validationEntry.getAssertter().accept(Assertions.<Action>assertThat(validationEntry.actions.getActions()));
      }
    }
    assertFalse(ai.hasNext());
  }
  
}
