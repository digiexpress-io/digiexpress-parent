package io.resys.hdes.interpreter.spi;

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

import io.resys.hdes.ast.api.RootNodeFactory;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.interpreter.api.HdesInterpreter;

public class ImmutableHdesInterpreter implements HdesInterpreter {
  @Override
  public Executor executor() {
    return new HdesInterpreterExecutor();
  }

  @Override
  public Parser parser() {
    return new Parser() {
      private String src;
      private String externalId;
      @Override
      public Parser src(String src) {
        this.src = src;
        return this;
      }
      @Override
      public Parser externalId(String externalId) {
        this.externalId = externalId;
        return this;
      }
      
      @Override
      public RootNode build() {
        Assertions.notNull(src, () -> "src can't be null!");
        return RootNodeFactory.builder().ignoreErrors()
          .add().externalId(externalId).src(src)
          .build();
      }
    };
  }

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    public ImmutableHdesInterpreter build() {
      return new ImmutableHdesInterpreter();
    }
  }
}
