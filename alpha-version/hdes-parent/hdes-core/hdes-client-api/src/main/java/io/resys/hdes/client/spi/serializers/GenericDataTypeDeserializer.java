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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Deserializer;
import io.resys.hdes.client.api.exceptions.DataTypeException;

public class GenericDataTypeDeserializer implements Deserializer {

  private final ObjectMapper objectMapper;
  private final Class<?> type;

  public GenericDataTypeDeserializer(ObjectMapper objectMapper, Class<?> type) {
    super();
    this.objectMapper = objectMapper;
    this.type = type;
  }

  @Override
  public Serializable deserialize(TypeDef dataType, Object value) {
    if(value == null) {
      return null;
    }
    try {
      return (Serializable) objectMapper.convertValue(value, type);
    } catch(Exception e) {
      throw new DataTypeException(dataType, value, e);
    }
  }
}
