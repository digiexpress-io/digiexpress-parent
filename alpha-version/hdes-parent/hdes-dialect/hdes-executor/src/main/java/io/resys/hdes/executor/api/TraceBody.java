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
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.resys.hdes.executor.api.Trace.TraceEnd;

public interface TraceBody extends Serializable {
  interface Returns extends TraceBody {}
  interface Accepts extends TraceBody {}

  @Value.Immutable
  interface PromiseDataId extends Returns {
    String getDataId();
  }
  
  @Value.Immutable
  interface Suspends extends Returns {
    List<Await> getValues();
  }
  
  @Value.Immutable
  interface Await extends Serializable {
    String getDataId();
    StepId getStepId();
    Trace getTrace();
  }
  
  @Value.Immutable
  interface StepId extends Serializable {
    Optional<StepId> getParent();
    Optional<Integer> getIteration();
    String getFlow();
    String getFlowStep();
  }
  
  interface Matched extends TraceBody {
    List<MatchedCondition> getMatches();
    Optional<Returns> getReturns();
  }
  
  interface MatchedCondition extends TraceBody {
    String getId();
    String getSrc();
  }
  
  @Value.Immutable
  interface Maped extends TraceBody {
    List<TraceEnd> getValues();
  }

  @Value.Immutable
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  interface MapedIterator extends TraceBody {
    Serializable getValue();
  }
  
  @Value.Immutable
  interface Nested extends TraceBody, Returns {
    List<Trace> getValues();
  }
}
