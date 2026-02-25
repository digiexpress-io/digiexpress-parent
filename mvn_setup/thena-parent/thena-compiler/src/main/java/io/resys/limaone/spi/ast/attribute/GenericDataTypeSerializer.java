package io.resys.limaone.spi.ast.attribute;

import io.resys.limaone.ast.attribute.Attribute_AST;
import io.resys.limaone.ast.attribute.Attribute_AST.Serializer;
import io.vertx.core.json.Json;

public class GenericDataTypeSerializer implements Serializer {

  @Override
  public String serialize(Attribute_AST dataType, Object value) {
    if(value == null) {
      return null;
    }
    if(value.getClass().equals(String.class)) {
      return (String) value;
    }
    return Json.CODEC.fromValue(value, String.class);
  }
}
