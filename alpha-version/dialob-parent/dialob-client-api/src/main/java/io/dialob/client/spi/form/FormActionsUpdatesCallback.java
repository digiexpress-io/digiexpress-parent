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
package io.dialob.client.spi.form;

import io.dialob.api.proto.ActionItem;
import io.dialob.api.proto.ValueSet;
import io.dialob.api.questionnaire.Error;
import io.dialob.client.api.QuestionnaireSession;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Locale;


@Slf4j
public class FormActionsUpdatesCallback implements QuestionnaireSession.UpdatesCallback {


  private final FormActions formActions;

  public FormActionsUpdatesCallback(@Nonnull FormActions formActions) {
    this.formActions = formActions;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback questionAdded(@Nonnull ActionItem question) {
    log.debug("newQuestion({})", question);
    formActions.newQuestion(question);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback questionUpdated(@Nonnull ActionItem question) {
    log.debug("updateQuestion({})", question);
    formActions.updateQuestion(question);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback questionRemoved(@Nonnull String itemId) {
    log.debug("removeQuestion({})", itemId);
    formActions.removeQuestion(itemId);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback valueSetAdded(@Nonnull ValueSet valueSet) {
    log.debug("valueSetAdded({})", valueSet);
    formActions.newValueSet(valueSet);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback valueSetUpdated(@Nonnull ValueSet valueSet) {
    log.debug("valueSetUpdated({})", valueSet);
    formActions.updateValueSet(valueSet);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback valueSetRemoved(@Nonnull String valueSetId) {
    log.debug("valueSetRemoved({})", valueSetId);
    formActions.removeValueSet(valueSetId);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback errorAdded(@Nonnull Error error) {
    log.debug("addError({})", error);
    formActions.addError(error);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback errorRemoved(@Nonnull Error error) {
    log.debug("removeError({})", error);
    formActions.removeError(error);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback removeAll() {
    log.debug("removeAll()");
    formActions.removeAll();
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback locale(Locale locale) {
    log.debug("locale({})", locale);
    formActions.locale(locale);
    return this;
  }

  @Nonnull
  @Override
  public FormActionsUpdatesCallback completed() {
    log.debug("completed()");
    formActions.complete();
    return this;
  }
}
