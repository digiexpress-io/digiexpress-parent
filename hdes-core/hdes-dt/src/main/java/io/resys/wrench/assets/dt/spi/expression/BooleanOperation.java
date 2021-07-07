package io.resys.wrench.assets.dt.spi.expression;

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

import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BooleanOperation {


  public static Builder builder(ObjectMapper objectMapper) {
    return new Builder(objectMapper);
  }

  public static class Builder {
    @SuppressWarnings("unused")
    private final ObjectMapper objectMapper;

    private Builder(ObjectMapper objectMapper) {
      super();
      this.objectMapper = objectMapper;
    }

    public Operation<Boolean> build(String value, Consumer<String> constants) {
      constants.accept(value);
      return eq(Boolean.parseBoolean(value));
    }
    
    private static Operation<Boolean> eq(boolean constant) {
      return (Boolean parameter) -> constant == parameter;
    } 
  }
}
