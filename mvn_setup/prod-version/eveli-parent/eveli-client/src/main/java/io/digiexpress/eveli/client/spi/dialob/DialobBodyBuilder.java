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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.DialobCommands;
import io.digiexpress.eveli.client.api.DialobCommands.OneSessionBuilder;
import io.digiexpress.eveli.client.spi.dialob.DialobAssert.DialobException;

public abstract class DialobBodyBuilder implements DialobCommands.OneSessionBuilder {
  private final ObjectMapper objectMapper;
  private final JsonFactory jsonFactory;
  private final String submitCallbackUrl;
  private final Map<String, Serializable> context = new HashMap<>();
  private final Map<String, Serializable> answer = new HashMap<>();
  
  private String formId;
  private String language;
  
  public DialobBodyBuilder(
      ObjectMapper objectMapper,
      JsonFactory jsonFactory,
      String submitCallbackUrl) {
    this.objectMapper = objectMapper;
    this.jsonFactory = jsonFactory;
    this.submitCallbackUrl = submitCallbackUrl;
  }

  @Override
  public OneSessionBuilder language(String language) {
    this.language = language;
    return this;
  }
  @Override
  public OneSessionBuilder addContext(String id, Serializable value) {
    this.context.put(id, value);
    return this;
  }
  @Override
  public OneSessionBuilder addAnswer(String id, Serializable value) {
    this.answer.put(id, value);
    return this;
  }
  
  protected OneSessionBuilder formId(String formId) {
    this.formId = formId;
    return this;
  }
  
  protected String buildBody() {
    Assert.notNull(formId, () -> "formName can't be null!");
    final String body = createBody(formId, language, context, answer);
    return body;
  }
  
  private String createBody(String formId, 
      String language, Map<String, Serializable> context, Map<String, Serializable> answer) {
    StringWriter stringWriter = new StringWriter();
    try {
      JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
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
    jsonGenerator.writeStringField("formRev", "LATEST");
    if (copy) {
      jsonGenerator.writeStringField("status", "OPEN");
    }
    if (language != null) {
      jsonGenerator.writeStringField("language", language);
    } else {
      jsonGenerator.writeStringField("language", "fi");
    }
    if (submitCallbackUrl != null) {
      jsonGenerator.writeStringField("submitUrl", submitCallbackUrl);
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
