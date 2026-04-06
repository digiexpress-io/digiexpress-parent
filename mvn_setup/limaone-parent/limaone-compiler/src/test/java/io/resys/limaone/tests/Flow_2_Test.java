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


import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.ast.CST_YamlParser;
import io.resys.limaone.spi.ast.MutableYamlFlow;


public class Flow_2_Test {

  @Test
  public void astIndentNormal() {
    
    
    final var parseTree = new CST_YamlParser<>(AST_ParserImpl.builder().props(), new MutableYamlFlow()).parseCST(
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
  public void astDeleteId() {
    final var parseTree = new CST_YamlParser<>(AST_ParserImpl.builder().props(), new MutableYamlFlow()).parseCST(
"""
description: uber description
tasks:
  - second task:
""");
    final var cst = parseTree.getItem1();
    
    Assertions.assertTrue(parseTree.getItem2().isEmpty());
    Assertions.assertEquals(2, cst.getChildren().size());
    
    Assertions.assertEquals("uber description", cst.get("description").getValue());
    Assertions.assertNotNull(cst.get("tasks"));
    Assertions.assertEquals(1, cst.get("tasks").getChildren().size());
    Assertions.assertNotNull(cst.get("tasks").get("second task"));
  }

  @Test
  public void astDeleteAndSetId() throws IOException {
    final var parseTree = new CST_YamlParser<>(AST_ParserImpl.builder().props(), new MutableYamlFlow()).parseCST(
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

}
