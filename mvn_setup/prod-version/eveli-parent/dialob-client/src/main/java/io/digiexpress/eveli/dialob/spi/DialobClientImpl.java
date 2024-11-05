package io.digiexpress.eveli.dialob.spi;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.dialob.api.form.Form;
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
  private final RestTemplate restTemplate;
  private final String authorization;
  
  private final String sessionUrl;
  private final String questionnaireUrl;
  private final String callbackUrl;
  private final String formUrl;
  
  private final String serviceUrl;
  
  @Override
  public DialobSessionBuilderImpl createSession() {
    return new DialobSessionBuilderImpl(objectMapper, objectMapper.getFactory(), authorization, questionnaireUrl, callbackUrl, formUrl, restTemplate);
  }

  @Override
  public DialobProxy createProxy() {
    return new DialobProxyImpl(serviceUrl, sessionUrl, questionnaireUrl, authorization, restTemplate);
  }

  @Override
  public Questionnaire getQuestionnaireById(String questionnaireId) {
    return restTemplate.getForObject(questionnaireUrl + "/"+ questionnaireId, Questionnaire.class);
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
      final var resp = createProxy().anyRequest("dialob/api/forms", null, HttpMethod.POST, body, headers);
      
      return objectMapper.readValue(resp.getBody(), Form.class);
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }

  @Override
  public void completeSession(String sessionId) {

    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");
    try {
      final var headers = headers();
      final var sessionUrl = UriComponentsBuilder.fromHttpUrl(questionnaireUrl).pathSegment(sessionId).toUriString();
      
      final var getResponse = restTemplate.exchange(sessionUrl, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      
      // NEW -> OPEN
      String statusValue = getStatus(getResponse.getBody());
      if(statusValue.equals("NEW")) {
        
        ResponseEntity<String> response = restTemplate.exchange(sessionUrl, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "OPEN"), headers), String.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
      
        // OPEN -> COMPLETED
        ResponseEntity<JsonNode> openResponse = restTemplate.exchange(sessionUrl, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
        ResponseEntity<String> completed = restTemplate.exchange(sessionUrl, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(openResponse.getBody(), "COMPLETED"), headers), String.class);
        DialobAssert.isTrue(completed.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        
        return;
      } else if (statusValue.equals("COMPLETED")) {
        return;
        
      } else {
        ResponseEntity<String> response = restTemplate.exchange(sessionUrl, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "COMPLETED"), headers), String.class);
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
      String sessionUrl = UriComponentsBuilder.fromHttpUrl(questionnaireUrl).pathSegment(sessionId).pathSegment("status").toUriString();
      ResponseEntity<String> getResponse = restTemplate.exchange(sessionUrl, HttpMethod.GET, new HttpEntity<String>(headers()), String.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      String statusValue = getResponse.getBody().substring(1, getResponse.getBody().length() - 1);
      return statusValue.equals("COMPLETED") ? Questionnaire.Metadata.Status.COMPLETED : Questionnaire.Metadata.Status.OPEN;
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  

  @Override
  public Form getFormById(String formId) {
    Map<String, String> uriMap = new HashMap<>();
    uriMap.put("formId", formId);
    return restTemplate.getForObject(formUrl + "/{formId}", Form.class, uriMap);
  }
  
  private HttpHeaders headers() {
    final var headers = new HttpHeaders();
    headers.set(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON.toString());
    //headers.set(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON.toString());
    
    
    
    if(!ObjectUtils.isEmpty(authorization)) {
      headers.set("x-api-key", authorization);
    }
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
    private RestTemplate restTemplate;
    private String authorization;
    private String url;
    private String submitCallbackUrl;
    private String formUrl;
    private String serviceUrl;
    private String sessionUrl;

    public DialobClientImpl build() {
      DialobAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      DialobAssert.notNull(restTemplate, () -> "restTemplate must be defined!");
      DialobAssert.notNull(url, () -> "url must be defined!");
      DialobAssert.notNull(formUrl, () -> "form url must be defined!");
      DialobAssert.notNull(sessionUrl, () -> "form url must be defined!");
      DialobAssert.notNull(serviceUrl, () -> "serviceUrl must be defined!");
      
      return new DialobClientImpl(objectMapper, restTemplate, authorization, url, submitCallbackUrl, formUrl, sessionUrl, serviceUrl);
    }
  }
}
