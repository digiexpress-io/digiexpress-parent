package io.resys.thena.tasks.dev.app.events;

import io.vertx.core.json.JsonObject;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class UIEventEncoder implements Encoder.Text<UIEvent> {
  @Override
  public String encode(UIEvent object) throws EncodeException {
    return JsonObject.mapFrom(object).encode();
  }
}
