package io.digiexpress.eveli.client.assets.property_object.api;

import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;

import io.resys.limaone.model.Model;
import io.resys.limaone.model.PropertyObject;

public interface PropertyObjectDescriptor <T extends BasePropertyObject> {
  T convertToObject(Model<PropertyObject> m);
  String getPropertyObjectType();
  JsonSchema getSchema(); // if property object is based on JSON then it will provide schema, otherwise null
  boolean validateBody(String input);
}
