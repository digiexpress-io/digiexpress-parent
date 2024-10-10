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
import java.util.Optional;

import javax.annotation.Nullable;

import io.resys.hdes.executor.api.TraceBody.Accepts;
import io.resys.hdes.executor.api.TraceBody.Maped;
import io.resys.hdes.executor.api.TraceBody.MapedIterator;
import io.resys.hdes.executor.api.TraceBody.Matched;
import io.resys.hdes.executor.api.TraceBody.MatchedCondition;
import io.resys.hdes.executor.api.TraceBody.Nested;
import io.resys.hdes.executor.api.TraceBody.PromiseDataId;
import io.resys.hdes.executor.api.TraceBody.Returns;
import io.resys.hdes.executor.api.TraceBody.Suspends;

public interface Trace extends Serializable {
  
  String getId();
  long getTime();
  Optional<Trace> getParent();
  TraceBody getBody();

  interface TraceStep extends Trace { }
  
  interface TraceEnd extends Trace {
    @Nullable
    Returns getBody();  
    @Nullable
    Suspends getSuspends();
  }
  
  interface TracePromise extends TraceEnd {
    PromiseDataId getBody();  
  }
  
  interface TraceTree {
    Optional<TraceTree> getParent();
    TraceTree next(Trace trace);
    Trace getValue();
  }
  
  interface TraceVisitor<T, R> {
    R visitBody(TraceEnd end);
    
    T visitTrace(Trace trace, TraceTree tree);
    T visitTraceBody(TraceBody traceBody, TraceTree treee);
    T visitAccepts(Accepts accepts, TraceTree tree);
    T visitReturns(Returns accepts, TraceTree tree);
    
    T visitCalls(Nested calls, TraceTree tree);
    T visitIteration(Maped iteration, TraceTree tree);
    T visitIterator(MapedIterator iterator, TraceTree tree);
    
    T visitHitPolicy(Matched hitpolicy, TraceTree tree);
    T visitHitPolicyMatch(MatchedCondition when, TraceTree tree);
  }
}
