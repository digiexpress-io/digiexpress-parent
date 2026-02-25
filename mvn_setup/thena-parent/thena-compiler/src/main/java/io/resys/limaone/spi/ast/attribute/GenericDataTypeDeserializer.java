package io.resys.limaone.spi.ast.attribute;

/*-
 * #%L
 * wrench-assets-datatypes
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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

import java.io.Serializable;

import io.resys.limaone.ast.attribute.Attribute_AST;
import io.resys.limaone.ast.attribute.Attribute_AST.Deserializer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class GenericDataTypeDeserializer implements Deserializer {
  private final Class<?> type;

  public GenericDataTypeDeserializer(Class<?> type) {
    super();
    this.type = type;
  }

  @Override
  public Serializable deserialize(Attribute_AST dataType, Object value) {
    if(value == null) {
      return null;
    }
    try {
      return (Serializable) Json.CODEC.fromValue(value, type);
    } catch(Exception e) {
      throw new Attribute_AST_Exception(
          JsonObject.of(
              "msg", e.getMessage(),
              "type", type,
              "value", value
            ).encodePrettily(), e);
    }
  }
}
