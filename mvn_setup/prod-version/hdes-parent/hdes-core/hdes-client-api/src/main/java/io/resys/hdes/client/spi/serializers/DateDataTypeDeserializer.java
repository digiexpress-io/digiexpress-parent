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
import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Deserializer;

public class DateDataTypeDeserializer implements Deserializer {

  private final ObjectMapper objectMapper;

  public DateDataTypeDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }

  @Override
  public Serializable deserialize(TypeDef dataType, Object value) {
    if(value == null) {
      return null;
    }
    if(value.getClass() == LocalDate.class) {
      return (Serializable) value;
    }

    Serializable result = objectMapper.convertValue(value, String.class);
    return parseLocalDate((String) result);
  }

  public static LocalDate parseLocalDate(String date) {
    try {
      if(date.length() > 10) {
        return LocalDate.parse(date.substring(0, 10));
      }
      return LocalDate.parse(date);
    } catch(Exception e) {
      throw new IllegalArgumentException("Incorrect date: '" + date + "', correct format: YYYY-MM-DD, example: 2017-07-03!");
    }
  }
}
