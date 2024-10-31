package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient.UserActionQuery;
import io.digiexpress.eveli.client.api.HdesCommands;
import io.digiexpress.eveli.client.api.HdesCommands.ProcessAuthorization;
import io.digiexpress.eveli.client.api.ImmutableInitProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessCommands.ProcessStatus;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.thestencil.iam.api.ImmutableAttachment;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class UserActionsQueryImpl implements UserActionQuery {
  
  private final TaskRepository taskRepository;
  private final ProcessRepository processRepository;
  private final CrmClient authClient;
  private final HdesCommands hdesCommands;
  private final AttachmentCommands attachmentsCommands;
  
  @Override
  public List<UserAction> findAll() {
    final var ssn = visitUserId();
    final var processes = processRepository.findAllByUserId(ssn);
    final var tasks = visitTasks(processes, ssn);
    final var auth = visitAuthorization();
    
    return processes.stream()
        .filter(process -> isAuthorizedProcess(process, auth))
        .map(process -> visitUserAction(process, tasks))
        .toList();
  }
  
  
  private String visitUserId() {
    final var customer = authClient.getCustomer().getPrincipal();
    if(customer.getRepresentedId() == null) {
      return customer.getRepresentedId();
    }
    return customer.getSsn();
  }
  
  private AttachmentsContext visitAttachments(ProcessEntity process) {
    final List<AttachmentCommands.Attachment> processAttachments = attachmentsCommands.query().processId(process.getId().toString());
    final List<AttachmentCommands.Attachment> taskAttachments = process.getTask() == null ? Collections.emptyList(): attachmentsCommands.query().taskId(process.getTask());
    return new AttachmentsContext(processAttachments, taskAttachments);
  }
  
  private ImmutableAttachment visitAttachment(ProcessEntity process, AttachmentCommands.Attachment source) {
    final var id = UserAttachmentBuilderImpl.attachmentId(source.getName(), process);
    
    return ImmutableAttachment.builder()
        .id(id)
        .processId(process.getId().toString())
        .taskId(process.getTask())
        .name(source.getName())
        .created(source.getCreated().toString())
        .size(source.getSize())
        .status(source.getStatus().name())
        .build();
  }
  
  private TasksContext visitTasks(List<ProcessEntity> processes, String userId) {
    final var taskIds = processes.stream().filter(t -> t.getTask() != null).map(t -> Long.parseLong(t.getTask())).toList();
    final var unreadTasks = taskRepository.findUnreadExternalTasks(userId);
    final var allTasks = taskRepository.findAllTasksId(taskIds);    
    return new TasksContext(allTasks.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)), unreadTasks);
  }
  
  private Optional<ProcessAuthorization> visitAuthorization() {
    if(authClient.getCustomer().getPrincipal().getRepresentedId() != null) {
      final var userRoles = authClient.getCustomerRoles().getRoles();  
      final var allowed = hdesCommands.processAuthorizationQuery().get(ImmutableInitProcessAuthorization.builder()
          .addAllUserRoles(userRoles)
          .build());
      return Optional.of(allowed);      
    }
    
    return Optional.empty();
  }
  
  private UserMessagesContext visitUserActionMessages(ProcessEntity process, TasksContext tasks) {
    
    // task not created yet
    if(process.getTask() == null) {
      return new UserMessagesContext(Collections.emptyList(), true, process.getUpdated());
    }
    final var user = authClient.getCustomer();
    
    final var task = tasks.getTasksById().get(Long.parseLong(process.getTask()));
    var lastUpdate = process.getUpdated();
    final var userMessages = new ArrayList<UserMessage>();
    for(final var msg : task.getComments()) {
      if(!Boolean.TRUE.equals(msg.getExternal())) {
        continue;
      }

      userMessages.add(UserMessagesQueryImpl.visitUserMessage(msg, user));
      
      final var msgCreated = msg.getCreated().toLocalDateTime();
      if(lastUpdate.isBefore(msgCreated)) {
        lastUpdate = msgCreated;
      }
    }
    
    final var viewed = userMessages.isEmpty() || tasks.getUnreadTaskIds().contains(task.getId());
    return new UserMessagesContext(userMessages, viewed, lastUpdate);
  }
  

  
  private UserAction visitUserAction(ProcessEntity process, TasksContext tasks) {
    final var messages = visitUserActionMessages(process, tasks);
    final var taskRef = Optional.ofNullable(process.getTask())
        .map(Long::parseLong)
        .map(taskId -> tasks.getTasksById().get(taskId))
        .map(task -> task.getTaskRef())
        .orElse(null);
    
    final var att = visitAttachments(process);
    
    return ImmutableUserAction.builder()
        .id(process.getId().toString())
        .taskId(process.getTask())
        .status(process.getStatus().name())
        .created(process.getCreated())
        .updated(process.getUpdated())
        .name(process.getWorkflowName())
        .inputContextId(process.getInputContextId())
        .inputParentContextId(process.getInputParentContextId())
        .formId(process.getQuestionnaire())
        .formInProgress(process.getStatus() == ProcessStatus.ANSWERING || process.getStatus() == ProcessStatus.CREATED)        
        .taskRef(taskRef)        
        .viewed(messages.isViewed())
        .updated(messages.getUpdated())
        .addAllAttachments(att.getProcessAttachments().stream().map(attachment -> visitAttachment(process, attachment)).toList())
        .addAllAttachments(att.getTaskAttachments().stream().map(attachment -> visitAttachment(process, attachment)).toList())
        
        // deprecated
        .messagesUri("not-needed")
        .reviewUri("not-needed")
        .formUri("not-needed")
        .build();
  }
  
  private boolean isAuthorizedProcess(ProcessEntity process, Optional<ProcessAuthorization> authorization) {
    if(authorization.isEmpty()) {
      return true;
    }
    return authorization.get().getAllowedProcessNames().contains(process.getWorkflowName());
  }
  

  @Data
  @RequiredArgsConstructor
  private static class TasksContext {
    private final Map<Long, TaskEntity> tasksById;
    private final List<Long> unreadTaskIds;
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
    private final LocalDateTime updated;
  }
}
