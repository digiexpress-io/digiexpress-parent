package io.resys.hdes.executor.api;

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

import org.immutables.value.Value;

import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.api.TraceBody.Accepts;

public interface HdesDefContinue<A extends Accepts, E extends TraceEnd> extends HdesRunnable {

  @Value.Immutable
  interface HdesWakeup extends Serializable {
    List<HdesWakeupValue> getValues();
  }
  
  @Value.Immutable
  interface HdesWakeupValue extends Serializable {
    String getDataId();
    Serializable getData();
  }
  
  E apply(E trace, HdesWakeup wakeup);
}
