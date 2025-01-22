package io.digiexpress.eveli.client.spi.assets;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.HdesStore.ImportStoreEntity;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ImmutableImportStoreEntity;
import io.resys.hdes.client.api.ImmutableUpdateStoreEntityWithBodyType;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class HdesDefaultAssets {
  private final HdesClient client;
  private final boolean overwrite;
  
  public Uni<List<StoreEntity>> accept() {
    return client.store().query().get()
        .onItem().transform(this::visitCurrentState)
        .onItem().transform(this::visitNewAssets)
        .onItem().transformToUni(batch -> {
          if(batch.getCreate().isEmpty() && batch.getUpdate().isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
          }
          
          
          return client.store().batch(batch);
        });
  }
  
  private ImportStoreEntity visitNewAssets(ComposerState state) {
    final var builder = ImmutableImportStoreEntity.builder();
    visitFlow(state, builder, events_flow());
    visitDt(state, builder, events_dt());
    visitDt(state, builder, queues_dt());
    return builder.build();
  }
  
  private void visitDt(ComposerState state, ImmutableImportStoreEntity.Builder builder, CreateEntity dt) {

    final var prev = state.getDecisions().values().stream()
      .filter(e -> e.getAst().getName().equals(dt.getName()))
      .findFirst();
    
    if(prev.isPresent()) {
      
      if(!overwrite) {
        return;
      }
      builder.addUpdate(
          ImmutableUpdateStoreEntityWithBodyType.builder()
          .id(prev.get().getId())
          .body(dt.getBody())
          .bodyType(dt.getType())
          .build()
      );
    } else {
      builder.addCreate(ImmutableCreateStoreEntity.builder()
          .bodyType(dt.getType())
          .body(dt.getBody())
          .build());
    }
  }
  
  private void visitFlow(
      ComposerState state, 
      ImmutableImportStoreEntity.Builder builder,
      CreateEntity flow) {

    final var prev = state.getFlows().values().stream()
      .filter(e -> e.getAst().getName().equals(flow.getName()))
      .findFirst();
    

    if(prev.isPresent()) {
      if(!overwrite) {
        return;
      }
      builder.addUpdate(
          ImmutableUpdateStoreEntityWithBodyType.builder()
          .id(prev.get().getId())
          .body(flow.getBody())
          .bodyType(flow.getType())
          .build()
      );
    } else {
      builder.addCreate(ImmutableCreateStoreEntity.builder()
          .bodyType(flow.getType())
          .body(flow.getBody())
          .build());
    }
  }
  
  
  private ComposerState visitCurrentState(StoreState source) {
    // create envir
    final var envir = ComposerEntityMapper.toEnvir(client.envir().tagName(source.getTagName()), source).build();
    
    // map envir
    final var builder = ImmutableComposerState.builder();
    envir.getValues().values().forEach(v -> ComposerEntityMapper.toComposer(builder, v));
    return (ComposerState) builder.build(); 
  }
  
  
  public CreateEntity events_flow() {
    final var flow = """
id: task_mq_router
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
        ref: task_events_dt
        collection: true
        inputs:
          path: path
          op: operation
        
  - map events to queues:
      id: "task_event_queues"
      then: "end"
      decisionTable:
        ref: task_queues_dt
        collection: true
        inputs:
          event: task_events.event
""";
    return ImmutableCreateEntity.builder()
        .name("task_mq_router")
        .desc("Task event MQ router")
        .addBody(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(flow).build())
        .type(AstBodyType.FLOW)
        .build();
  }
  
  public CreateEntity events_dt() {
    final var name = "task_events_dt";
    
    final var commands = Arrays.asList(
      ImmutableAstCommand.builder().type(AstCommandValue.SET_NAME).value("task_events_dt").build(),
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
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("4").value(in("")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("5").value(in("add")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("6").value("TASK_CREATED").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//7
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("8").value(in("/updated")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("9").value(in("replace")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("10").value("TASK_UPDATED").build(),
      
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//11
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("12").value(qin("/completed/*")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("13").value(in("replace")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("14").value("TASK_COMPLETED").build(),
      
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//15
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("16").value(qin("/description/*")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("17").value(in("replace")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("18").value("TASK_DESC_UPDATED").build(),
      
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//19
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("20").value(qin("/dueDate/*")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("21").value(in("replace")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("22").value("TASK_DUEDATE_UPDATED").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//23
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("24").value(qin("/subject/*")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("25").value(in("replace")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("26").value("TASK_SUBJECT_UPDATED").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//27
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("28").value(qin(
          "/assignedId/*",
          "/assignedUser/*",
          "/assignedUserEmail/*"
      )).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("29").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("30").value("TASK_ASSIGNEE_UPDATED").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//31
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("32").value(qin("/keyWords/*")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("33").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("34").value("TASK_KEYWORDS_UPDATED").build(),
      
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//35
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("36").value(qin("/assignedRoles/*")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("37").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("38").value("TASK_ROLES_UPDATED").build(),
      
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//39
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("40").value(qin("/status")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("41").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("42").value("TASK_STATUS_UPDATED").build(),
      
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//43
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("44").value(qin("/priority")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("45").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("46").value("TASK_PRIORITY_UPDATED").build(),

      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//47              
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("48").value(qin("/comments/*/external/true")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("49").value(in("add")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("50").value("EXTERNAL_COMMENT_ADDED").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//51
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("52").value(qin("/comments/*/external/false")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("53").value(in("add")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("54").value("INTERNAL_COMMENT_ADDED").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//55
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("56").value(qin("/status/NEW")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("57").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("58").value("TASK_STATUS_UPDATED_TO_NEW").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//59
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("60").value(qin("/status/OPEN")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("61").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("62").value("TASK_STATUS_UPDATED_TO_OPEN").build(),
      
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//63
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("64").value(qin("/status/DELEGATED")).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("65").value(null).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("66").value("TASK_STATUS_UPDATED_TO_DELEGATED").build()
    );
    
    
    return ImmutableCreateEntity.builder()
        .name(name)
        .desc("Create task events from diff")
        .body(commands)
        .type(AstBodyType.DT)
        .build();
  }
  
  
  public CreateEntity queues_dt() {
    final var name = "task_queues_dt";
     final var commands = Arrays.asList(
        ImmutableAstCommand.builder().type(AstCommandValue.SET_NAME).value(name).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HIT_POLICY).value("ALL").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_IN).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("0").value("event").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("0").value("STRING").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_OUT).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("1").value("queue").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("1").value("STRING").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_OUT).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("2").value("enabled").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("2").value("BOOLEAN").build(),
        
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//3
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("4").value(in("TASK_CREATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("5").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("6").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//7
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("8").value(in("TASK_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("9").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("10").value("true").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//11
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("12").value(in("TASK_COMPLETED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("13").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("14").value("true").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//15
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("16").value(in("TASK_DESC_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("17").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("18").value("true").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//19
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("20").value(in("TASK_DUEDATE_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("21").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("22").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//23
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("24").value(in("TASK_SUBJECT_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("25").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("26").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//27
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("28").value(in("TASK_ASSIGNEE_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("29").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("30").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//31
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("32").value(in("TASK_KEYWORDS_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("33").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("34").value("true").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//35
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("36").value(in("TASK_ROLES_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("37").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("38").value("true").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//39
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("40").value(in("TASK_STATUS_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("41").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("42").value("true").build(),
        
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//43
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("44").value(in("TASK_PRIORITY_UPDATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("45").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("46").value("true").build(),

        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//47              
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("48").value(in("EXTERNAL_COMMENT_ADDED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("49").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("50").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//51
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("52").value(in("INTERNAL_COMMENT_ADDED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("53").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("54").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//55
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("56").value(in("TASK_STATUS_UPDATED_TO_NEW")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("57").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("58").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//59
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("60").value(in("TASK_STATUS_UPDATED_TO_OPEN")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("61").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("62").value("true").build(),
        
        ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build(),//63
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("64").value(in("TASK_STATUS_UPDATED_TO_DELEGATED")).build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("65").value("queue.task.log").build(),
        ImmutableAstCommand.builder().type(AstCommandValue.SET_CELL_VALUE).id("66").value("true").build()
        
        
    );
     
    return ImmutableCreateEntity.builder()
        .name(name)
        .desc("Map task events to QUEUE-s")
        .body(commands)
        .type(AstBodyType.DT)
        .build();
  }
  
  
  
  private String in(String exp) {
    return "in[\"" + exp + "\"]";
  }
  private String qin(String ...exp) {
    final var next = Arrays.asList(exp).stream().map(e -> "\"" + e + "\"").toList().toArray(new String[] {});
    return "qin[" + String.join(",", next) + "]";
  }
}
