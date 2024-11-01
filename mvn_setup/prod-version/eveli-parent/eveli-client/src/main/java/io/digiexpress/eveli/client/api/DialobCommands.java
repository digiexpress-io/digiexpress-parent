package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
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
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.rest.IdAndRevision;
import lombok.Data;

public interface DialobCommands {
  
  OneSessionBuilder createOneSession();
  DialobProxy createProxy();

  
  // is it used ???
  Questionnaire.Metadata.Status getStatus(String sessionId);
  
  
  Questionnaire get(String questionnaireId);
  Dialob getDialob(String questionnaireId);
  
  Form getForm(String formId);
  TagQueryBuilder getTags();
  
  
  
  void complete(String sessionId);  
  void delete(String sessionId);
  
  
  
  interface OneSessionBuilder {
    OneSessionBuilder formName(String formName);
    OneSessionBuilder formTag(String formTag);
    OneSessionBuilder language(String language);
    OneSessionBuilder addContext(String id, Serializable value);
    OneSessionBuilder addAnswer(String id, Serializable value);
    IdAndRevision build();
  }
  
  // Proxy any request against dialob api
  interface DialobProxy {
    ResponseEntity<String> fillGet(String sessionId);
    ResponseEntity<String> fillPost(String sessionId, String body);
    
    ResponseEntity<String> reviewGet(String sessionId);
    ResponseEntity<String> anyRequest(String path, String query, HttpMethod method, String body, Map<String, String> headers);
  }
  


  
  interface TagQueryBuilder {
    TagQueryBuilder formName(String formName);
    List<FormTag> build();
  }
  
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
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Data
  public static class FormListItem {
    private String id;
    private Form.Metadata metadata;
  }
  
  @Data
  public static class FormTagResult {
    private String formLabel;
    private String formName;
    private String tagFormId;
    private String tagName;
  }


}
