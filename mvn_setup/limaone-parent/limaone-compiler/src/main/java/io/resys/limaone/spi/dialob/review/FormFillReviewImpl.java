package io.resys.limaone.spi.dialob.review;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.Objects;

import io.dialob.api.form.Form;
import io.dialob.api.proto.Actions;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.Questionnaire.Metadata;
import io.dialob.questionnaire.service.api.FormActions;
import io.dialob.questionnaire.service.api.FormActionsUpdatesCallback;
import io.resys.limaone.spi.dialob.FormDb.FormFillReview;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.dialob.builders.FormInstanceQueryImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class FormFillReviewImpl implements FormFillReview {
  private final FormDbProps formDbProps;
  private String formInstanceId;
  private Actions action;
  
  @Override
  public FormFillReview formInstanceId(String formInstanceId) {
    this.formInstanceId = Objects.requireNonNull(formInstanceId, () -> "formInstanceId must be defined");
    return this;
  }

  @Override
  public FormFillReview navigateTo(Actions action) {
    this.action = Objects.requireNonNull(action, () -> "action must be defined");
    return this;
  }

  @Override
  public Uni<Actions> build() {
    this.formInstanceId = Objects.requireNonNull(formInstanceId, () -> "formInstanceId must be defined");
    return new FormInstanceQueryImpl(formDbProps).includeForm(true).getOne(formInstanceId)
      .onItem().transform(data -> {
        if(action == null) {
          return build(data.getForm().get(), data.getQuestionnaire());
        }
        return build(data.getForm().get(), data.getQuestionnaire(), action);
      });
  }

  
  private Actions build(Form form, Questionnaire formData) {
    Objects.requireNonNull(form, () -> "form must be defined!");
    Objects.requireNonNull(formData, () -> "form data must be defined!");
    
    final var envir = new DialobSessionEnvir(form, 
        new Questionnaire.Builder()
          .from(formData)
          .id(formData.getId() + "-review") // wipe the ID, just in case
          .metadata(new Questionnaire.Metadata.Builder()
              .from(formData.getMetadata())
              .status(Metadata.Status.OPEN)
              .completed(null)
              .build())
          .build())
      .accept();
    
    final var formActions = new FormActions();
    envir.buildFullForm(new FormActionsUpdatesCallback(formActions));
    
    return new Actions(envir.getRevision(), formActions.getActions());
  }

  private Actions build(Form form, Questionnaire formData, Actions action) {
    Objects.requireNonNull(form, () -> "form must be defined!");
    Objects.requireNonNull(formData, () -> "form data must be defined!");
    Objects.requireNonNull(action, () -> "action must be defined!");
    
    final var formActions = new FormActions();
    
    final var envir = new DialobSessionEnvir(form, 
        new Questionnaire.Builder()
          .from(formData)
          .id(formData.getId() + "-review") // wipe the ID, just in case
          .metadata(new Questionnaire.Metadata.Builder()
              .from(formData.getMetadata())
              .status(Metadata.Status.OPEN)
              .completed(null)
              .build())
          .build())
      .accept();
    
    envir.dispatchActions(action.getActions());
    envir.buildFullForm(new FormActionsUpdatesCallback(formActions));

    
    return new Actions.Builder()
      .actions(formActions.getActions())
      .rev(envir.getRevision())
      .build();
  }

}
