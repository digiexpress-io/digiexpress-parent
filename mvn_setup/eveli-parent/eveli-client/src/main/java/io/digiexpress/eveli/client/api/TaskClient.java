package io.digiexpress.eveli.client.api;

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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.immutables.value.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.api.TaskCommand.TaskUpdateCommand;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimMissionStats.GrimMissionAttributeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import jakarta.json.JsonPatch.Operation;

public interface TaskClient {
  TaskStore unwrap();
  
  TaskCommandBuilder taskBuilder();  
  PaginateTasks paginateTasks();
  QueryTasks queryTasks();
  
  DeleteTasks deleteTasks();
  
  ModifyTaskProcess modifyProcess();
  
  QueryUnreadUserTasks queryUnreadUserTasks();
  QueryTaskComments queryTaskComments();
  QueryTaskKeywords queryTaskKeywords();
  QueryTaskDasboard queryTaskDasboard();
  
  QueryTaskProcesess queryTaskProcesess(); 
  
  QueryFormAssignments queryFormAssignments();
  
  
  
  interface QueryFormAssignments {
    Multi<FormAssignment> findAll(String taskId);
  }
  
  interface ModifyTaskProcess {
    ModifyTaskProcess id(String id);
    ModifyTaskProcess commitMessage(String commitMessage);
    ModifyTaskProcess commitAuthor(String commitAuthor);
    ModifyTaskProcess status(ProcessStatus status);
    Uni<ProcessClient.ProcessInstance> build();
  }
  
  interface QueryTaskProcesess {
    Uni<Optional<ProcessClient.ProcessInstance>> findOneByTaskId(String taskId);
    Multi<ProcessClient.ProcessInstance> findLast6Months();
    Multi<ProcessClient.ProcessInstance> findStaleWithoutTasks(OffsetDateTime olderThen); 
  }
  
  interface QueryTaskDasboard {
    QueryTaskDasboard requireAnyRoles(List<String> roles);
    Uni<TaskDasboard> findAll();
  }
  
  interface DeleteTasks {
    DeleteTasks commitMessage(String commitMessage);
    DeleteTasks commitAuthor(String commitAuthor);
    Uni<TaskArchivePointer> deleteOne(String id);
  }
  
  interface TaskCommandBuilder {
    TaskCommandBuilder userId(String userId, String userEmail);
    
    
    Uni<Task> createTask(CreateTaskCommand command);
    Uni<TaskComment> createTaskComment(CreateTaskCommentCommand command);
    
    Uni<Task> modifyTask(String taskId, ModifyTaskCommand command);
    Uni<Task> deleteTask(String taskId);
    Uni<Task> transferTask(String taskId, TransferTaskCommand command);
    Uni<Task> completeCustomerAssignment(String taskId, CompleteCustomerAssignmentCommand command);
    
    Uni<Void> addWorkerCommitViewer(String taskId);
    Uni<Void> addCustomerCommitViewer(String taskId);

  }
  
  interface QueryTaskComments {
    Uni<List<TaskComment>> findAllByTaskId(String taskId);
    Uni<List<TaskComment>> findAllByReporterId(String reporterId);
    Uni<TaskComment> getOneById(String commentId);
  }
  
  interface QueryTaskKeywords {
    Uni<List<String>> findAllKeywords();
  }
  
  interface QueryUnreadUserTasks {
    QueryUnreadUserTasks workerId(String userId);
    QueryUnreadUserTasks customerId(String userId);
    QueryUnreadUserTasks requireAnyRoles(List<String> roles);
    Uni<List<String>> findAll();
  }
  
  interface QueryTasks {
    QueryTasks requireAnyRoles(List<String> roles);
    Uni<Task> getOneById(String taskId);
    Uni<TaskDiff> getOneTaskDiff(String taskId, String commitId);
    Uni<List<Task>> findAll(List<String> taskId);
    Uni<List<Task>> findAll();
  }
  
  interface PaginateTasks {
    PaginateTasks page(@Nullable Pageable pageable);
    PaginateTasks subject(@Nullable String subject); 
    PaginateTasks clientIdentificator(@Nullable String clientIdentificator);
    PaginateTasks assignedUser(@Nullable String assignedUser);
    PaginateTasks additionalInfo(@Nullable String additionalInfo);
    PaginateTasks status(@Nullable List<TaskStatus> status);
    PaginateTasks priority(@Nullable List<TaskPriority> priority);
    PaginateTasks dueDate(@Nullable String dueDate);
    PaginateTasks role(@Nullable String role); // find task assigned to the role
    
    PaginateTasks requireAnyRoles(List<String> roles); // secondary role filter, must contain at least one of these
    Uni<Page<Task>> findAll();
  }
  



  @JsonSerialize(as = ImmutableCreateTaskCommand.class)
  @JsonDeserialize(as = ImmutableCreateTaskCommand.class)
  @Value.Immutable
  interface CreateTaskCommand extends TaskCommand {
    // null on new task
    @Nullable TaskStatus getStatus();
    @Nullable ZonedDateTime getCompleted();

    // optional props
    @Nullable String getAdditionalInfo();
    @Nullable String getDescription();
    @Nullable String getClientLanguage();
    @Nullable String getClientIdentificator();
    @Nullable LocalDate getDueDate();
    
    @Nullable String getAssignedId();
    @Nullable String getAssignedUser();    
    @Nullable String getAssignedUserEmail();
    
    @Nullable String getQuestionnaireId();
    
    String getSubject();
    @Nullable TaskPriority getPriority();
    @Nullable String getFeaturesAsCsv();

    List<String> getKeyWords();
    List<String> getFeatures();
    Set<String> getAssignedRoles();
    
    List<TaskComment> getComments();
    List<Checklist> getChecklist();
    
    Map<String, String> getDocumentProperties();
    
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.CreateTask; }
    
  }
  
  
  @JsonSerialize(as = ImmutableCreateTaskCommentCommand.class)
  @JsonDeserialize(as = ImmutableCreateTaskCommentCommand.class)
  @Value.Immutable
  interface CreateTaskCommentCommand extends TaskUpdateCommand {
    @Nullable Boolean getExternal();
    @Nullable String getReplyToId();
    String getTaskId();
    String getCommentText();
    TaskCommentSource getSource(); 
    
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.CommentOnTask; }
  }
  
  
  @JsonSerialize(as = ImmutableModifyTaskCommand.class)
  @JsonDeserialize(as = ImmutableModifyTaskCommand.class)
  @Value.Immutable
  interface ModifyTaskCommand  extends TaskUpdateCommand {
    @Nullable TaskStatus getStatus();
    @Nullable ZonedDateTime getCompleted();
    @Nullable String getVersion();

    @Nullable String getDescription();
    @Nullable String getClientIdentificator();
    @Nullable LocalDate getDueDate();
    @Nullable String getAssignedId();
    @Nullable String getAdditionalInfo();
    
    @Nullable String getAssignedUser();    
    @Nullable String getAssignedUserEmail();
    @Nullable TaskPriority getPriority();
    
    String getSubject();
    Set<String> getAssignedRoles();
    
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ModifyTask; }
  }
  
  
  
  @JsonSerialize(as = ImmutableTransferTaskCommand.class)
  @JsonDeserialize(as = ImmutableTransferTaskCommand.class)
  @Value.Immutable
  interface TransferTaskCommand {
    String getTransferTitle();
    Map<String, String> getTransferProps();
  }
  
  
  @JsonSerialize(as = ImmutableCompleteCustomerAssignmentCommand.class)
  @JsonDeserialize(as = ImmutableCompleteCustomerAssignmentCommand.class)
  @Value.Immutable
  interface CompleteCustomerAssignmentCommand {
    String getAssignmentId(); 
    String getTaskVersion();
    ZonedDateTime getTargetDate();
  }
  
  
  
  enum TaskStatus { NEW, OPEN, COMPLETED, TRANSFERRED, REJECTED, DELEGATED, WAITING }
  enum TaskPriority { LOW, NORMAL, HIGH }
  enum TaskCommentSource { FRONTDESK, PORTAL }
  enum TaskAssignmentStatus { OPEN, COMPLETED }
  
  @JsonSerialize(as = ImmutableTask.class)
  @JsonDeserialize(as = ImmutableTask.class)
  @Value.Immutable
  interface Task {
    // null on new task
    String getId();
    ZonedDateTime getCreated();
    @Nullable ZonedDateTime getUpdated();
    String getUpdaterId();
    
    String getTaskRef(); // Task reference, semantic ID for task.
    TaskStatus getStatus();
    @Nullable ZonedDateTime getCompleted();
    String getVersion();

    // optional props
    @Nullable String getQuestionnaireId();
    @Nullable String getDescription();
    @Nullable String getClientIdentificator();
    @Nullable String getClientLanguage();
    @Nullable String getAdditionalInfo();
    @Nullable LocalDate getDueDate();
    
    @Nullable String getAssignedId();
    @Nullable String getAssignedUser();
    @Nullable String getAssignedUserEmail();


    Map<String, String> getDocumentProperties();
    @Nullable String getTransferredId();
    @Nullable JsonObject getTransferredProps();
    
    String getSubject();
    TaskPriority getPriority();

    List<String> getKeyWords();
    List<String> getFeatures();
    Set<String> getAssignedRoles();

    List<TaskComment> getComments();
    List<TaskCustomerAssignment> getCustomerAssignments();
  }
  
  @JsonSerialize(as = ImmutableTaskCustomerAssignment.class)
  @JsonDeserialize(as = ImmutableTaskCustomerAssignment.class)
  @Value.Immutable
  interface TaskCustomerAssignment {
    String getId();
    ZonedDateTime getCreated();
    TaskAssignmentStatus getStatus();
    String getQuestionnaireId();
  }
  
  
  @JsonSerialize(as = ImmutableFormAssignment.class)
  @JsonDeserialize(as = ImmutableFormAssignment.class)
  @Value.Immutable
  interface FormAssignment {
    List<String> getLocales();
    
    String getServiceName();
    String getFormId();
    String getFormName();
    String getFormTag();
  }
  
  
  @JsonSerialize(as = ImmutableTaskComment.class)
  @JsonDeserialize(as = ImmutableTaskComment.class)
  @Value.Immutable
  interface TaskComment {
    
    String getId();
    String getVersion();
    ZonedDateTime getCreated();
    
    Boolean getExternal();
    @Nullable String getUserName();
    @Nullable String getReplyToId();
    
    String getTaskId();
    String getCommentText();
    TaskCommentSource getSource(); 
  }
  
  @JsonSerialize(as = ImmutableTaskDiff.class)
  @JsonDeserialize(as = ImmutableTaskDiff.class)
  @Value.Immutable
  interface TaskDiff {
    String getTaskId();
    String getVersion();
    
    @Nullable Task getTask();
    String getLog();
    List<TaskDiffValue> getValues();
  }
  
  @JsonSerialize(as = ImmutableTaskDiffValue.class)
  @JsonDeserialize(as = ImmutableTaskDiffValue.class)
  @Value.Immutable
  interface TaskDiffValue {
    Operation getOp();
    String getPath();
    @Nullable Object getRaw();
    @Nullable String getValue();
  }
  
  
  
  @JsonSerialize(as = ImmutableTaskDasboard.class)
  @JsonDeserialize(as = ImmutableTaskDasboard.class)
  @Value.Immutable
  interface TaskDasboard {
    List<GrimMissionAttributeEvent> getEvents();
  }
  
  @JsonSerialize(as = ImmutableTaskArchivePointer.class)
  @JsonDeserialize(as = ImmutableTaskArchivePointer.class)
  @Value.Immutable
  interface TaskArchivePointer {
    GrimCommit getCommit();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableChecklist.class) @JsonDeserialize(as = ImmutableChecklist.class)
  interface Checklist {
    String getId();
    String getTitle();
    
    List<ChecklistItem> getItems();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChecklistItem.class) @JsonDeserialize(as = ImmutableChecklistItem.class)
  interface ChecklistItem {
    String getId();
    List<String> getAssigneeIds();
    @Nullable LocalDate getDueDate();
    Boolean getCompleted();
    String getTitle();
  }
}
