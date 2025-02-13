package io.digiexpress.eveli.dialob.spi;

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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.rest.IdAndRevision;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.api.DialobClient.DialobSessionBuilder;
import io.digiexpress.eveli.dialob.spi.DialobAssert.DialobException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DialobSessionBuilderImpl implements DialobClient.DialobSessionBuilder {
  private final ObjectMapper objectMapper;
  private final DialobService dialobService;
  
  private final Map<String, Serializable> context = new HashMap<>();
  private final Map<String, Serializable> answer = new HashMap<>();
  
  private String language;
  private String formId;
  
  @Override
  public DialobSessionBuilder language(String language) {
    this.language = language;
    return this;
  }
  @Override
  public DialobSessionBuilder addContext(String id, Serializable value) {
    this.context.put(id, value);
    return this;
  }
  @Override
  public DialobSessionBuilder addAnswer(String id, Serializable value) {
    this.answer.put(id, value);
    return this;
  }
  @Override
  public DialobSessionBuilder formId(String formId) {
    this.formId = formId;
    return this;
  }

  @Override
  public IdAndRevision build() {
    Assert.notNull(formId, () -> "formId can't be null!");
    final String body = createBody(formId, language, context, answer);
    
    final HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers());
    try {
      ResponseEntity<IdAndRevision> response = dialobService.getQuestionnaires().exchange("", HttpMethod.POST, requestEntity, IdAndRevision.class);
      DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 201!");
      return response.getBody();
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  
  private HttpHeaders headers() {
    final var headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  } 
  
  private String createBody(String formId, 
      String language, Map<String, Serializable> context, Map<String, Serializable> answer) {
    StringWriter stringWriter = new StringWriter();
    try {
      JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(stringWriter);
      jsonGenerator.writeStartObject();
      
      writeMetadata(jsonGenerator, formId, language, false);
      writeContext(jsonGenerator, context);
      writeAnswers(jsonGenerator, answer);
      
      jsonGenerator.writeEndObject();
      jsonGenerator.flush();
      stringWriter.flush();
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
    return stringWriter.toString();
  }

  protected void writeContext(JsonGenerator jsonGenerator, Map<String, Serializable> context) throws IOException {
    jsonGenerator.writeFieldName("context");
    jsonGenerator.writeStartArray();
    for (Map.Entry<String, Serializable> variable : context.entrySet()) {
      if (variable.getValue() == null) {
        continue;
      }
      
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField("id", variable.getKey());
      jsonGenerator.writeStringField("value", objectMapper.convertValue(variable.getValue(), String.class));
      jsonGenerator.writeEndObject();
    }
    jsonGenerator.writeEndArray();
  }

  protected void writeMetadata(JsonGenerator jsonGenerator, String formId, String language, boolean copy) throws IOException {
    jsonGenerator.writeFieldName("metadata");
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("formId", formId);
    if (copy) {
      jsonGenerator.writeStringField("status", "OPEN");
    }
    
    
    if (language != null) {
      jsonGenerator.writeStringField("language", language);
    } else {
      jsonGenerator.writeStringField("language", "fi");
    }
    jsonGenerator.writeEndObject();
  }

  protected void writeAnswers(JsonGenerator jsonGenerator, Map<String, Serializable> answer) throws IOException {
    jsonGenerator.writeFieldName("answers");
    jsonGenerator.writeStartArray();
    for (Map.Entry<String, Serializable> variable : answer.entrySet()) {
      if (variable.getValue() == null) {
        continue;
      }
      
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField("id", variable.getKey());
      jsonGenerator.writeStringField("value", objectMapper.convertValue(variable.getValue(), String.class));
      
      jsonGenerator.writeEndObject();
    }
    jsonGenerator.writeEndArray();
  }
}
