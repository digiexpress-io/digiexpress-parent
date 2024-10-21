package io.digiexpress.eveli.client.spi;

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
import io.digiexpress.eveli.client.api.DialobCommands.QuestionnaireBuilder;
import io.digiexpress.eveli.client.spi.asserts.DialobAssert.DialobException;

public abstract class DialobBodyBuilder implements DialobCommands.QuestionnaireBuilder {
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
  public QuestionnaireBuilder language(String language) {
    this.language = language;
    return this;
  }
  @Override
  public QuestionnaireBuilder addContext(String id, Serializable value) {
    this.context.put(id, value);
    return this;
  }
  @Override
  public QuestionnaireBuilder addAnswer(String id, Serializable value) {
    this.answer.put(id, value);
    return this;
  }
  
  protected QuestionnaireBuilder formId(String formId) {
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
