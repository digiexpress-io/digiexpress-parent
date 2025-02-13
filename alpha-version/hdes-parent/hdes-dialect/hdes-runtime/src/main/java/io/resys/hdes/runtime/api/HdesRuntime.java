package io.resys.hdes.runtime.api;

/*-
 * #%L
 * hdes-runtime
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

import javax.tools.Diagnostic;

import org.immutables.value.Value;

import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.executor.api.HdesRunnable;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.TraceBody;

public interface HdesRuntime {
  
  interface Builder {
    Builder from(List<Resource> resources);
    RuntimeEnvir build();
  }

  interface RuntimeEnvir {
    RuntimeTask get(String name) throws ClassNotFoundException;
    List<Diagnostic<?>> getDiagnostics();
  }
  
  @Value.Immutable
  interface RuntimeTask {
    String getName();
    HdesRunnable getValue();
    Class<? extends TraceBody.Accepts> getAccepts();
    Class<? extends TraceBody.Returns> getReturns();
    Class<? extends Trace.TraceEnd> getEnds();
  }
}
