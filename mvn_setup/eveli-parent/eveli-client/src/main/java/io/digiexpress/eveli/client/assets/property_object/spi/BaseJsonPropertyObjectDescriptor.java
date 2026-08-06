package io.digiexpress.eveli.client.assets.property_object.spi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

import io.digiexpress.eveli.client.assets.property_object.api.BasePropertyObject;
import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptor;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.PropertyObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseJsonPropertyObjectDescriptor <T extends BasePropertyObject> implements PropertyObjectDescriptor<T> {

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
  public T convertToObject(Model<PropertyObject> propObject) {
    try {
      T result = mapper.readValue(propObject.getBody().getContent(), getObjectClass());
      result.setName(propObject.getBody().getName());
      result.setId(propObject.getId());
      return result;
    } catch (JsonProcessingException e) {
      log.error("Error converting object '{}'",propObject, e);
    }
    return null;
  }

  @Override
  public JsonSchema getSchema() {
    return schema;
  }

  
  protected abstract Class<T> getObjectClass();
}
