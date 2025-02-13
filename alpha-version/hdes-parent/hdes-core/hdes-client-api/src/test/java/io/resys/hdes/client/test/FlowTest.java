package io.resys.hdes.client.test;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.resys.hdes.client.api.ast.AstBody.AstCommandMessage;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowNode;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.programs.FlowProgram.FlowExecutionStatus;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResultLog;
import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.client.test.config.TestUtils;


public class FlowTest {

  @Test
  public void astIndentNormal() throws IOException {
    
    final var ast = TestUtils.client.types().flow()
        .srcAdd(1, "id: uber flow")
        .srcAdd(2, "description: uber description")
        .srcAdd(3, "tasks:")
        .srcAdd(4, "  - first task:")
        .srcAdd(5, "  - second task:")
        .build();

    AstFlowNode node = ast.getSrc();
    List<AstCommandMessage> messages = ast.getMessages();
    
    Assertions.assertTrue(messages.isEmpty());

    Assertions.assertEquals(3, node.getChildren().size());

    Assertions.assertNotNull(node.get("id"));
    Assertions.assertEquals("uber flow", node.get("id").getValue());

    Assertions.assertNotNull(node.get("description"));
    Assertions.assertEquals("uber description", node.get("description").getValue());

    Assertions.assertNotNull(node.get("tasks"));
    Assertions.assertEquals(2, node.get("tasks").getChildren().size());

    Assertions.assertNotNull(node.get("tasks").get("first task"));
    Assertions.assertNotNull(node.get("tasks").get("second task"));
  }


  @Test
  public void astDeleteId() throws IOException {
    List<AstCommandMessage> messages = new ArrayList<>();
    final var ast = TestUtils.client.types().flow()
        .srcAdd(1, "id: uber flow")
        .srcAdd(2, "description: uber description")
        .srcAdd(3, "tasks:")
        .srcAdd(4, "  - first task:")
        .srcAdd(5, "  - second task:")
        .srcDel(1)
        .srcDel(4)
        .build();
    
    AstFlowNode node = ast.getSrc();
    
    Assertions.assertTrue(messages.isEmpty());
    Assertions.assertEquals("uber description", node.get("description").getValue());
    Assertions.assertNotNull(node.get("tasks"));
    Assertions.assertEquals(1, node.get("tasks").getChildren().size());
    Assertions.assertNotNull(node.get("tasks").get("first task"));
  }

  @Test
  public void astDeleteAndSetId() throws IOException {
    List<AstCommandMessage> messages = new ArrayList<>();
    final var ast = TestUtils.client.types().flow()
        .srcAdd(1, "id: uber flow")
        .srcAdd(2, "description: uber description")
        .srcAdd(3, "tasks:")
        .srcAdd(4, "  - first task:")
        .srcAdd(5, "  - second task:")
        .srcDel(1)
        .srcAdd(1, "id: uber flow")
        .build();
    
    AstFlowNode node = ast.getSrc();

    Assertions.assertTrue(messages.isEmpty());
    Assertions.assertEquals(3, node.getChildren().size());
    Assertions.assertNotNull(node.get("id"));
    Assertions.assertEquals("uber flow", node.get("id").getValue());
    Assertions.assertNotNull(node.get("description"));
    Assertions.assertEquals("uber description", node.get("description").getValue());

    Assertions.assertNotNull(node.get("tasks"));
    Assertions.assertEquals(2, node.get("tasks").getChildren().size());
    Assertions.assertNotNull(node.get("tasks").get("first task"));
    Assertions.assertNotNull(node.get("tasks").get("second task"));
  }
  
  
  @Test
  public void programAmlTest() throws IOException {
    final var envir = TestUtils.client.envir().addCommand().id("programAmlTest")
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
