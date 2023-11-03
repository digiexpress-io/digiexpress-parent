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
package io.dialob.client.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.ActionItem;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ValueSet;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Error;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.VariableValue;

@Value.Enclosing
public interface QuestionnaireSession {

  enum QuestionClientVisibility {
    ALL,
    SHOW_DISABLED, //
    ONLY_ENABLED // Default behaviour
  }

  @Value.Immutable
  interface DispatchActionsResult {

    Actions getActions();

    boolean isDidComplete();

  }

  /**
   * @return update actions
   * @deprecated session should always generate updates. Client connectors should check state of session and
   * choose to send updates or full form
   */
  @Deprecated
  @Nonnull
  DispatchActionsResult dispatchActions(String revision, @Nonnull Collection<Action> actions);

  @Nonnull
  DispatchActionsResult dispatchActions(@Nonnull Collection<Action> actions);

  @Nonnull
  Questionnaire getQuestionnaire();

  @Nonnull
  String getRevision();

  String getRev();

  String getOwner();

  @Nonnull
  Instant getLastUpdate();

  Optional<String> getActiveItem();

  @Nonnull
  List<ValueSet> getValueSets();

  @Nonnull
  List<Error> getErrors();

  @Nonnull
  List<ActionItem> getItems();

  Optional<ActionItem> getItemById(@Nonnull String itemId);

  @Nonnull
  List<ActionItem> getVisibleItems();

  @Nonnull
  List<Answer> getAnswers();

  @Nonnull
  List<VariableValue> getVariableValues();

  void buildFullForm(@Nonnull UpdatesCallback updatesCallback);

  Optional<String> getSessionId();

  String getTenantId();

  void activate();

  void passivate();

  boolean isActive();

  boolean isCompleted();

  boolean usesLastestFormRevision();

  @Nonnull
  String getFormId();

  Optional<Locale> getLocale();

  QuestionClientVisibility getQuestionClientVisibility();

  QuestionnaireSession withIdAndRev(String id, String rev);

  @Nonnull
  Questionnaire.Metadata.Status getStatus();

  enum State {
    NEW,
    INITIALIZED,
    ACTIVE,
    PASSIVE,
    COMPLETED;

    @Nonnull
    public State to(@Nonnull State next) {
      return next;
    }
  }

  interface UpdatesCallback {
    @Nonnull
    UpdatesCallback questionAdded(@Nonnull ActionItem item);

    @Nonnull
    UpdatesCallback questionUpdated(@Nonnull ActionItem item);

    @Nonnull
    UpdatesCallback questionRemoved(@Nonnull String itemId);

    @Nonnull
    UpdatesCallback errorAdded(@Nonnull Error error);

    @Nonnull
    UpdatesCallback errorRemoved(@Nonnull Error error);

    @Nonnull
    UpdatesCallback removeAll();

    @Nonnull
    UpdatesCallback locale(Locale locale);

    @Nonnull
    UpdatesCallback completed();

    @Nonnull
    UpdatesCallback valueSetAdded(@Nonnull ValueSet valueSet);

    @Nonnull
    UpdatesCallback valueSetUpdated(@Nonnull ValueSet valueSet);

    @Nonnull
    UpdatesCallback valueSetRemoved(@Nonnull String valueSetId);

  }

  void close();

}
