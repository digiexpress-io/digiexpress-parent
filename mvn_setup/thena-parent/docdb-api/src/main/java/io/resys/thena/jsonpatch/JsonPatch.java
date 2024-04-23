package io.resys.thena.jsonpatch;

import io.resys.thena.jsonpatch.model.JsonType;
import io.resys.thena.jsonpatch.visitors.ApplyPatch;
import io.resys.thena.jsonpatch.visitors.CreatePatch;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonPatch {
  private final JsonArray value;
  
  public static JsonPatch diff(@Nullable JsonObject previousState, @Nullable JsonObject nextState) {
    final var visitor = new CreatePatch();
    final var jsonType = JsonType.of(previousState, nextState);
    return new JsonPatch(jsonType.accept(visitor));
  }
  public JsonArray getValue() {
    return value;
  }
  
  public JsonObject apply(JsonObject applyOn) {
    final var body = new ApplyPatch().start(this.value, applyOn).close(); 
    return body;
  }
}
