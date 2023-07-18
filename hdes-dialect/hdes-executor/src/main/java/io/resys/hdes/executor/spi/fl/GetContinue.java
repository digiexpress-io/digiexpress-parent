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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.hdes.executor.api.HdesDefContinue.HdesWakeup;
import io.resys.hdes.executor.api.HdesDefContinue.HdesWakeupValue;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.api.TraceBody.Await;
import io.resys.hdes.executor.api.TraceBody.Suspends;
import io.resys.hdes.executor.spi.exceptions.FlowDataIdException;
import io.resys.hdes.executor.spi.exceptions.FlowStepIdException;
import io.resys.hdes.executor.spi.fl.ContinueNode.ContinueBody;
import io.resys.hdes.executor.spi.fl.ContinueNode.ContinueCall;

public class GetContinue {

  private TraceEnd trace;
  
  private GetContinue(TraceEnd trace) {
    super();
    this.trace = trace;
  }
  
  public ContinueNode body(HdesWakeup body) {
    final Suspends suspends = trace.getSuspends();
    Map<String, Await> awaits = suspends.getValues().stream()
      .collect(Collectors.toMap(
          e -> e.getDataId(),
          e -> e));
    
    final Trace previousStep = trace.getParent().get();
    final var continueNode = ImmutableContinueNode.builder();
    String continueStepId = null;
    
    final List<ContinueCall> calls = new ArrayList<>();
    
    for(HdesWakeupValue wakeup : body.getValues()) {
      final var dataId = wakeup.getDataId();
      if(!awaits.containsKey(dataId)) {
        throw new FlowDataIdException(dataId, trace);
      }
      final var await = awaits.get(dataId);
      final var stepId = await.getStepId();
      
      if(continueStepId == null) {
        continueStepId = stepId.getFlowStep();  
      } else if(continueStepId.equals(stepId.getFlowStep())) {
        throw new FlowStepIdException(dataId, stepId.getFlowStep(), trace);
      }
      final var continueCall = ImmutableContinueCall.builder()
          .data(wakeup.getData())
          .dataId(wakeup.getDataId())
          .parent(await.getTrace())
          .build();
      calls.add(continueCall);
    }
    
    ContinueBody continueBody;
    if(calls.size() == 1) {
      continueBody = calls.iterator().next();
    } else {
      // TODO
      continueBody = ImmutableContinueCalls.builder().parent(null).values(calls).build();
    }
    
    
    return continueNode
        .parent(previousStep)
        .step(continueStepId)
        .body(continueBody)
        .build();
  }

  public static GetContinue from(TraceEnd trace) {
    return new GetContinue(trace);
  }
  
}
