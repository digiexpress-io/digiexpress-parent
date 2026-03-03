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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.resys.hdes.client.api.ast.AstBody.AstCommandMessage;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowNode;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.test.config.TestUtils;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowProgram.FlowResultLog;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.ast.FlowParserCST;


public class Flow_2_Test {

  @Test
  public void astIndentNormal() throws IOException {
    
    
    final var parseTree = new FlowParserCST(AST_ParserImpl.builder().props()).parseCST(
"""
id: uber flow
description: uber description
tasks:
  - first task:
  - second task:
""");
    final var cst = parseTree.getItem1();
    
    Assertions.assertTrue(parseTree.getItem2().isEmpty());

    Assertions.assertEquals(3, cst.getChildren().size());

    Assertions.assertNotNull(cst.get("id"));
    Assertions.assertEquals("uber flow", cst.get("id").getValue());

    Assertions.assertNotNull(cst.get("description"));
    Assertions.assertEquals("uber description", cst.get("description").getValue());

    Assertions.assertNotNull(cst.get("tasks"));
    Assertions.assertEquals(2, cst.get("tasks").getChildren().size());

    Assertions.assertNotNull(cst.get("tasks").get("first task"));
    Assertions.assertNotNull(cst.get("tasks").get("second task"));
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

}
