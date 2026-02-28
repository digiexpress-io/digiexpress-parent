package io.resys.limaone.spi.parameter;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Serializer;
import io.vertx.core.json.Json;

public class GenericDataTypeSerializer implements Serializer {

  @Override
  public String serialize(Parameter dataType, Object value) {
    if(value == null) {
      return null;
    }
    if(value.getClass().equals(String.class)) {
      return (String) value;
    }
    return Json.CODEC.fromValue(value, String.class);
  }
}
