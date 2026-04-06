package io.resys.hdes.client.spi.serializers;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Deserializer;

public class IntlJsonObjectDataTypeDeserializer implements Deserializer {

  private final ObjectMapper objectMapper;

  public IntlJsonObjectDataTypeDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Serializable deserialize(TypeDef dataType, Object value) {
    final List<String> locales = dataType.getValueSet() == null ? Collections.emptyList() : dataType.getValueSet();
    
    if(value == null || "".equals(value)) {
      return new HashMap<>(locales.stream().collect(Collectors.toMap(e -> e, e -> e)));
    }
    
    try {
      final var result = new HashMap<String, String>(objectMapper.readValue((String) value, HashMap.class));
      for(final var locale : locales) {
        if(!result.containsKey(locale)) {
          result.put(locale, "");
        }
      }
      return result;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }
}
