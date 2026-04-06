package io.digiexpress.eveli.dialob.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.dialob.api.DialobReviewClient;
import io.digiexpress.eveli.dialob.spi.DialobReviewClientImpl;


public class CreateReviewTest {

  DialobReviewClient client = new DialobReviewClientImpl();
  
  @Test
  public void createSession() throws JsonProcessingException {
    var mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new GuavaModule());
    
    final var form = mapper.readValue(DialobReviewConfig.toExpectedFile("form_1.json"), Form.class);
    final var formData = mapper.readValue(DialobReviewConfig.toExpectedFile("session_1.json"), Questionnaire.class);
    final var actions = client.createReview().form(form).formData(formData).build();
    
    Assertions.assertNotNull(actions);
    Assertions.assertEquals(12, actions.getActions().size());
    
    Assertions.assertEquals(1, 
        actions.getActions().stream()
          .filter(e -> e.getItem() != null)
          .filter(e -> e.getItem().getId() != null)
          .filter(e -> e.getItem().getId().equals("questionnaire"))
          .count());
  }
}
