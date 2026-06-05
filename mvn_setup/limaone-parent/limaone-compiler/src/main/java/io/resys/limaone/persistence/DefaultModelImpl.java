package io.resys.limaone.persistence;

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

import java.time.OffsetDateTime;
import java.util.Arrays;

import io.resys.limaone.authoring.Authoring.DefaultModel;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.StatementType;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.ImmutableDecisionStatement;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class DefaultModelImpl implements DefaultModel {
  
  private final AuthoringConfig config;
  private final boolean overwrite;
  
  @Override
  public Uni<Void> buildDefaultModel() {
        
    return config.getPersistence()
      .worldBuilder()
      .author(config.getEnvir().getCurrentUser().get().getUserName())
      .createdAt(OffsetDateTime.now())
      .docs(BodyType.without(BodyType.DEPLOYMENT))
      .build(nextWorld -> {

        visitBody(nextWorld, events_flow());
        visitBody(nextWorld, events_dt());
        visitBody(nextWorld, queues_dt());
        
        visitBody(nextWorld, event_message_worker_intl());
        visitBody(nextWorld, event_message_suomifi_intl());
        
        return "";
      })
      .onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
  }
  
  private void visitBody(NextWorld next, DecisionTable body) {
    final var prev = next.getCurrentWorld().getDecisionTables().values().stream()
      .filter(t -> t.getBody().getName().equals(body.getName()))
      .findFirst();
    
    if(prev.isEmpty()) {
      next.newModel(body.getName(), body, null, null);
      return;
    }
    
    if(!overwrite) {
      return;
    }
    
    next.mergeModel(
        prev.get().getId(), 
        prev.get().getBody().getName(), body, null, null);
  }
  
  private void visitBody(NextWorld next, Flow flow) {
    final var prev = next.getCurrentWorld().getFlows().values().stream()
        .filter(t -> t.getBody().getFlowName().equals(flow.getFlowName()))
        .findFirst();

    if(prev.isEmpty()) {
      next.newModel(flow.getFlowName(), flow, null, null);
      return;
    }
    
    if(!overwrite) {
      return;
    }
    
    next.mergeModel(
        prev.get().getId(), 
        prev.get().getBody().getFlowName(), flow, null, null);
  }

  public Flow events_flow() {
    final var flow = """
id: task_mq_router
inputs:
  path:
    required: true
    type: STRING
  operation:
    required: true
    type: STRING  
  clientLanguage:
    required: true
    type: STRING
  clientId:
    required: true
    type: STRING
  taskGroupId:
    required: true
    type: STRING
  taskRef:
    required: true
    type: STRING
tasks:
  - generate events:
      id: task_events
      then: task_event_queues
      decisionTable:
        ref: event_types
        collection: true
        inputs:
          path: path
          op: operation

  - map events to queues:
      id: task_event_queues
      then: notification_content
      decisionTable:
        ref: event_queues
        collection: true
        inputs:
          event: task_events.event
 
  - select notification content:
      id: notification_content
      switch:
        - internal worker notification:
            when: "task_event_queues.queue == 'queue.task.worker_email'"
            then: worker_message_contents
        - customer notification using suomifi:
            when: "task_event_queues.queue == 'queue.task.suomifi'"
            then: suomifi_message_contents
        - other cases:
            then: end

  - suomifi message contents:
      id: suomifi_message_contents
      then: format_suomifi_message
      decisionTable:
        ref: event_message_suomifi_intl
        collection: true
        inputs:
          change_type: task_event_queues._event
          queue: task_event_queues.queue

  - format suomifi message:
      id: format_suomifi_message
      then: end
      returns:
        collection: true
        inputs:
          changeType: suomifi_message_contents._change_type
          queue: suomifi_message_contents._queue
          customerId: clientId
          taskRef: taskRef
          message: suomifi_message_contents.message
          title: suomifi_message_contents.title
          email: suomifi_message_contents.email
          messageType: "SUOMIFI_MSG"

  - worker message contents:
      id: worker_message_contents
      then: format_worker_message
      decisionTable:
        ref: event_message_worker_intl
        collection: true
        inputs:
          change_type: task_event_queues._event
          queue: task_event_queues.queue

  - format worker message:
      id: format_worker_message
      then: end
      returns:
        collection: true
        inputs:
          changeType: worker_message_contents._change_type
          queue: worker_message_contents._queue
          message: worker_message_contents.message
          title: worker_message_contents.title
          customerId: clientId
          taskRef: taskRef
          messageType: "WORKER_MSG"
""";
    return ImmutableFlow.builder()
        .flowName("task_mq_router")
        .flowValue(flow)
        .build();
  }
  
  public ImmutableDecisionTable events_dt() {
    final var name = "event_types";
    
    final var commands = Arrays.asList(
      ImmutableDecisionStatement.builder().type(StatementType.SET_NAME).value("event_types").build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_HIT_POLICY).value("ALL").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_IN).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).id("0").value("path").build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).id("0").value("STRING").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_IN).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).id("1").value("op").build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).id("1").value("STRING").build(),

      ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).id("2").value("event").build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).id("2").value("STRING").build(),
      
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//3
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("4").value(in("")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("5").value(in("add")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("6").value("TASK_CREATED").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//7
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("8").value(in("/updated")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("9").value(in("replace")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("10").value("TASK_UPDATED").build(),
      
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//11
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("12").value(qin("/completed/*")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("13").value(in("replace")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("14").value("TASK_COMPLETED").build(),
      
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//15
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("16").value(qin("/description/*")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("17").value(in("replace")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("18").value("TASK_DESC_UPDATED").build(),
      
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//19
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("20").value(qin("/dueDate/*")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("21").value(in("replace")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("22").value("TASK_DUEDATE_UPDATED").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//23
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("24").value(qin("/subject/*")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("25").value(in("replace")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("26").value("TASK_SUBJECT_UPDATED").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//27
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("28").value(qin(
          "/assignedId/*",
          "/assignedUser/*",
          "/assignedUserEmail/*"
      )).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("29").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("30").value("TASK_ASSIGNEE_UPDATED").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//31
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("32").value(qin("/keyWords/*")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("33").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("34").value("TASK_KEYWORDS_UPDATED").build(),
      
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//35
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("36").value(qin("/assignedRoles/*")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("37").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("38").value("TASK_ROLES_UPDATED").build(),
      
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//39
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("40").value(qin("/status")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("41").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("42").value("TASK_STATUS_UPDATED").build(),
      
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//43
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("44").value(qin("/priority")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("45").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("46").value("TASK_PRIORITY_UPDATED").build(),

      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//47              
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("48").value(qin("/comments/*/external/true")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("49").value(in("add")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("50").value("EXTERNAL_COMMENT_ADDED").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//51
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("52").value(qin("/comments/*/external/false")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("53").value(in("add")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("54").value("INTERNAL_COMMENT_ADDED").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//55
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("56").value(qin("/status/NEW")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("57").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("58").value("TASK_STATUS_UPDATED_TO_NEW").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//59
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("60").value(qin("/status/OPEN")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("61").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("62").value("TASK_STATUS_UPDATED_TO_OPEN").build(),
      
      ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//63
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("64").value(qin("/status/DELEGATED")).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("65").value(null).build(),
      ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("66").value("TASK_STATUS_UPDATED_TO_DELEGATED").build()
    );
    
    
    return ImmutableDecisionTable.builder()
        .name(name)
        .nodes(commands)
        .build();
  }
  
  
  public DecisionTable queues_dt() {
    final var name = "event_queues";
     final var commands = Arrays.asList(
        ImmutableDecisionStatement.builder().type(StatementType.SET_NAME).value(name).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_HIT_POLICY).value("ALL").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_IN).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).id("0").value("event").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).id("0").value("STRING").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).id("1").value("queue").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).id("1").value("STRING").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).id("2").value("enabled").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).id("2").value("BOOLEAN").build(),
        
        
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//3
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("4").value(in("TASK_CREATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("5").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("6").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//7
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("8").value(in("TASK_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("9").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("10").value("true").build(),
        
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//11
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("12").value(in("TASK_COMPLETED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("13").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("14").value("true").build(),
        
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//15
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("16").value(in("TASK_DESC_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("17").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("18").value("true").build(),
        
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//19
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("20").value(in("TASK_DUEDATE_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("21").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("22").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//23
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("24").value(in("TASK_SUBJECT_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("25").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("26").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//27
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("28").value(in("TASK_ASSIGNEE_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("29").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("30").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//31
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("32").value(in("TASK_KEYWORDS_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("33").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("34").value("true").build(),
        
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//35
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("36").value(in("TASK_ROLES_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("37").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("38").value("true").build(),
        
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//39
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("40").value(in("TASK_STATUS_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("41").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("42").value("true").build(),
        
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//43
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("44").value(in("TASK_PRIORITY_UPDATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("45").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("46").value("true").build(),

        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//47              
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("48").value(in("EXTERNAL_COMMENT_ADDED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("49").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("50").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//51
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("52").value(in("INTERNAL_COMMENT_ADDED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("53").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("54").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//55
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("56").value(in("TASK_STATUS_UPDATED_TO_NEW")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("57").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("58").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//59
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("60").value(in("TASK_STATUS_UPDATED_TO_OPEN")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("61").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("62").value("true").build(),
        
        ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build(),//63
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("64").value(in("TASK_STATUS_UPDATED_TO_DELEGATED")).build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("65").value("queue.task.log").build(),
        ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).id("66").value("true").build()
        
        
    );
     
    return ImmutableDecisionTable.builder()
        .name(name)
        .nodes(commands)
        .build();
  }
  
  
  public DecisionTable event_message_suomifi_intl() {
    final var name = "event_message_suomifi_intl";
    final var commands = Arrays.asList(
       ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_IN).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).value("change_type").id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_SCRIPT).value(null).id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).value("STRING").id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_VALUE_SET).value("").id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).value("message").id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_SCRIPT).value(null).id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).value("INTL").id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_VALUE_SET).value("fi, en, sv").id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).value("title").id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_SCRIPT).value(null).id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).value("INTL").id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_VALUE_SET).value("fi, sv, en").id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).value("email").id("3").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_SCRIPT).value(null).id("3").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).value("INTL").id("3").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_VALUE_SET).value("fi, sv, en").id("3").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("in[\"TASK_COMPLETED\",\"TASK_CREATED\"]").id("5").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("{\"fi\":\"\",\"en\":\"\",\"sv\":\"\"}").id("6").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("{\"en\":\"\",\"fi\":\"\"}").id("7").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("{\"fi\":\"\",\"sv\":\"\",\"en\":\"\"}").id("8").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("in[\"EXTERNAL_COMMENT_ADDED\"]").id("10").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("{\"en\":\"\",\"fi\":\"\"}").id("11").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("{\"en\":\"\"}").id("12").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("{\"en\":\"\",\"sv\":\"\"}").id("13").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HIT_POLICY).value("ALL").id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_NAME).value(name).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_DESCRIPTION).value(null).id(null).build()
    );
    return ImmutableDecisionTable.builder()
        .name(name)
        .nodes(commands)
        .build();
  }
  
  
  
  public DecisionTable event_message_worker_intl() {
    final var name = "event_message_worker_intl";
    final var commands = Arrays.asList(
       ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_IN).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).value("change_type").id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_SCRIPT).value(null).id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).value("STRING").id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_VALUE_SET).value("").id("0").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).value("message").id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_SCRIPT).value(null).id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).value("INTL").id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_VALUE_SET).value("fi, sv, en").id("1").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_HEADER_OUT).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_REF).value("title").id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_SCRIPT).value(null).id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HEADER_TYPE).value("INTL").id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_VALUE_SET).value("fi, sv, en").id("2").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("in[\"TASK_CREATED\"]").id("4").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("{\"fi\":\"task created\"}").id("5").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value(null).id("6").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("in [\"TASK_ASSIGNEE_UPDATED\"]").id("8").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value(null).id("9").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value(null).id("10").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("in [\"TASK_ROLES_UPDATED\"]").id("12").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value(null).id("13").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value(null).id("14").build(),
       ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).value(null).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value("in [\"EXTERNAL_COMMENT_ADDED\"]").id("16").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value(null).id("17").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_CELL_VALUE).value(null).id("18").build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_HIT_POLICY).value("ALL").id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_NAME).value(name).id(null).build(),
       ImmutableDecisionStatement.builder().type(StatementType.SET_DESCRIPTION).value(null).id(null).build()
    );
    return ImmutableDecisionTable.builder()
        .name(name)
        .nodes(commands)
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
