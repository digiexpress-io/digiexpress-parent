package io.resys.hdes.interpreter.api;

/*-
 * #%L
 * hdes-interpreter
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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
import java.util.HashMap;
import java.util.Map;

import io.resys.hdes.ast.spi.util.Assertions;

public interface HdesAcceptsSupplier {
  Serializable accept(String name);
  
  
  // Map based supplier
  public static class HdesAcceptsMapSupplier implements HdesAcceptsSupplier {
    private final Map<String, Serializable> entries;
    private HdesAcceptsMapSupplier(Map<String, Serializable> entries) {
      super();
      this.entries = entries;
    }

    @Override
    public Serializable accept(String name) {
      return entries.get(name);
    }
    
    public static HdesAcceptsMapSupplier create(Map<String, Serializable> entries) {
      Assertions.notNull(entries, () -> "entries can't be null!");
      return new HdesAcceptsMapSupplier(entries);
    }

    public static Builder builder() {
      return new Builder();
    }
    public static class Builder {
      private final Map<String, Serializable> entries = new HashMap<>();
      
      public Builder put(String name, Serializable value) {
        entries.put(name, value);
        return this;
      }
      
      public HdesAcceptsMapSupplier build() {
        return new HdesAcceptsMapSupplier(entries);
      }
    }
  }

}
