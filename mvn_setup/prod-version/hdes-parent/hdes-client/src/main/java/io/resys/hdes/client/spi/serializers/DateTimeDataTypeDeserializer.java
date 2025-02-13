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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Deserializer;

public class DateTimeDataTypeDeserializer implements Deserializer {

  private final ObjectMapper objectMapper;

  public DateTimeDataTypeDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }

  @Override
  public Serializable deserialize(TypeDef dataType, Object value) {
    if(value == null) {
      return null;
    }

    String result = objectMapper.convertValue(value, String.class);
    return parseLocalDateTime(result);
  }

  public static LocalDateTime parseLocalDateTime(String date) {
    try {
      return ZonedDateTime.parse(date).toLocalDateTime();
    } catch(Exception e) {
      throw new IllegalArgumentException("Incorrect date time: '" + date + "', correct format: YYYY-MM-DDThh:mm:ssTZD, example: 2017-07-03T00:00:00Z!");
    }
  }
}
