package io.resys.limaone.tests;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.tests.support.TestTemplate;


public class Flow_5_Test {

  @Test
  public void programAmlTest() throws IOException {
    final var envir = TestTemplate.compileOneFlow(
"""
id: aml flow

inputs:
  param1:
    required: true
    type: INTEGER
  whitelist:
    required: true
    type: BOOLEAN
  param2:
    required: false
    type: STRING

tasks:
  - Add party to investigation list:
      id: "addPartyToInvestigationList"
      then: "resolveAmlViolation"

  - Resolve aml violation:
      id: "resolveAmlViolation"
      switch:
        - add transaction to source whitelist?:
            when: "whitelist == true"
            then: "addToWhitelist"
        - remove party from investigation list?:
            when: "investigationList == true"
            then: "rmInvList"
        - wait for fiu decision:
            when: "waitFiuDecision == true"
            then: "waitFiuDecision"
  
  - Add transaction to source whitelist:
      id: "addToWhitelist"
      then: "rmInvList"

  - Remove party from investgation list:
      id: "rmInvList"
      then: "end"

  - Wait for fiu deicision:
      id: "waitFiuDecision"
      switch:
        - remove party from investigation list?:
            when: "rmInvList == true"
            then: "rmInvList"
        - default gateway to the end:
            then: "end"        
""");

    // switch 1
    final var wrapper = envir.run(Map.of("whitelist", true, "param1", 1));
    Assertions.assertEquals(
"""
FLOW NAME: aml flow
HISTORY: addPartyToInvestigationList -> resolveAmlViolation -> addToWhitelist -> rmInvList

┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {1} addPartyToInvestigationList : COMPLETED                                                                     │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {2} resolveAmlViolation : COMPLETED                                                                             │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ whitelist: true                                  │ whitelist == true: true                                           │
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {3} addToWhitelist : COMPLETED                                                                                  │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ STEP {4} rmInvList : COMPLETED                                                                                       │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
│ ACCEPTS                                          │ RETURNS                                                           │
├──────────────────────────────────────────────────┬───────────────────────────────────────────────────────────────────┤
└──────────────────────────────────────────────────┴───────────────────────────────────────────────────────────────────┘
""", wrapper.andEncodePrettily());
    FlowResult flow = wrapper.andGetBody();
    
    
    // last step
    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("rmInvList", flow.getStepId());
    Assertions.assertEquals("addPartyToInvestigationList -> resolveAmlViolation -> addToWhitelist -> rmInvList", flow.getShortHistory());
    Assertions.assertEquals(4, flow.getLogs().size());
    Assertions.assertEquals(1, flow.getLogs().stream().filter(t -> t.getStepId().equals("resolveAmlViolation")).count());

    FlowResultLog task = flow.getLogs().stream().filter(t -> t.getStepId().equals("resolveAmlViolation")).findFirst().get();
    Assertions.assertNotNull(task);
    
    // switch 2
    flow = envir.run(Map.of(
        "whitelist", false,
        "investigationList", true,
        "param1", 1)).andGetBody();
    Assertions.assertEquals("addPartyToInvestigationList -> resolveAmlViolation -> rmInvList", flow.getShortHistory());
    
    // switch 3
    flow = envir.run(Map.of(
        "whitelist", false,
        "investigationList", false,
        "waitFiuDecision", true,
        "rmInvList", true,
        "param1", 1)).andGetBody();
    
    Assertions.assertEquals("addPartyToInvestigationList -> resolveAmlViolation -> waitFiuDecision -> rmInvList", flow.getShortHistory());
  }

  @Disabled
  @Test
  public void programSelfRefTest() throws IOException {
    final var envir = TestTemplate.compileOneFlow(
"""
id: self ref
tasks:
  - Add party to investigation list:
      id: "addToInvList"
      then: "addToWhitelist"
  
  - Add transaction to source whitelist:
      id: "addToWhitelist"
      then: "rmInvList"

  - Remove party from investgation list:
      id: "rmInvList"
      then: "waitFiuDecision"

  - Wait for fiu deicision:
      id: "waitFiuDecision"
      switch:
        - restart:
            when: "restart == true"
            then: "addToInvList"
        - default gateway to the end:
            then: "end"
""");
    
    FlowResult flow = envir.run(Map.of("restart", true)).andGetBody();
    Assertions.assertEquals("[Add party to investigation list, Resolve aml violation, Resolve aml violation-EXCLUSIVE, addToWhitelist, rmInvList, end]", flow.getShortHistory());

  }

}
