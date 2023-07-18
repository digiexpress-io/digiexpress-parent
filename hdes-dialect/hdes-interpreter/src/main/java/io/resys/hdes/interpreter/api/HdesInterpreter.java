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
import java.util.Map;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.api.TraceBody.Accepts;
import io.resys.hdes.executor.api.TraceBody.Returns;


public interface HdesInterpreter {
  Executor executor();
  Parser parser();
  
  interface Executor {
    Executor src(String src);
    
    // optional, in case of multiple bodies run the one with given id
    Executor bodyId(String bodyId);
    // optional, way how to identify source as a whole
    Executor externalId(String externalId);
    
    Executor accepts(HdesAcceptsSupplier input);

    TraceEnd build();
  }
  
  interface Parser {
    // optional, way how to identify source as a whole
    Parser externalId(String externalId);
    
    Parser src(String src);
    RootNode build();
  }
  
  interface DataAccessNode extends HdesNode {
    Serializable get(String name);
  }
  
  interface InterpratedNode {}
  
  @Value.Immutable
  interface AcceptsMap extends Accepts {
    Map<String, Serializable> getValues();
  }
  
  @Value.Immutable
  interface ReturnsMap extends Returns {
    Map<String, Serializable> getValues();
  }
  
}
