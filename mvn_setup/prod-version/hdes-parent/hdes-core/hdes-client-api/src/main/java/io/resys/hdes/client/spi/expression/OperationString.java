package io.resys.hdes.client.spi.expression;

/*-
 * #%L
 * wrench-assets-dt
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.exceptions.DecisionAstException;

public class OperationString {


  public static Builder builder(ObjectMapper objectMapper) {
    return new Builder(objectMapper);
  }

  public static class Builder {
    private final ObjectMapper objectMapper;

    private Builder(ObjectMapper objectMapper) {
      super();
      this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public Operation<String> build(String value, Consumer<String> constants) {

      try {
        if(value.indexOf("[") > -1) {
          List<String> values = objectMapper.readValue(value.substring(value.indexOf("[")), List.class);
          values.forEach(constants);
          boolean contains = value.startsWith("in") ? true : false;
          return contains ? in(values) : notIn(values);
        } else {
          constants.accept(value);
          return in(Arrays.asList(value));
        }
      } catch(IOException e) {
        throw new DecisionAstException("Incorrect string expression: " + value + "!", e);
      }
    }
    
    private static Operation<String> in(Collection<String> constant) {
      return (String parameter) -> constant.contains(parameter);
    }
    private static Operation<String> notIn(Collection<String> constant) {
      return (String parameter) -> !constant.contains(parameter);
    }
    
  }
}
