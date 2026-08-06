package io.digiexpress.eveli.client.assets.property_object.spi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseJsonPropertyObjectDescriptor <T> implements PropertyObjectDescriptor<T> {

  protected final ObjectMapper mapper;
  protected JsonSchema schema;
  
  public BaseJsonPropertyObjectDescriptor(ObjectMapper mapper) {
    this.mapper = mapper;
    JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
    try {
      this.schema = schemaGen.generateSchema(getObjectClass());
    } catch (JsonMappingException e) {
      schema = null;
      log.error("Error initializing schema", e);
    }
  }
  

  @Override
  public String convertToContent(T o) {
    try {
      return mapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      log.error("Error converting object '{}'",o, e);
    }
    return null;
  }

  @Override
  public T convertToObject(String content) {
    try {
      return mapper.readValue(content, getObjectClass());
    } catch (JsonProcessingException e) {
      log.error("Error converting object '{}'",content, e);
    }
    return null;
  }

  @Override
  public JsonSchema getSchema() {
    return schema;
  }

  
  protected abstract Class<T> getObjectClass();
}
