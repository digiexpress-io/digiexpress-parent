package io.resys.hdes.executor.spi.fl;

/*-
 * #%L
 * hdes-executor
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
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.hdes.executor.api.HdesDefContinue.HdesWakeup;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.Trace.TraceEnd;

@Value.Immutable
public interface ContinueNode {
  Trace getParent();
  String getStep();
  Optional<ContinueNested> getNested();
  Optional<ContinueBody> getBody(); 


  interface ContinueBody {}
  
  @Value.Immutable
  interface ContinueNested extends ContinueBody {
    HdesWakeup getWakeup();
    TraceEnd getTrace();
  }
  
  @Value.Immutable
  interface ContinueItration extends ContinueBody {
    Trace getParent();
    List<ContinueCalls> getValues();
  }
  
  @Value.Immutable
  interface ContinueCalls extends ContinueBody {
    Trace getParent();
    List<ContinueCall> getValues();
  }
  
  @Value.Immutable
  interface ContinueCall extends ContinueBody {
    Trace getParent();
    String getDataId();
    Serializable getData(); 
  }
}
