package io.digiexpress.tagomi.spi.json;

import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.digiexpress.tagomi.spi.TagomiStoreConfig;
import io.vertx.core.json.JsonObject;



public class FromJsonObject implements TagomiStoreConfig.Deserializer {

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IsTagomiObject> T fromString(JsonObject value) {
    return (T) value.mapTo(IsTagomiObject.class);
  }
}
