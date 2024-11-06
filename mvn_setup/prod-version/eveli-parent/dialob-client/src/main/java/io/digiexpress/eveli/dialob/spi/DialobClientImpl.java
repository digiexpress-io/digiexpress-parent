package io.digiexpress.eveli.dialob.spi;

import java.io.IOException;
import java.util.Optional;

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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.dialob.api.form.FormTag.Type;
import io.dialob.api.form.ImmutableFormTag;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.api.DialobProxy;
import io.digiexpress.eveli.dialob.spi.DialobAssert.DialobException;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
public class DialobClientImpl implements DialobClient {
  
  private final ObjectMapper objectMapper;
  private final DialobService dialobService;
  private final String callbackUrl;
  
  @Override
  public DialobSessionBuilderImpl createSession() {
    return new DialobSessionBuilderImpl(objectMapper, callbackUrl, dialobService);
  }
  @Override
  public DialobProxy createProxy() {
    return new DialobProxyImpl(dialobService);
  }
  @Override
  public Questionnaire getQuestionnaireById(String questionnaireId) {
    return dialobService.getSessions().getForObject(questionnaireId, Questionnaire.class);
  }
  @Override
  public Dialob getDialobById(String questionnaireId) {
    final var q = getQuestionnaireById(questionnaireId);
    return new QuestionnaireWrapperImpl(q);
  }
  @Override
  public Form createForm(Form form) {
    try {
      final var headers = headers().toSingleValueMap();
      final var body = objectMapper.writeValueAsString(form);
      final var resp = createProxy().formRequest("", null, HttpMethod.POST, body, headers);
      return objectMapper.readValue(resp.getBody(), Form.class);
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  @Override
  public FormTag createTag(String formId, String tagName) {
    try {
      final var headers = headers().toSingleValueMap();
      final var body = objectMapper.writeValueAsString(ImmutableFormTag.builder()
          .name(tagName)
          .formName(formId)
          .type(Type.NORMAL)
          .build());
      final var postTagResp = createProxy().formRequest("/" + formId + "/tags", null, HttpMethod.POST, body, headers);
      DialobAssert.isTrue(postTagResp.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + postTagResp.getStatusCode() + " but expecting 2xx!");
      
      
      final var getTagResp = createProxy().formRequest("/" + formId + "/tags/" + tagName, null, HttpMethod.GET, body, headers);
      DialobAssert.isTrue(getTagResp.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getTagResp.getStatusCode() + " but expecting 2xx!");
      
      return objectMapper.readValue(getTagResp.getBody(), FormTag.class);
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  @Override
  public Form getFormById(String formId) {
    return dialobService.getForms().getForObject("/" + formId, Form.class);
  }
  @Override
  public Form getFormByNameAndTag(String formName, String formTag) {
    return dialobService.getForms().getForObject("/" + formName + "/tags/" + formTag, Form.class);
  }
  @Override
  public Optional<Form> findOneFormById(String formId) {
    try {
      return Optional.ofNullable(dialobService.getForms().getForObject("/" + formId, Form.class));
    } catch(org.springframework.web.client.HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
  

  @Override
  public void completeSession(String sessionId) {

    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");
    try {
      final var headers = headers();
      
      final var getResponse = dialobService.getSessions().exchange(sessionId, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      
      // NEW -> OPEN
      String statusValue = getStatus(getResponse.getBody());
      if(statusValue.equals("NEW")) {
        
        ResponseEntity<String> response = dialobService.getSessions().exchange(sessionId, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "OPEN"), headers), String.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
      
        // OPEN -> COMPLETED
        ResponseEntity<JsonNode> openResponse = dialobService.getSessions().exchange(sessionId, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
        ResponseEntity<String> completed = dialobService.getSessions().exchange(sessionId, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(openResponse.getBody(), "COMPLETED"), headers), String.class);
        DialobAssert.isTrue(completed.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        
        return;
      } else if (statusValue.equals("COMPLETED")) {
        return;
        
      } else {
        ResponseEntity<String> response = dialobService.getSessions().exchange(sessionId, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "COMPLETED"), headers), String.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        return;
      }
      
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }


  public Questionnaire.Metadata.Status getStatus(String sessionId) {
    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");
    try {

      ResponseEntity<String> getResponse = dialobService.getSessions().exchange(sessionId + "/status", HttpMethod.GET, new HttpEntity<String>(headers()), String.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      String statusValue = getResponse.getBody().substring(1, getResponse.getBody().length() - 1);
      return statusValue.equals("COMPLETED") ? Questionnaire.Metadata.Status.COMPLETED : Questionnaire.Metadata.Status.OPEN;
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  
  private HttpHeaders headers() {
    final var headers = new HttpHeaders();
    headers.set(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON.toString());
    headers.set(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON.toString());
    return headers;
  } 
  
  private static String getStatus(JsonNode getBody) {
    ObjectNode metadata = (ObjectNode) getBody.get("metadata");
    String statusValue = metadata.get("status").textValue();
    return statusValue;
  }
  
  private static JsonNode setStatus(JsonNode getBody, String status) {
    ObjectNode metadata = (ObjectNode) getBody.get("metadata");
    metadata.set("status", TextNode.valueOf(status));
    return getBody;
  }

  public static Builder builder() {
    return new Builder();
  }
  
  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private ObjectMapper objectMapper;
    private DialobService dialobService;
    private String submitCallbackUrl;

    public DialobClientImpl build() {
      DialobAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      DialobAssert.notNull(dialobService, () -> "dialobService must be defined!");
      
      return new DialobClientImpl(objectMapper, dialobService, submitCallbackUrl);
    }
  }
}
