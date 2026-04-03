package io.resys.limaone.spi.program.expression;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import io.vertx.core.json.JsonArray;

public class OperationString {

  public static Operation<String> build(String value, Consumer<String> constants) {
    try {
      if(value.indexOf("[") > -1) {
        
        final List<String> values = new ArrayList<>();
        new JsonArray(value.substring(value.indexOf("["))).forEach(item -> {
          final String stringValue = (String) item;
          values.add(stringValue);
          constants.accept(stringValue);
        });
        values.forEach(constants);
        
        boolean patternMatching = value.startsWith("qin") ? true : false;
        if(patternMatching) {
          return xin(values);
        }
        
        boolean contains = value.startsWith("in") ? true : false;
        return contains ? in(values) : notIn(values);
      } else {
        constants.accept(value);
        return in(Arrays.asList(value));
      }
    } catch(Exception e) {
      throw new IllegalArgumentException("Incorrect string expression: " + value + "!", e);
    }
  }
  private static Operation<String> xin(Collection<String> constant) {
    return (String parameter) -> {
      
      for(final var value : constant) {
        if(Router.builderWithSlash().queueName(parameter).routingKey(value).isMatch()) {
          return true;
        }
      }
      
      return false;
    };
  }    
  private static Operation<String> in(Collection<String> constant) {
    return (String parameter) -> {
      
      return constant.contains(parameter);
    };
  }
  private static Operation<String> notIn(Collection<String> constant) {
    return (String parameter) -> !constant.contains(parameter);
  }
   
}
