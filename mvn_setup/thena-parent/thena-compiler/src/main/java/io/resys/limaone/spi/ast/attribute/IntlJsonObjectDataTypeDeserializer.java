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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.limaone.ast.attribute.Attribute_AST;
import io.resys.limaone.ast.attribute.Attribute_AST.Deserializer;
import io.vertx.core.json.JsonObject;

public class IntlJsonObjectDataTypeDeserializer implements Deserializer {
  @Override
  public Serializable deserialize(Attribute_AST dataType, Object value) {
    final List<String> locales = dataType.getValueSet() == null ? Collections.emptyList() : dataType.getValueSet();
    
    if(value == null || "".equals(value)) {
      return new HashMap<>(locales.stream().collect(Collectors.toMap(e -> e, e -> e)));
    }
    
    final var result = new JsonObject((String) value).getMap();
    for(final var locale : locales) {
      if(!result.containsKey(locale)) {
        result.put(locale, "");
      }
    }
    return (Serializable) result;

  }
}
