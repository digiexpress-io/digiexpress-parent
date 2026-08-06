package io.digiexpress.eveli.client.assets.property_object.api;

import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;

public interface PropertyObjectDescriptor <T> {
  T convertToObject(String content);
  String convertToContent(T o);
  String getPropertyObjectType();
  JsonSchema getSchema(); // if property object is based on JSON then it will provide schema, otherwise null
}
