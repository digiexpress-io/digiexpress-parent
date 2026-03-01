package io.resys.limaone.tests.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.io.IOUtils;

import com.google.common.hash.Hashing;

import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.DecisionTable.DecisionTableNode;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.Compiler;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.tests.DecisionTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class TestTemplate {
  private static final Compiler compiler = CompilerImpl.builder().build();

  
  public static LocalDateTime parseLocalDateTime(String date) {
    try {
      return LocalDateTime.ofInstant(ZonedDateTime.parse(date).toInstant(), ZoneId.systemDefault());
    } catch(Exception e) {
      throw new IllegalArgumentException("Incorrect date time: '" + date + "', correct format: YYYY-MM-DDThh:mm:ssTZD, example: 2017-07-03T00:00:00Z!");
    }
  }

  public static LocalDate parseLocalDate(String date) {
    try {
      if(date.length() > 10) {
        return LocalDate.parse(date.substring(0, 10));
      }
      return LocalDate.parse(date);
    } catch(Exception e) {
      throw new IllegalArgumentException("Incorrect date: '" + date + "', correct format: YYYY-MM-DD, example: 2017-07-03!");
    }
  }
  

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
      
      final var world = ImmutableModelWorld.builder().name("DecisionTest")
          .putDecisionTables(model.getId(), model)
          .build();
      return compiler.compile(world).id(fullPath).build().queryDecisions().name(fullPath).getOne();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  
  
  public static DecisionProgram compileOneDt(String fullPath) {
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
      
      final var world = ImmutableModelWorld.builder().name("DecisionTest")
          .putDecisionTables(model.getId(), model)
          .build();
      return compiler.compile(world).id(fullPath).build().queryDecisions().name(fullPath).getOne();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  

  public static FlowTaskProgram compileOneFlowTask(String fullPath) {
    try {
      final var taskValue = IOUtils.toString(DecisionTest.class.getClassLoader().getResource(fullPath), StandardCharsets.UTF_8);
      final var model = ImmutableModel.<FlowTask>builder()
          .id(fullPath)
          .bodyHash(Hashing.murmur3_128().hashString(taskValue, StandardCharsets.UTF_8).toString())
          .bodyType(BodyType.FLOW_TASK)
          .body(ImmutableFlowTask.builder()
              .taskName(fullPath)
              .taskValue(taskValue)
              .build())
          .build();
      
      final var world = ImmutableModelWorld.builder().name("FlowTaskTest")
          .putFlowTasks(model.getId(), model)
          .build();
      return compiler.compile(world).id(fullPath).build().queryFlowTasks().name(fullPath).getOne();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
