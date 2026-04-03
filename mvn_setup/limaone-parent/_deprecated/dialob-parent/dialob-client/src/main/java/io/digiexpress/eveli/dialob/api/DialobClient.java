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
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormTag;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.rest.IdAndRevision;
import lombok.Data;

// TODO:: clean up this mess
public interface DialobClient {

  DialobSessionBuilder createSession();
  void completeSession(String questionnaireId);
  DialobClientProxy createProxyClient(); // combines form and questionnaire api-s 
  
  
  List<FormListItem> findAllForms();
  List<FormTag> findAllFormTags();

  
  Form createForm(Form form);
  Form updateForm(Form form);
  FormTag createTag(String formId, String tagName);
  Form getFormById(String formId);
  
  Optional<Form> findOneFormById(String formId);
  List<FormTag> getOneFormTags(String formName);
  
  FormTag getFormTag(String formName, String formTag);
  Form getFormByNameAndTag(String formName, String formTag);
    
  
  Questionnaire getQuestionnaireById(String questionnaireId);
  ProxyAnswer proxyAnswer(Questionnaire q, Answer answer);
  
  // attaches form meta data to questionnaire
  Questionnaire getQuestionnaireAndMetaById(String questionnaireId);
  
  // used by wrench flows
  Dialob getDialobById(String questionnaireId);
  
  
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
    //DialobSessionBuilder formName(String formName);
    //DialobSessionBuilder formTag(String formTag);
    DialobSessionBuilder formId(String formId);
    DialobSessionBuilder language(String language);
    DialobSessionBuilder addContext(String id, Serializable value);
    DialobSessionBuilder addAnswer(String id, Serializable value);
    IdAndRevision build();
  }
  
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Data
  public static class FormListItem {
    private String id;
    private Form.Metadata metadata;
  }
  
  
  @lombok.Data @lombok.Builder
  public static class ProxyAnswer {
    private final Answer answer;
    private final FormItem formItem;
    private final Optional<String> valueSetLabel;
  }

}
