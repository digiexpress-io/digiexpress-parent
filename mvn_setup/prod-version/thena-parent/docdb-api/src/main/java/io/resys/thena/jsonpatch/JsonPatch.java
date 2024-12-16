package io.resys.thena.jsonpatch;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
