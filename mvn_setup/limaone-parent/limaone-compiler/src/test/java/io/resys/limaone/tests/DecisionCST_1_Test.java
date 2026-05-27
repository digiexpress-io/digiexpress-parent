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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.resys.limaone.ast.DecisionTable_CST.YamlDecision;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.DecisionTable.StatementType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.CST_YamlParser;
import io.resys.limaone.spi.ast.decisiontable.DecisionCSTToCommands;
import io.resys.limaone.spi.ast.decisiontable.MutableYamlDecision;
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
          | age:INTEGER    | income: INTEGER | -> | riskLevel:STRING   |
          |----------------|-----------------|----|--------------------|
          | < 25           | < 30000         |    | high               |
          | >= 25          | >= 50000        |    | low                |
        """;

    final var parser = new CST_YamlParser<MutableYamlDecision>(props, new MutableYamlDecision());
    final Tuple2<MutableYamlDecision, List<ModelError>> result = parser.parseCST(yaml);
    final YamlDecision parseTree = result.getItem1();
    
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
    assertTrue(isCommandWithId(commands, StatementType.SET_VALUE_SET, "2", "low,medium,high"));
    
    // Verify header commands
    assertTrue(isCommandWithValue(commands, StatementType.SET_HEADER_REF, "age"), () -> "Known commands: " + join(null, commands));
    assertTrue(isCommandWithValue(commands, StatementType.SET_HEADER_REF, "income"));
    assertTrue(isCommandWithValue(commands, StatementType.SET_HEADER_REF, "riskLevel"));
    
    // Verify row commands
    long rowCommands = commands.stream()
        .filter(cmd -> cmd.getType() == StatementType.ADD_ROW)
        .count();
    assertEquals(2, rowCommands, "Should have 2 ADD_ROW commands");
    
    // Verify cell value commands
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "4,age", "< 25"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "5,income", "< 30000"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "6,riskLevel", "high"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "8,age", ">= 25"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "9,income", ">= 50000"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "10,riskLevel", "low"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
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

    final var parser = new CST_YamlParser<MutableYamlDecision>(props, new MutableYamlDecision());
    final Tuple2<MutableYamlDecision, List<ModelError>> result = parser.parseCST(yaml);
    final YamlDecision parseTree = result.getItem1();
    
    final List<DecisionStatement> commands = new DecisionCSTToCommands().convert(parseTree);
    
    // Should have: SET_NAME + SET_HIT_POLICY + 2 headers + 1 ADD_ROW + 2 cell values = 7 commands
    assertEquals(11, commands.size(), () -> "Known commands: " + join(null, commands));
    
    assertTrue(isCommand(commands, StatementType.SET_NAME, "Simple Table"));
    assertTrue(isCommand(commands, StatementType.SET_HIT_POLICY, "ALL"));
    assertTrue(isCommandWithValue(commands, StatementType.SET_HEADER_REF, "input"));
    assertTrue(isCommandWithValue(commands, StatementType.SET_HEADER_REF, "output"));
    assertTrue(isCommand(commands, StatementType.ADD_ROW, null));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "3,input", "value1"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
    assertTrue(isCommandWithId(commands, StatementType.SET_CELL_VALUE, "4,output", "result1"), () -> "Known commands: " + join(StatementType.SET_CELL_VALUE, commands));
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
  
  private boolean isCommandWithValue(List<DecisionStatement> commands, StatementType type, String value) {
    return commands.stream()
        .anyMatch(cmd -> cmd.getType() == type && 
                        (value == null ? cmd.getValue() == null : value.equals(cmd.getValue())));
  }
  
  private boolean isCommandWithId(List<DecisionStatement> commands, StatementType type, String idAndName, String value) {
    final var id = idAndName.split(",")[0];
    return commands.stream()
        .anyMatch(cmd -> cmd.getType() == type && 
                        (id == null ? cmd.getId() == null : id.equals(cmd.getId())) &&
                        (value == null ? cmd.getValue() == null : value.equals(cmd.getValue())));
  }
}
