package io.digiexpress.eveli.dialob.test;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.dialob.api.DialobReviewClient;
import io.digiexpress.eveli.dialob.spi.DialobReviewClientImpl;
import io.vertx.core.json.JsonObject;


public class CreateReviewTest {

  DialobReviewClient client = new DialobReviewClientImpl();
  
  @Test
  public void createSession() {
    
    final var form = new JsonObject(DialobReviewConfig.toExpectedFile("form_1.json")).mapTo(Form.class);
    final var formData = new JsonObject(DialobReviewConfig.toExpectedFile("session_1.json")).mapTo(Questionnaire.class);
    final var actions = client.createReview().form(form).formData(formData).build();
    
    Assertions.assertNotNull(actions);
    Assertions.assertEquals(16, actions.getActions().size());
  }
}
