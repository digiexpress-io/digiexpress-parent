package io.resys.thena.jackson;

/*-
 * #%L
 * thena-sql-client-pg
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

import io.vertx.core.json.JsonArray;

@SuppressWarnings({"rawtypes"})
public class JsonArrayDeserializer extends StdDelegatingDeserializer<JsonArray> {

  private static final long serialVersionUID = -1173207659510895675L;

  public JsonArrayDeserializer() {
    super(new StdConverter<List<?>, JsonArray>() {
      @Override
      public JsonArray convert(List list) {
        return new JsonArray(list);
      }
    });
  }

  @Override
  protected StdDelegatingDeserializer<JsonArray> withDelegate(Converter<Object, JsonArray> converter, JavaType delegateType, JsonDeserializer<?> delegateDeserializer) {
    return new StdDelegatingDeserializer<>(converter, delegateType, delegateDeserializer);
  }
}
