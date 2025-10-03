package io.digiexpress.tagomi.spi.json;

import io.digiexpress.tagomi.api.TagomiStoreConfig;
import io.digiexpress.tagomi.api.entities.TagomiContainer.IsTagomiObject;
import io.vertx.core.json.JsonObject;

public class ToJsonObject implements TagomiStoreConfig.Serializer {

  @Override
  public JsonObject toString(IsTagomiObject entity) {
    return JsonObject.mapFrom(entity);
  }
}
