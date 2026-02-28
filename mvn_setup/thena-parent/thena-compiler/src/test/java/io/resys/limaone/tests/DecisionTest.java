package io.resys.limaone.tests;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.hash.Hashing;

import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.DecisionTableNode;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.Compiler;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.DecisionProgram.DecisionRow;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.tests.support.DateParser;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;



public class DecisionTest {

  private final Compiler compiler = CompilerImpl.builder().build();

  public DecisionProgram compileDt(String fullPath) {
    try {
      final var nodeString = IOUtils.toString(DecisionTest.class.getClassLoader().getResource(fullPath), StandardCharsets.UTF_8);
      final var model = ImmutableModel.<DecisionTable>builder()
          .id(fullPath)
          .bodyHash(Hashing.murmur3_128().hashString(nodeString, StandardCharsets.UTF_8).toString())
          .bodyType(BodyType.DECISION_TABLE)
          .body(ImmutableDecisionTable.builder()
              .name(fullPath)
              .nodes(new JsonArray(nodeString).stream()
                    .map(e -> ((JsonObject) e))
                    .map(e -> e.mapTo(DecisionTableNode.class))
                    .toList())
              .build())
          .build();
      
      final var world = ImmutableModelWorld.builder().name("DecisionTest").putDecisionTables(model.getId(), model).build();
      return compiler.compile(world).build().queryDecisions().name(fullPath).getOne();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  
  @Test
  public void readerNodeOrderTest() throws IOException {
    final var decisionTable = compileDt("decision/dt.json");
    
    List<DecisionRow> rows = decisionTable.getRows();
    Assertions.assertEquals(0, rows.get(0).getOrder());
    Assertions.assertEquals(1, rows.get(1).getOrder());
    Assertions.assertEquals(2, rows.get(2).getOrder());
    Assertions.assertEquals(3, rows.get(3).getOrder());
    Assertions.assertEquals(4, rows.get(4).getOrder());
    Assertions.assertEquals(5, rows.size());
  }

  
//Match 1
//{
//   "id": "sriBoolean",
//   "value": "false"
//},
//{
//   "id": "risk",
//   "value": "in [\"CAREFUL\", \"NOT\"]"
//},
//{
//   "id": "sri",
//   "value": "[1..2]"
//},
//{
//   "id": "sriDate",
//   "value": "equals 2017-07-03T00:00:00"
//},
//
//Match 3
//{
//   "id": "sriBoolean",
//   "value": "false"
//},
//{
//   "id": "risk",
//   "value": "not in [\"MODERATE\"]"
//},
//{
//   "id": "sri",
//   "value": "[1..5]"
//},
//{
//   "id": "sriDate",
//   "value": "equals 2017-07-03T00:00:00"
//}
  @Test
  public void executionTest() throws IOException {
    final var envir = compileDt("decision/dt.json");

    Map<String, Serializable> values = new HashMap<>();
    values.put("sriBoolean", false);
    values.put("risk", "CAREFUL");
    values.put("sri", 1);
    values.put("sriDate", DateParser.parseLocalDate("2017-07-03"));
    
    DecisionResult result = envir.run(values).andGetBody();

    Assertions.assertEquals(2, result.getMatches().size());
    Assertions.assertEquals(0, result.getMatches().get(0).getOrder());
    Assertions.assertEquals(2, result.getMatches().get(1).getOrder());
  }

  
  @Test
  public void qinMatchingTest() throws IOException {
    final var envir = compileDt("decision/dt3.json");

    {
      Map<String, Serializable> values = new HashMap<>();
      values.put("sriBoolean", false);
      values.put("path", "xyz");
      values.put("sri", 1);
      values.put("sriDate", DateParser.parseLocalDate("2017-07-03"));
      DecisionResult result = envir.run(values).andGetBody();
      Assertions.assertEquals(0, result.getMatches().size());
    }
    
    {
      Map<String, Serializable> values = new HashMap<>();
      values.put("sriBoolean", false);
      values.put("path", "task/smt/name");
      values.put("sri", 1);
      values.put("sriDate", DateParser.parseLocalDate("2017-07-03"));
      DecisionResult result = envir.run(values).andGetBody();
      Assertions.assertEquals(1, result.getMatches().size());
    }

  }
  
  @Test
  public void nullEqualsNull() throws IOException {
    final var envir = compileDt("decision/nullEqualsNull.json");
    
    Map<String, Serializable> values = new HashMap<>();
    values.put("risk", null);
    
    DecisionResult result = envir.run(values).andGetBody();

    Assertions.assertEquals(1, result.getMatches().size());
  }
  
  @Test
  public void firstHitPolicy() throws IOException {
    final var envir = compileDt("decision/firstHitPolicy.json");

    Map<String, Serializable> values = new HashMap<>();
    values.put("regionName", "FIN");
    DecisionResult result = envir.run(values).andGetBody();
    Assertions.assertEquals(1, result.getMatches().size());
    Assertions.assertEquals(0, result.getMatches().get(0).getOrder());


    values = new HashMap<>();
    values.put("regionName", "X");
    result = envir.run(values).andGetBody();
    Assertions.assertEquals(1, result.getMatches().size());
    Assertions.assertEquals(1, result.getMatches().get(0).getOrder());
  }

  @Test
  public void all() throws IOException {
    final var envir = compileDt("decision/allHitPolicy.json");

    Map<String, Serializable> values = new HashMap<>();
    values.put("firstName", "Mark");
    DecisionResult result = envir.run(values).andGetBody();
    Assertions.assertEquals(2, result.getMatches().size());
  }
  
  @Test
  public void csvImportCommand() throws IOException {
    final var ast = compileDt("decision/dt-import.json");
    Assertions.assertEquals(ProgramStatus.UP, ast.getStatus());
  }

  @Test
  public void valueSetTest() throws IOException {
    final var ast = compileDt("decision/dtWithValueSet.json");
    List<String> valueSet = ast.getHeaders().get(0).getValueSet();
    Assertions.assertEquals(3, valueSet.size());
  }

}
