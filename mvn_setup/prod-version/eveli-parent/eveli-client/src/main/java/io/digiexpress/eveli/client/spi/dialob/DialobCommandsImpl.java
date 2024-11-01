package io.digiexpress.eveli.client.spi.dialob;

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
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.rest.IdAndRevision;
import io.digiexpress.eveli.client.api.DialobClient;
import io.digiexpress.eveli.client.spi.dialob.DialobAssert.DialobException;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DialobCommandsImpl implements DialobClient {
  
  
  private final ObjectMapper objectMapper;
  private final RestTemplate client;
  private final String authorization;
  
  private final String sessionUrl;
  private final String questionnaireUrl;
  private final String callbackUrl;
  private final String formUrl;
  
  private final String serviceUrl;
  
  @Override
  public Questionnaire get(String questionnaireId) {
    return client.getForObject(questionnaireUrl + "/"+ questionnaireId, Questionnaire.class);
  }
  
  @Override
  public Dialob getDialob(String questionnaireId) {
    return new DialobImpl(client.getForObject(questionnaireUrl + "/"+ questionnaireId, Questionnaire.class));
  }
  
  
  @Override
  public SessionBuilder createSession() {
    return new DialobBodyBuilder(objectMapper, objectMapper.getFactory(), callbackUrl) {
      private String formName;
      private String formTag;
      @Override
      public IdAndRevision build() {
        String formId = getFormId(formName, formTag);
        formId(formId);
        final var body = buildBody();
        final HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers());
        try {
          ResponseEntity<IdAndRevision> response = client.exchange(questionnaireUrl, HttpMethod.POST, requestEntity, IdAndRevision.class);
          DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 201!");
          return response.getBody();
        } catch (Exception e) {
          throw new DialobException(e.getMessage(), e);
        }
      }

      private String getFormId(String formName2, String formTag2) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(formUrl)
        .pathSegment(formName)
        .pathSegment("tags")
        .pathSegment(formTag);
        ResponseEntity<FormTag> response = client.getForEntity(uriBuilder.toUriString(), FormTag.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        FormTag tag = response.getBody();
        return tag.getFormId();
      }

      @Override
      public SessionBuilder formName(String formName) {
        this.formName = formName;
        return this;
      }

      @Override
      public SessionBuilder formTag(String formTag) {
        this.formTag = formTag;
        return this;
      }
    };
  }
  
  @Override
  public void delete(String sessionId) {

    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");
    try {
      log.debug("Dialob form deleting is not supported for: {}", sessionId);
      
      //String sessionUrl = UriComponentsBuilder.fromHttpUrl(url).pathSegment(sessionId).toUriString();
      //ResponseEntity<JsonNode> getResponse = client.exchange(sessionUrl, HttpMethod.DELETE, new HttpEntity<String>(headers), JsonNode.class);
      //DialobAssert.isTrue(getResponse.getStatusCodeValue() == 200, () -> "DIALOB status was: " + getResponse.getStatusCodeValue() + " but expecting 200!");
      
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }

  @Override
  public void complete(String sessionId) {

    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");
    try {
      final var headers = headers();
      final var sessionUrl = UriComponentsBuilder.fromHttpUrl(questionnaireUrl).pathSegment(sessionId).toUriString();
      
      final var getResponse = client.exchange(sessionUrl, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      
      // NEW -> OPEN
      String statusValue = getStatus(getResponse.getBody());
      if(statusValue.equals("NEW")) {
        
        ResponseEntity<String> response = client.exchange(sessionUrl, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "OPEN"), headers), String.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
      
        // OPEN -> COMPLETED
        ResponseEntity<JsonNode> openResponse = client.exchange(sessionUrl, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
        ResponseEntity<String> completed = client.exchange(sessionUrl, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(openResponse.getBody(), "COMPLETED"), headers), String.class);
        DialobAssert.isTrue(completed.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        
        return;
      } else if (statusValue.equals("COMPLETED")) {
        return;
        
      } else {
        ResponseEntity<String> response = client.exchange(sessionUrl, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "COMPLETED"), headers), String.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        return;
      }
      
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }

  @Override
  public Questionnaire.Metadata.Status getStatus(String sessionId) {
    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");

    try {
      String sessionUrl = UriComponentsBuilder.fromHttpUrl(questionnaireUrl).pathSegment(sessionId).pathSegment("status").toUriString();
      ResponseEntity<String> getResponse = client.exchange(sessionUrl, HttpMethod.GET, new HttpEntity<String>(headers()), String.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      String statusValue = getResponse.getBody().substring(1, getResponse.getBody().length() - 1);
      return statusValue.equals("COMPLETED") ? Questionnaire.Metadata.Status.COMPLETED : Questionnaire.Metadata.Status.OPEN;
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  

  @Override
  public Form getForm(String formId) {
    Map<String, String> uriMap = new HashMap<>();
    uriMap.put("formId", formId);
    return client.getForObject(formUrl + "/{formId}", Form.class, uriMap);
  }
  
  private HttpHeaders headers() {
    final var headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    if(authorization != null) {
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


  @Override
  public TagQueryBuilder getTags() {
    
    return new TagQueryBuilder() {
      String formName = null;
      
      @Override
      public TagQueryBuilder formName(String formName) {
        this.formName = formName;
        return this;
      }
      
      @Override
      public List<FormTag> build() {
        DialobAssert.notNull(formUrl, () -> "form url must be defined!");
        DialobAssert.notNull(formName, () -> "form name must be defined!");
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(formUrl)
        .pathSegment(formName)
        .pathSegment("tags");
        ResponseEntity<FormTag[]> response = client.getForEntity(uriBuilder.toUriString(), FormTag[].class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB form tags status was: " + response.getStatusCode() + " but expecting 200!");
        return Arrays.asList(response.getBody());
      }
    };
  }

  @Override
  public DialobProxy createProxy() {
    return new DialobProxy() {
      @Override
      public ResponseEntity<String> reviewGet(String sessionId) {
        return client.getForEntity(questionnaireUrl + "/"+ sessionId, String.class);
      }
      @Override
      public ResponseEntity<String> fillPost(String sessionId, String body) {
        final var headers = headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json");
        final HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        try {
          ResponseEntity<String> response = client.exchange(questionnaireUrl, HttpMethod.POST, requestEntity, String.class);
          return response;
        } catch (Exception e) {
          throw new DialobException(e.getMessage(), e);
        }
      }

      @Override
      public ResponseEntity<String> fillGet(String sessionId) {            
        try {
          UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(sessionUrl).pathSegment("sessionId");
          ResponseEntity<String> response = client.getForEntity(uriBuilder.toUriString(), String.class);
          return response;
        } catch (Exception e) {
          throw new DialobException(e.getMessage(), e);
        }
      }
      @Override
      public ResponseEntity<String> anyRequest(String path, String query, HttpMethod method, String body, Map<String, String> headers) {
        final var uriBuilder = UriComponentsBuilder.fromHttpUrl(serviceUrl)
            .pathSegment(path).query(query);
        final var reqHeaders = new HttpHeaders();
        if(authorization != null) {
          reqHeaders.set("x-api-key", authorization);
        }
        
        headers.entrySet().stream()
        .filter(entry -> 
          entry.getKey().equals(HttpHeaderNames.CONTENT_TYPE.toString()) 
          )
        .forEach((entry) -> reqHeaders.put(entry.getKey(), Arrays.asList(entry.getValue())));
        //reqHeaders.keySet().toArray()
        return client.exchange(uriBuilder.build().toUri(), method, new HttpEntity<String>(headers()), String.class);
      }
    };
  }
  
  
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private ObjectMapper objectMapper;
    private RestTemplate client;
    private String authorization;
    private String url;
    private String submitCallbackUrl;
    private String formUrl;
    private String serviceUrl;
    private String sessionUrl;

    public DialobCommandsImpl build() {
      DialobAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      DialobAssert.notNull(client, () -> "client must be defined!");
      DialobAssert.notNull(url, () -> "url must be defined!");
      DialobAssert.notNull(formUrl, () -> "form url must be defined!");
      DialobAssert.notNull(sessionUrl, () -> "form url must be defined!");
      DialobAssert.notNull(serviceUrl, () -> "serviceUrl must be defined!");
      
      return new DialobCommandsImpl(objectMapper, client, authorization, url, submitCallbackUrl, formUrl, sessionUrl, serviceUrl);
    }
  }
}
