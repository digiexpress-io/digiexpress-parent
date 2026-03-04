package io.resys.limaone.tests;


import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.ast.CST_YamlParser;
import io.resys.limaone.spi.ast.flow.MutableYamlFlow;


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
