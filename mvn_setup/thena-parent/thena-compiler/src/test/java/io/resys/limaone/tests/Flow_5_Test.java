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
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.test.config.TestUtils;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowProgram.FlowResultLog;


public class Flow_5_Test {

  @Test
  public void programAmlTest() throws IOException {
    final var envir = TestUtils.client.envir().tagName("programAmlTest").addCommand().id("programAmlTest")
        .flow(
            TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
                .type(AstCommandValue.SET_BODY)
                .value(FileUtils.toString(getClass(), "flow/aml-flow.yaml"))
                .build()))
            
            ).build().build();

    // switch 1
    FlowResult flow = TestUtils.client.executor(envir)
        .inputField("whitelist", true)
        .inputField("param1", 1)
        .flow("aml flow").andGetBody();
    // last step
    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, flow.getStatus());
    Assertions.assertEquals("rmInvList", flow.getStepId());
    Assertions.assertEquals("addPartyToInvestigationList -> resolveAmlViolation -> addToWhitelist -> rmInvList", flow.getShortHistory());
    Assertions.assertEquals(4, flow.getLogs().size());
    Assertions.assertEquals(1, flow.getLogs().stream().filter(t -> t.getStepId().equals("resolveAmlViolation")).count());

    FlowResultLog task = flow.getLogs().stream().filter(t -> t.getStepId().equals("resolveAmlViolation")).findFirst().get();
    Assertions.assertNotNull(task);
    
    // switch 2
    flow = TestUtils.client.executor(envir)
        .inputField("whitelist", false)
        .inputField("investigationList", true)
        .inputField("param1", 1)
        .flow("aml flow").andGetBody();
    Assertions.assertEquals("addPartyToInvestigationList -> resolveAmlViolation -> rmInvList", flow.getShortHistory());
    
    // switch 3
    flow = TestUtils.client.executor(envir)
        .inputField("whitelist", false)
        .inputField("investigationList", false)
        .inputField("waitFiuDecision", true)
        .inputField("rmInvList", true)
        .inputField("param1", 1)
        .flow("aml flow").andGetBody();
    
    Assertions.assertEquals("addPartyToInvestigationList -> resolveAmlViolation -> waitFiuDecision -> rmInvList", flow.getShortHistory());
  }

  @Disabled
  @Test
  public void programSelfRefTest() throws IOException {
    final var envir = TestUtils.client.envir()
        .addCommand().id("test1")
        .flow(
            TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
                .type(AstCommandValue.SET_BODY)
                .value(FileUtils.toString(getClass(), "flow/self-ref.yaml"))
                .build()))
            ).build().build();
    
    
    FlowResult flow = TestUtils.client.executor(envir)
        .inputField("restart", true)
        .flow("self ref").andGetBody();
    
    Assertions.assertEquals("[Add party to investigation list, Resolve aml violation, Resolve aml violation-EXCLUSIVE, addToWhitelist, rmInvList, end]", flow.getShortHistory());

  }

}
