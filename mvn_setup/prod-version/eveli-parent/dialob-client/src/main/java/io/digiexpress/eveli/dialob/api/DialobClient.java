package io.digiexpress.eveli.dialob.api;

/*-
 * #%L
 * dialob-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.io.Serializable;
import java.math.BigDecimal;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.rest.IdAndRevision;

public interface DialobClient {

  DialobSessionBuilder createSession();
  DialobProxy createProxy();

  
  Form createForm(Form form);
  Form getFormById(String formId);
  Questionnaire getQuestionnaireById(String questionnaireId);
  Dialob getDialobById(String questionnaireId);
  
  void completeSession(String questionnaireId);
  
  
  
  // Wrapper object, shorthand name for simplicity
  interface Dialob {
    Questionnaire unwrap();
    Answer answer(String name);
    String text(String name);
    BigDecimal decimal(String name);
    boolean bool(String name);
    Object variable(String name);
    Object context(String name);
    Questionnaire.Metadata metadata();
  }
  
  interface DialobSessionBuilder {
    DialobSessionBuilder formName(String formName);
    DialobSessionBuilder formTag(String formTag);
    DialobSessionBuilder language(String language);
    DialobSessionBuilder addContext(String id, Serializable value);
    DialobSessionBuilder addAnswer(String id, Serializable value);
    IdAndRevision build();
  }
}