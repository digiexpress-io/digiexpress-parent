package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerId;
import io.digiexpress.eveli.client.api.GamutClient.ProcessAuthorization;
import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.api.GamutClient.UserActionQuery;
import io.digiexpress.eveli.client.api.GamutClient.UserMessage;
import io.digiexpress.eveli.client.api.GamutClient.UserSubAction;
import io.digiexpress.eveli.client.api.ImmutableUserAction;
import io.digiexpress.eveli.client.api.ImmutableUserActionAttachment;
import io.digiexpress.eveli.client.api.ImmutableUserSubAction;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.ProcessStatus;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskAssignmentStatus;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RequiredArgsConstructor
public class UserActionsQueryImpl implements UserActionQuery {
  
  private final EveliEnvirClient eveliEnvir;
  private final TaskClient taskClient;
  private final GamutAuthClient authClient;
  private final AttachmentCommands attachmentsCommands;

  private String cockpitId;
  
  @Override
  public UserActionQuery cockpitId(String cockpitId) {
    this.cockpitId = cockpitId;
    return this;
  }
  @Override
  public Uni<Optional<UserAction>> findOneById(String id) {
    final var customer = authClient.getCustomer().getCustomerId();
    
    return Uni.combine().all().unis(
      taskClient.queryTaskProcesess().findOneById(id), 
      visitAuthorization()
    )
    .asTuple()
    .onItem().transformToUni(tuple -> {
      final var processes = tuple.getItem1().map(e -> Arrays.asList(e)).orElse(Collections.emptyList());
      final var auth = tuple.getItem2();
      return visitTasks(processes, customer).onItem().transform(tasks -> Tuple3.of(processes, auth, tasks));
    })
    .onItem().transform(tuple -> {
      final var processes = tuple.getItem1();
      final var auth = tuple.getItem2();
      final var tasks = tuple.getItem3();
      
      return processes.stream().filter(proc -> customer.getHolderId().equals(proc.getUserId()))
        .filter(process -> isAuthorizedProcess(process, auth))
        .map(process -> visitUserAction(process, tasks))
        .findFirst();
    });
  }
  
  @Override
  public Multi<UserAction> findAll() {
    final var customer = authClient.getCustomer().getCustomerId();
    
    return Uni.combine().all().unis(
        taskClient.queryTaskProcesess().findAllNotArchivedyUserId(customer.getHolderId()).collect().asList(), 
        visitAuthorization()
    )
    .asTuple()
    .onItem().transformToUni(tuple -> {
      final var processes = tuple.getItem1();
      final var auth = tuple.getItem2();
      
      return visitTasks(processes, customer).onItem().transform(tasks -> Tuple3.of(processes, auth, tasks));
    })
    .onItem().transformToMulti(tuple -> {
      final var processes = tuple.getItem1();
      final var auth = tuple.getItem2();
      final var tasks = tuple.getItem3();
      
      final var actions = processes.stream()
          .filter(process -> isAuthorizedProcess(process, auth))
          .map(process -> visitUserAction(process, tasks))
          .toList();
      
      return Multi.createFrom().items(actions.stream());
    });
  }
  
  
  @Override
  public Uni<Optional<UserAction>> findOneAnonById(String id) {
    return taskClient.queryTaskProcesess().findOneById(id)
      .onItem().transform(proc -> {
        
        final var processes = proc.stream()
            .filter(process -> Boolean.TRUE.equals(process.getAnon()))
            .toList();
        final var tasks = new TasksContext(Collections.emptyMap(), Collections.emptyMap());
        
        return processes.stream()
            .map(process -> visitUserAction(process, tasks))
            .findFirst();
      });
  }

  private AttachmentsContext visitAttachments(ProcessInstance process) {
    final List<AttachmentCommands.Attachment> processAttachments = attachmentsCommands.query().processId(process.getId().toString());
    final List<AttachmentCommands.Attachment> taskAttachments = process.getTaskId() == null ? 
      Collections.emptyList() : 
      attachmentsCommands.query().taskId(process.getTaskId().toString());
    return new AttachmentsContext(processAttachments, taskAttachments);
  }
  
  private ImmutableUserActionAttachment visitAttachment(ProcessInstance process, AttachmentCommands.Attachment source) {
    final var id = UserAttachmentBuilderImpl.attachmentId(source.getName(), process);
    
    return ImmutableUserActionAttachment.builder()
        .id(id)
        .processId(process.getId().toString())
        .taskId(Optional.ofNullable(process.getTaskId()).map(e -> e.toString()).orElse(null))
        .name(source.getName())
        .created(source.getCreated().toString())
        .size(source.getSize())
        .status(source.getStatus().name())
        .build();
  }
  
  private Uni<TasksContext> visitTasks(List<ProcessInstance> processes, CustomerId userId) {
    final var config = taskClient.unwrap().getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    final var taskIds = processes.stream().filter(t -> t.getTaskId() != null).map(t -> t.getTaskId()).toList();
    
    return Uni.combine().all().unis(
        taskClient.queryTasks().findAll(taskIds), 
        grim.find().commitViewersQuery().usedBy(userId.getSafeId()).findAll()
      )
      .asTuple()
      .onItem().transform(tuple -> {
  
        final var allTasks =  tuple.getItem1();
        final var unreadTasks =  tuple.getItem2();
        
        return new TasksContext(
            allTasks.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)), 
            unreadTasks.getObjects().stream().collect(Collectors.groupingBy(GrimCommitViewer::getMissionId))
        );      
      });
    
  }
  
  private Uni<Optional<ProcessAuthorization>> visitAuthorization() {
    if(authClient.getCustomer().getPrincipal().getRepresentedId() != null) {
      final var userRoles = authClient.getCustomerRoles().getRoles();  
      return new ProcessAuthorizationQueryImpl(eveliEnvir)
          .cockpitId(cockpitId)
          .userRoles(userRoles)
          .getOne()
          .onItem().transform(allowed -> Optional.of(allowed));
    }
    
    return Uni.createFrom().item(Optional.empty());
  }
  
  private UserMessagesContext visitUserActionMessages(ProcessInstance process, TasksContext tasks) {
    
    // task not created yet
    if(process.getTaskId() == null) {
      return new UserMessagesContext(Collections.emptyList(), true, process.getUpdated());
    }
    final var user = authClient.getCustomer();
    final var task = tasks.getTasksById().get(process.getTaskId());
    
    if(task == null) {
      log.error("Process id: {} has no task with id: {}", process.getId(), process.getTaskId());
      return new UserMessagesContext(Collections.emptyList(), true, process.getUpdated());
    }
    
    var lastUpdate = process.getUpdated();
    final var userMessages = new ArrayList<UserMessage>();
    for(final var msg : task.getComments()) {
      if(!Boolean.TRUE.equals(msg.getExternal())) {
        continue;
      }

      userMessages.add(UserMessagesQueryImpl.visitUserMessage(msg, user));
      
      final var msgCreated = msg.getCreated().toOffsetDateTime();
      if(lastUpdate.isBefore(msgCreated)) {
        lastUpdate = msgCreated;
      }
    }
    
    final var taskViewedLastAt = tasks.getViews().getOrDefault(task.getId(), Collections.emptyList())
        .stream().map(e -> e.getCreatedAt())
        .max(Comparator.naturalOrder())
        .orElse(lastUpdate);
    
    final var isMessagingDisabled = userMessages.isEmpty();
    
    final var isViewed = isMessagingDisabled || taskViewedLastAt.isAfter(lastUpdate);
    return new UserMessagesContext(userMessages, isViewed, lastUpdate);
  }
  

  
  private UserAction visitUserAction(ProcessInstance process, TasksContext tasks) {
    final var messages = visitUserActionMessages(process, tasks);
    final var task =  Optional.ofNullable(process.getTaskId())
        .map(taskId -> tasks.getTasksById().get(taskId));
    final var taskRef = task
        .map(t -> t.getTaskRef())
        .orElse(null);
    
    final List<UserSubAction> subActions = task
        .map(t -> t.getCustomerAssignments())
        .orElse(Collections.emptyList()).stream()
        .filter(e -> e.getProcessId() != null)
        .filter(e -> e.getStatus() != TaskAssignmentStatus.CANCELLED)
        .filter(e -> e.getStatus() != TaskAssignmentStatus.NEW)
        .map(assignment -> {
          final UserSubAction action = ImmutableUserSubAction.builder()
              .id(assignment.getProcessId())
              .formId(assignment.getQuestionnaireId())
              .formInProgress(assignment.getStatus() == TaskAssignmentStatus.OPEN)
              .build();
          return action;
        }).toList();
    
    final var att = visitAttachments(process);
    
    return ImmutableUserAction.builder()
        .id(process.getId().toString())
        .taskId(Optional.ofNullable(process.getTaskId()).map(e -> e.toString()).orElse(null))
        .status(process.getStatus().name())
        .created(process.getCreated())
        .updated(process.getUpdated())
        .name(process.getWorkflowName())
        .inputContextId(process.getArticleName())
        .inputParentContextId(process.getParentArticleName())
        .formId(process.getQuestionnaireId() == null ? null : process.getQuestionnaireId())
        .formInProgress(process.getStatus() == ProcessStatus.ANSWERING || process.getStatus() == ProcessStatus.CREATED)        
        .taskRef(taskRef)
        .taskStatus(task.map(t -> t.getStatus().name()).orElse(null))
        .taskCreated(task.map(t -> t.getCreated()).orElse(null))
        .taskUpdated(task.map(t -> t.getUpdated()).orElse(null))
        .assigned(process.getType() == GrimProcessType.CUSTOMER_ASSIGNMENT ? true : false)
        .viewed(messages.isViewed())
        .updated(messages.getUpdated())
        .addAllAttachments(att.getProcessAttachments().stream().map(attachment -> visitAttachment(process, attachment)).toList())
        .addAllAttachments(att.getTaskAttachments().stream().map(attachment -> visitAttachment(process, attachment)).toList())
        .subActions(subActions)
        .addAllMessages(messages.getMessages())
        .cockpitId(process.getCockpitId())
        .build();
  }
  
  private boolean isAuthorizedProcess(ProcessInstance process, Optional<ProcessAuthorization> authorization) {
    if(authorization.isEmpty()) {
      return true;
    }
    return authorization.get().getAllowedProcessNames().contains(process.getWorkflowName());
  }
  

  @Data
  @RequiredArgsConstructor
  private static class TasksContext {
    private final Map<String, Task> tasksById;
    private final Map<String, List<GrimCommitViewer>> views;
  }
  
  
  @Data
  @RequiredArgsConstructor
  private static class AttachmentsContext {
    private final List<AttachmentCommands.Attachment> processAttachments;
    private final List<AttachmentCommands.Attachment> taskAttachments;
  }
  @Data
  @RequiredArgsConstructor
  private static class UserMessagesContext {
    private final List<UserMessage> messages;
    private final boolean viewed;
    private final OffsetDateTime updated;
  }
}
