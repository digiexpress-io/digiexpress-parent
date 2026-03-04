package io.resys.limaone.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.resys.limaone.ast.DecisionTable_CST.YamlParseTree;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.DecisionTable.StatementType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.decisiontable.DecisionCSTToCommands;
import io.resys.limaone.spi.ast.decisiontable.DecisionParserCST;
import io.resys.limaone.spi.ast.decisiontable.MutableYamlParseTree;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;

public class DecisionCST_1_Test {
  private final AST_ParserProps props = AST_ParserImpl.builder().props();

  @Test
  public void testBasicCSTToCommands() {
    final String yaml = """
        name: Risk Assessment
        description: Customer risk evaluation
        hitPolicy: FIRST
        valueSets:
          riskLevel: low, medium, high
        table: |
          | age:NUMBER | income:NUMBER | -> | riskLevel:STRING |
          |------------|---------------|----|--------------------|
          | < 25       | < 30000       |    | high               |
          | >= 25      | >= 50000      |    | low                |
        """;

    final DecisionParserCST parser = new DecisionParserCST(props);
    final Tuple2<MutableYamlParseTree, List<ModelError>> result = parser.parseCST(yaml);
    final YamlParseTree parseTree = result.getItem1();
    
    assertTrue(result.getItem2().isEmpty(), "Should have no parsing errors");
    
    final List<DecisionStatement> commands = new DecisionCSTToCommands().convert(parseTree);
    
    // Verify we have the expected number of commands
    // 1 SET_NAME + 1 SET_DESCRIPTION + 1 SET_HIT_POLICY + 1 SET_VALUE_SET 
    // + 3 headers (2 input + 1 output) + 2 ADD_ROW + cell values
    assertTrue(commands.size() > 10, "Should have multiple commands, got: " + commands.size());
    
    // Verify basic property commands
    assertTrue(isCommand(commands, StatementType.SET_NAME, "Risk Assessment"));
    assertTrue(isCommand(commands, StatementType.SET_DESCRIPTION, "Customer risk evaluation"));
    assertTrue(isCommand(commands, StatementType.SET_HIT_POLICY, "FIRST"));
    
    // Verify value set command
    assertTrue(isCommandWithId(commands, StatementType.SET_VALUE_SET, "riskLevel", "low,medium,high"));
    
    // Verify header commands
    assertTrue(isCommandWithId(commands, StatementType.ADD_HEADER_IN, "age", "NUMBER"));
    assertTrue(isCommandWithId(commands, StatementType.ADD_HEADER_IN, "income", "NUMBER"));
    assertTrue(isCommandWithId(commands, StatementType.ADD_HEADER_OUT, "riskLevel", "STRING"));
    
    // Verify row commands
    long rowCommands = commands.stream()
        .filter(cmd -> cmd.getType() == StatementType.ADD_ROW)
        .count();
    assertEquals(2, rowCommands, "Should have 2 ADD_ROW commands");
    
    // Verify cell value commands
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "0,age", "< 25"));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "0,income", "< 30000"));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "0,riskLevel", "high"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "1,age", ">= 25"));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "1,income", ">= 50000"));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "1,riskLevel", "low"));
  }

  @Test
  public void testMinimalTableCommands() {
    final String yaml = """
        name: Simple Table
        hitPolicy: ALL
        table: |
          | input:STRING | -> | output:STRING |
          |--------------|----|---------------|
          | value1       |    | result1       |
        """;

    final DecisionParserCST parser = new DecisionParserCST(props);
    final Tuple2<MutableYamlParseTree, List<ModelError>> result = parser.parseCST(yaml);
    final YamlParseTree parseTree = result.getItem1();
    
    final List<DecisionStatement> commands = new DecisionCSTToCommands().convert(parseTree);
    
    // Should have: SET_NAME + SET_HIT_POLICY + 2 headers + 1 ADD_ROW + 2 cell values = 7 commands
    assertEquals(7, commands.size(), () -> "Known commands: " + join(null, commands));
    
    assertTrue(isCommand(commands, StatementType.SET_NAME, "Simple Table"));
    assertTrue(isCommand(commands, StatementType.SET_HIT_POLICY, "ALL"));
    assertTrue(isCommandWithId(commands, StatementType.ADD_HEADER_IN, "input", "STRING"));
    assertTrue(isCommandWithId(commands, StatementType.ADD_HEADER_OUT, "output", "STRING"));
    assertTrue(isCommand(commands, StatementType.ADD_ROW, null));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "0,input", "value1"));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "0,output", "result1"));
  }
  
  private String join(StatementType type,  List<DecisionStatement> commands) {
    return String.join(System.lineSeparator(), commands.stream()
        .filter(cmd -> type == null || cmd.getType() == type)
        .map(cmd -> JsonObject.mapFrom(cmd).encodePrettily())
        .toList());
  }

  private boolean isCommand(List<DecisionStatement> commands, StatementType type, String value) {
    return commands.stream()
        .anyMatch(cmd -> cmd.getType() == type && 
                        (value == null ? cmd.getValue() == null : value.equals(cmd.getValue())));
  }
  
  private boolean isCommandWithId(List<DecisionStatement> commands, StatementType type, String id, String value) {
    return commands.stream()
        .anyMatch(cmd -> cmd.getType() == type && 
                        (id == null ? cmd.getId() == null : id.equals(cmd.getId())) &&
                        (value == null ? cmd.getValue() == null : value.equals(cmd.getValue())));
  }
}