package io.resys.hdes.compiler.spi;

/*-
 * #%L
 * hdes-compiler
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

import java.util.List;

import io.resys.hdes.ast.api.RootNodeFactory;
import io.resys.hdes.compiler.api.HdesCompiler;

public class GenericHdesCompiler implements HdesCompiler {
  @Override
  public ResourceParser parser() {
    final var builder = RootNodeFactory.builder();
    return new ResourceParser() {
      @Override
      public List<Resource> build() {
        return new CompilerRootNodeVisitor().visitBody(builder.build());
      }
      @Override
      public ResourceParser add(String filename, String src) {
        builder.add().externalId(filename).src(src);
        return this;
      }
    };
  }
  

  public static Config config() {
    return new Config();
  }

  public static class Config {
    public GenericHdesCompiler build() {
      return new GenericHdesCompiler();
    }
  }
}
