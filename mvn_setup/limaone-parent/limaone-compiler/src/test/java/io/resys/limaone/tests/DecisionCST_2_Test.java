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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.resys.limaone.ast.DecisionTable_CST.YamlDecision;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.CST_YamlParser;
import io.resys.limaone.spi.ast.decisiontable.MutableYamlDecision;
import io.resys.limaone.tests.support.TestTemplate;
import io.smallrye.mutiny.tuples.Tuple2;

public class DecisionCST_2_Test {
  private final AST_ParserProps props = TestTemplate.props; 
  @Test
  public void testBasicDecisionTableParsing() {
    final String yaml = """
        name: Customer Risk Assessment
        description: Determines customer risk level based on age and income
        hitPolicy: FIRST
        valueSets:
          riskLevel: low, medium, high
          ageGroup: young, middle, senior
        table: |
          | age:NUMBER    | income:NUMBER | ageGroup:STRING  | -> | riskLevel:STRING   |
          |---------------|---------------|------------------|----|--------------------|
          | < 25          | < 30000       | young            |    | high               |
          | < 25          | >= 30000      | young            |    | medium             |
          | >= 25 && < 55 | < 50000       | middle           |    | medium             |
          | >= 25 && < 55 | >= 50000      | middle           |    | low                |
          | >= 55         | *             | senior           |    | low                |
        """;


    final var parser = new CST_YamlParser<MutableYamlDecision>(props, new MutableYamlDecision());;
    final Tuple2<MutableYamlDecision, List<ModelError>> result = parser.parseCST(yaml);
    
    final YamlDecision parseTree = result.getItem1();
    final List<ModelError> errors = result.getItem2();

    // Verify no parsing errors
    assertTrue(errors.isEmpty(), "Should have no parsing errors: " + errors);

    // Verify basic properties
    assertNotNull(parseTree.getName());
    assertEquals("Customer Risk Assessment", parseTree.getName().getValue());
    
    assertNotNull(parseTree.getDescription());
    assertEquals("Determines customer risk level based on age and income", parseTree.getDescription().getValue());
    
    assertNotNull(parseTree.getHitPolicy());
    assertEquals("FIRST", parseTree.getHitPolicy().getValue());

    // Verify value sets
    assertEquals(2, parseTree.getValueSetNodes().size());
    assertTrue(parseTree.getValueSetNodes().containsKey("riskLevel"));
    
    final var riskLevelSet = parseTree.getValueSetNodes().get("riskLevel");
    assertEquals(3, riskLevelSet.getValues().size());
    assertTrue(riskLevelSet.getValues().contains("low"));
    assertTrue(riskLevelSet.getValues().contains("medium"));
    assertTrue(riskLevelSet.getValues().contains("high"));

    // Verify table structure
    assertNotNull(parseTree.getTable());
    final var table = parseTree.getTable();
    
    // Verify headers
    assertEquals(4, table.getHeaders().size());
    assertEquals(3, table.getInputHeaders().size());
    assertEquals(1, table.getOutputHeaders().size());
    
    assertTrue(table.getInputHeaders().containsKey("age"));
    assertTrue(table.getInputHeaders().containsKey("income"));
    assertTrue(table.getInputHeaders().containsKey("ageGroup"));
    assertTrue(table.getOutputHeaders().containsKey("riskLevel"));

    // Verify rows
    assertEquals(5, table.getRows().size());
    
    final var firstRow = table.getRows().iterator().next();
    assertEquals(4, firstRow.getCells().size());
    assertTrue(firstRow.getCellsByHeader().containsKey("age"));
    assertEquals("< 25", firstRow.getCellsByHeader().get("age").getExpression());
  }

  @Test
  public void testInvalidIndentation() {
    final String yaml = """
        name: Test Table
         description: Invalid indent
        hitPolicy: FIRST
        """;

    final var parser = new CST_YamlParser<MutableYamlDecision>(props, new MutableYamlDecision());
    final Tuple2<MutableYamlDecision, List<ModelError>> result = parser.parseCST(yaml);
    
    final List<ModelError> errors = result.getItem2();
    
    // Should have indentation error
    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).getMsg().contains("Incorrect indent"));
  }

  @Test
  public void testMinimalDecisionTable() {
    final String yaml = """
        name: Minimal Table
        hitPolicy: ALL
        table: |
          | input:STRING | -> | output:STRING |
          |--------------|----|---------------| 
          | value1       |    | result1       |
        """;

    final var parser = new CST_YamlParser<MutableYamlDecision>(props, new MutableYamlDecision());
    final Tuple2<MutableYamlDecision, List<ModelError>> result = parser.parseCST(yaml);
    
    final YamlDecision parseTree = result.getItem1();
    final List<ModelError> errors = result.getItem2();

    assertTrue(errors.isEmpty());
    assertEquals("Minimal Table", parseTree.getName().getValue());
    assertEquals("ALL", parseTree.getHitPolicy().getValue());
    
    final var table = parseTree.getTable();
    assertEquals(1, table.getInputHeaders().size());
    assertEquals(1, table.getOutputHeaders().size());
    assertEquals(1, table.getRows().size());
  }
}
