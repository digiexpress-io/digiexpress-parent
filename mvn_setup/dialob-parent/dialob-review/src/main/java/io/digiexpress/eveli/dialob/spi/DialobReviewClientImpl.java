package io.digiexpress.eveli.dialob.spi;

/*-
 * #%L
 * dialob-review
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.dialob.api.form.Form;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.questionnaire.service.api.FormActions;
import io.dialob.questionnaire.service.api.FormActionsUpdatesCallback;
import io.digiexpress.eveli.dialob.api.DialobReviewClient;

public class DialobReviewClientImpl implements DialobReviewClient {

  @Override
  public ReviewBuilder createReview() {
    return new ReviewBuilder() {
      private Form form;
      private Questionnaire formData;
      
      @Override
      public ReviewBuilder formData(Questionnaire formData) {
        this.formData = formData;
        return this;
      }
      @Override
      public ReviewBuilder form(Form form) {
        this.form = form;
        return this;
      }
      
      @Override
      public Actions build() {
        DialobReviewAssert.notNull(form, () -> "form must be defined!");
        DialobReviewAssert.notNull(formData, () -> "form must be defined!");
        
        final var envir = new DialobSessionEnvir(form, formData).accept();
        
        final var formActions = new FormActions();
        envir.buildFullForm(new FormActionsUpdatesCallback(formActions));
        
        return ImmutableActions.builder()
          .actions(formActions.getActions())
          .rev(envir.getRevision())
          .build();
      }
    };
  }

}
