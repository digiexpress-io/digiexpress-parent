package io.resys.limaone.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.resys.limaone.ast.DecisionTable_CST.YamlParseTree;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.decisiontable.DecisionParserCST;
import io.resys.limaone.spi.ast.decisiontable.MutableDecisionParseTree;
import io.smallrye.mutiny.tuples.Tuple2;

public class DecisionTableCSTTest {
  private final AST_ParserProps props = AST_ParserImpl.builder().props(); 
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


    final DecisionParserCST parser = new DecisionParserCST(props);
    final Tuple2<MutableDecisionParseTree, List<ModelError>> result = parser.parseCST(yaml);
    
    final YamlParseTree parseTree = result.getItem1();
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

    final DecisionParserCST parser = new DecisionParserCST(props);
    final Tuple2<MutableDecisionParseTree, List<ModelError>> result = parser.parseCST(yaml);
    
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

    final DecisionParserCST parser = new DecisionParserCST(props);
    final Tuple2<MutableDecisionParseTree, List<ModelError>> result = parser.parseCST(yaml);
    
    final YamlParseTree parseTree = result.getItem1();
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