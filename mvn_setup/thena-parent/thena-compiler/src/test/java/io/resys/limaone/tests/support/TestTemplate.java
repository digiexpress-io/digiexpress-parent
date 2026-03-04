package io.resys.limaone.tests.support;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.io.IOUtils;

import com.google.common.hash.Hashing;

import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.program.DefaultRuntime;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Value;

public class TestTemplate {
  private static final CompilerImpl compiler = CompilerImpl.builder().build();

  
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
    final var nodeString = toString(fullPath);
    final var model = ImmutableModel.<DecisionTable>builder()
        .id(fullPath)
        .bodyHash(Hashing.murmur3_128().hashString(nodeString, StandardCharsets.UTF_8).toString())
        .bodyType(BodyType.DECISION_TABLE)
        .body(ImmutableDecisionTable.builder()
            .name(fullPath)
            .nodes(new JsonArray(nodeString).stream()
                  .map(e -> ((JsonObject) e))
                  .map(e -> e.mapTo(DecisionStatement.class))
                  .toList())
            .build())
        .build();
    
    final var world = ImmutableModelWorld.builder().name("DecisionTest")
        .putDecisionTables(model.getId(), model)
        .build();
    return compiler.compile(world).id(fullPath)
        .build().queryDecisions().name(fullPath).getOne();
  }
  
  
  
  public static DecisionProgram compileOneDt(String fullPath) {
    final var nodeString = toString(fullPath);
    final var model = ImmutableModel.<DecisionTable>builder()
        .id(fullPath)
        .bodyHash(Hashing.murmur3_128().hashString(nodeString, StandardCharsets.UTF_8).toString())
        .bodyType(BodyType.DECISION_TABLE)
        .body(ImmutableDecisionTable.builder()
            .name(fullPath)
            .nodes(new JsonArray(nodeString).stream()
                  .map(e -> ((JsonObject) e))
                  .map(e -> e.mapTo(DecisionStatement.class))
                  .toList())
            .build())
        .build();
    
    final var world = ImmutableModelWorld.builder().name("DecisionTest")
        .putDecisionTables(model.getId(), model)
        .build();
    return compiler.compile(world).id(fullPath)
        .build().queryDecisions().name(fullPath).getOne();
  }
  
  

  public static FlowTaskProgram compileOneFlowTask(String fullPath) {
  
    final var taskValue = toString(fullPath);
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
    return compiler.compile(world).id(fullPath)
        .build().queryFlowTasks().name(fullPath).getOne();

  }

  
  
  public static FlowProgram compileOneFlow(String flowSyntax, Deps ... deps) {
    
    final var flowValue = flowSyntax;
    final var ast = compiler.getParser().parseFlow().id("test-flow").syntax(flowValue).parse();
    final var model = ImmutableModel.<Flow>builder()
        .id("test-value")
        .bodyHash(ast.getHash())
        .bodyType(BodyType.FLOW)
        .body(ImmutableFlow.builder()
            .flowName(ast.getName())
            .flowValue(flowValue)
            .build())
        .build();
    
    final var world = ImmutableModelWorld.builder().name("FlowTest").putFlows(model.getId(), model);
    
    
    for(final var dep : deps) {
      final var syntax = toString(dep.getFullPath());
      if(dep.getType() == BodyType.FLOW_TASK) {
        final var target_ast = compiler.getParser().parseFlowTask().id(dep.getFullPath()).syntax(syntax).parse();
        final var ft = ImmutableModel.<FlowTask>builder()
            .id(dep.getFullPath())
            .bodyHash(target_ast.getHash())
            .bodyType(BodyType.FLOW_TASK)
            .body(ImmutableFlowTask.builder()
                .taskName(target_ast.getName())
                .taskValue(syntax)
                .build())
            .build();
        
        world.putFlowTasks(ft.getId(), ft);
      } else {
        final var nodes = new JsonArray(syntax).stream()
            .map(e -> ((JsonObject) e))
            .map(e -> e.mapTo(DecisionStatement.class))
            .toList();

        final var target_ast = compiler.getParser().parseDecisionTable().id(dep.getFullPath()).nodes(nodes).parse();
        final var dt = ImmutableModel.<DecisionTable>builder()
            .id(dep.getFullPath())
            .bodyHash(target_ast.getHash())
            .bodyType(BodyType.DECISION_TABLE)
            .body(ImmutableDecisionTable.builder()
                .name(target_ast.getName())
                .nodes(nodes)
                .build())
            .build();
        
        world.putDecisionTables(dt.getId(), dt);
      }
    }
  
    final var cacheKey = LocalDateTime.now().toString();
    return compiler.compile(world.build()).id("test-bundle")
        .cacheKey(cacheKey)
        .build().queryFlows().name(ast.getName()).getOne()
        .withRuntime(DefaultRuntime.withCache(cacheKey));
  }
  
  
  private static String toString(String fullPath) {
    try {
      return IOUtils.toString(TestTemplate.class.getClassLoader().getResource(fullPath), StandardCharsets.UTF_8);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  @Value
  public static class Deps {
    BodyType type;
    String fullPath;
    
    public static Deps dt(String fullPath) {
      return new Deps(BodyType.DECISION_TABLE, fullPath);
    }
    
    public static Deps ft(String fullPath) {
      return new Deps(BodyType.FLOW_TASK, fullPath);
    }
  }
}
