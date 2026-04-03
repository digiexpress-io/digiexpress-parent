package io.resys.limaone.tests;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.limaone.tests.support.TestTemplate.Deps;


public class Flow_3_Test {

  @SuppressWarnings("unchecked")
  @Test
  public void runAll() {
        final var envir = TestTemplate.compileOneFlow(
"""
id: flow with 2 DT
inputs:
  path:
    required: true
    type: STRING
  operation:
    required: true
    type: STRING

tasks:
  - match all events:
      id: "task_events"
      then: "task_event_queues"
      decisionTable:
        ref: events_dt
        collection: true
        inputs:
          path: path
          op: operation
        
  - map events to queues:
      id: "task_event_queues"
      then: "format_result"
      decisionTable:
        ref: queues_dt
        collection: true
        inputs:
          event: task_events.event
          
  - format result:
      id: "format_result"
      then: "end"
      returns:
        collection: true
        inputs:
          event: task_event_queues._event
          queue: task_event_queues.queue
          queue2: task_event_queues.queue
          queue3: task_event_queues.queue
""", Deps.dtx(events_dt()), Deps.dtx(queues_dt()));
    
    
    final var wrapper = envir.run(Map.of("path", "","operation", ""));
    final var result = wrapper.andGetBody();
    
    Assertions.assertEquals(
"""
FLOW NAME: flow with 2 DT
HISTORY: task_events -> task_event_queues -> (loop)
      task_event_queues -> format_result -> (loop)
        format_result

┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {1} task_events : COMPLETED                                                                                     │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ path:                                            │ event: TASK_CREATED                                               │
│ op:                                              │                                                                   │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {2} task_events[2] : COMPLETED                                                                                  │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ path:                                            │ event: TASK_UPDATED                                               │
│ op:                                              │                                                                   │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {3} task_event_queues : COMPLETED                                                                               │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ event: TASK_CREATED                              │ queue: sms                                                        │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {4} task_event_queues[2] : COMPLETED                                                                            │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ event: TASK_UPDATED                              │ queue: sms                                                        │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {5} format_result : COMPLETED                                                                                   │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│                                                  │ queue2: sms                                                       │
│                                                  │ queue3: sms                                                       │
│                                                  │ event: TASK_CREATED                                               │
│                                                  │ queue: sms                                                        │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {6} format_result[2] : COMPLETED                                                                                │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│                                                  │ queue2: sms                                                       │
│                                                  │ queue3: sms                                                       │
│                                                  │ event: TASK_UPDATED                                               │
│                                                  │ queue: sms                                                        │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
""", wrapper.andEncodePrettily());
    Assertions.assertEquals(result.getStatus(), FlowExecutionStatus.COMPLETED);

    
    final List<Map<String, Object>> values = (List<Map<String, Object>>) result.getReturns().get("");
    Assertions.assertEquals(2, values.size());
    Assertions.assertEquals("{queue2=sms, queue3=sms, event=TASK_CREATED, queue=sms}", values.get(0).toString());
    Assertions.assertEquals("{queue2=sms, queue3=sms, event=TASK_UPDATED, queue=sms}", values.get(1).toString());
  }
  
  public String events_dt() {
    return """
name: events_dt
hitPolicy: ALL
table: |
  | path:STRING | op:STRING | -> | event:STRING |
  |-------------|-----------|----|--------------|
  |             |           |    | TASK_CREATED |
  |             |           |    | TASK_UPDATED |
    """;
  }
  
  
  public String queues_dt() {
    return """
name: queues_dt
hitPolicy: ALL
table: |
  | event:STRING       | -> | queue:STRING |
  |--------------------|----|--------------|
  | in["TASK_CREATED"] |    | sms          |
  | in["TASK_UPDATED"] |    | sms          |
  """;
  }
}
