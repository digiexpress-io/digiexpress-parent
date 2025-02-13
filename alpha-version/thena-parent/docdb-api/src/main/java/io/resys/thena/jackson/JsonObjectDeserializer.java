package io.resys.thena.jackson;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.vertx.core.json.JsonObject;

public class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {

  @SuppressWarnings("unchecked")
  @Override
  public JsonObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    final var text = ctxt.readValue(p, Map.class);
    try {
      return new JsonObject(text);
    } catch (IllegalArgumentException e) {
      throw new InvalidFormatException(p, "Expected a json object", text, JsonObject.class);
    }
  }
}
