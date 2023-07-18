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
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.interpreter.api.HdesAcceptsSupplier;
import io.resys.hdes.interpreter.api.HdesInterpreter;
import io.resys.hdes.interpreter.api.HdesInterpreter.Executor;
import io.resys.hdes.interpreter.api.HdesInterpreterException;
import io.resys.hdes.interpreter.spi.visitors.InterpraterRootNodeVisitor;

public class HdesInterpreterExecutor implements HdesInterpreter.Executor {

  private HdesAcceptsSupplier input;
  private String src;
  private String bodyId;
  private String externalId;
  
  @Override
  public Executor src(String src) {
    this.src = src;
    return this;
  }
  @Override
  public Executor bodyId(String bodyId) {
    this.bodyId = bodyId;
    return this;
  }
  @Override
  public Executor externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }
  @Override
  public Executor accepts(HdesAcceptsSupplier input) {
    this.input = input;
    return this;
  }
  @Override
  public TraceEnd build() {
    Assertions.notNull(src, () -> "src can't be null!");
    Assertions.notNull(input, () -> "input can't be null!");
    
    final RootNode root = RootNodeFactory.builder()
        .add().externalId(externalId).src(src)
        .build();
    
    if(root.getBody().values().isEmpty()) {
      throw new HdesInterpreterException("Parsed content has no nodes to execute!");
    }
    if(root.getBody().values().size() > 1 && bodyId == null) {
      throw new HdesInterpreterException("Parsed content has: " + root.getBody().values().size() + " nodes but expecting only 1 node because runnable node 'bodyId' is not defined!");
    }
    if(bodyId != null && !root.getBody().containsKey(bodyId)) {
      throw new HdesInterpreterException("Parsed content does not contain elements with bodyId: '" + bodyId + "'!");
    }
    
    final BodyNode body = bodyId != null ? root.getBody().get(bodyId) : root.getBody().values().iterator().next();
    return InterpraterRootNodeVisitor.builder().root(root).main(body).input(input).build();
  }

}
