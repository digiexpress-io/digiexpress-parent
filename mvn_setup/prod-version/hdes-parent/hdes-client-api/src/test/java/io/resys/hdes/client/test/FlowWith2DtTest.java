package io.resys.hdes.client.test;


import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.test.config.TestUtils;


public class FlowWith2DtTest {

  @Test
  public void runAll() throws IOException {
    
    final var envir = TestUtils.client.envir().tagName("FlowWith2DtTest")
        .addCommand().id("events_dt").decision(events_dt()).build()
        .addCommand().id("queues_dt").decision(queues_dt()).build()
        .addCommand().id("flow with 2 DT").flow(flow(
"""
id: flow with 2 DT
inputs:
  path:
    required: true
    type: STRING
  operation:
    required: true
    type: STRING

tasks:
  - match all events:
      id: "task_events"
      then: "task_event_queues"
      decisionTable:
        ref: events_dt
        collection: true
        inputs:
          path: path
          op: operation
        
  - map events to queues:
      id: "task_event_queues"
      then: "end"
      decisionTable:
        ref: queues_dt
        collection: true
        inputs:
          event: task_events.event
"""))
        .build()
        .build();
    
    
    final var result = TestUtils.client.executor(envir)
      .inputMap(Map.of(
        "path", "",
        "operation", ""
      ))
      .flow("flow with 2 DT").andGetBody();

    
    System.out.println(result);
  }


  public String flow(String value) throws JsonProcessingException {
    return TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
    .type(AstCommandValue.SET_BODY)
    .value(value)
    .build()));
  }
  
  public String events_dt() throws JsonProcessingException {
    
    return TestUtils.objectMapper.writeValueAsString(Arrays.asList(
        ImmutableAstCommand.builder().type(AstCommandValue.SET_NAME).value("events_dt").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HIT_POLICY).value("ALL").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_IN).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("0").value("path").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("0").value("STRING").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_IN).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("1").value("op").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("1").value("STRING").build(),

        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_OUT).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("2").value("event").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("2").value("STRING").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//3
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("4").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("5").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("6").value("TASK_CREATED").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//7
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("8").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("9").value(null).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("10").value("TASK_UPDATED").build()
    ));
  }
  
  
  public String queues_dt() throws JsonProcessingException {
    return TestUtils.objectMapper.writeValueAsString(Arrays.asList(
        ImmutableAstCommand.builder().type(AstCommandValue.SET_NAME).value("queues_dt").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HIT_POLICY).value("ALL").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_IN).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("0").value("event").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("0").value("STRING").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_OUT).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("1").value("queue").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("1").value("STRING").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//2
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("3").value(in("TASK_CREATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("4").value("sms").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//5
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("6").value(in("TASK_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("7").value("sms").build()
    ));
  }
  
  private String in(String exp) {
    return "in[\"" + exp + "\"]";
  }
}
