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
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.immutables.value.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.TaskCommand.TaskUpdateCommand;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimMissionStats.GrimMissionAttributeEvent;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
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
  DeleteProcesses deleteProcesses();
  
  ModifyProcess modifyProcess();
  CreateProcess createProcess();
  
  QueryUnreadUserTasks queryUnreadUserTasks();
  QueryTaskComments queryTaskComments();
  QueryTaskKeywords queryTaskKeywords();
  QueryTaskDasboard queryTaskDasboard();
  
  QueryTaskProcesess queryTaskProcesess(); 
  
  QueryFormAssignments queryFormAssignments();
  
  
  
  interface QueryFormAssignments {
    Multi<FormAssignment> findAll(String taskId);
  }
  
  interface CreateProcess {  
    CreateProcess questionnaireId(String questionnaire);
    CreateProcess userId(String userId);
    CreateProcess expiresInSeconds(Long expires_in_seconds);
    CreateProcess expiresAt(OffsetDateTime expiresAt);
    CreateProcess workflowName(String name);
    
    CreateProcess anon(boolean anon);
    
    CreateProcess articleName(String articleName);
    CreateProcess parentArticleName(String parentArticleName);
    
    CreateProcess taskId(@Nullable String taskId);
    CreateProcess formName(String formName);
    CreateProcess flowName(String flowName);

    CreateProcess formBody(@Nullable String formBody);
    CreateProcess formTagName(String formTagName);
    CreateProcess stencilTagName(String stencilTagName);
    CreateProcess wrenchTagName(String wrenchTagName);
    CreateProcess customerAssignment(boolean isCustomerAssignment);
    CreateProcess cockpitId(@Nullable String cockpitId);
    
    CreateProcess commitAuthor(String author);
    CreateProcess commitMessage(String message);
    
    Uni<ProcessInstance> build();
  }
  
  interface ModifyProcess {
    ModifyProcess id(String id);
    ModifyProcess commitMessage(String commitMessage);
    ModifyProcess commitAuthor(String commitAuthor);
    
    ModifyProcess onAnyUni(Function<MergeProcess, Uni<?>> callback); // stage 1: do some async tasks after locking proc table and before on mission
    ModifyProcess onTask(BiConsumer<Optional<Task>, MergeProcess> onMission); // stage 2: resolves mission by questionnaire id
    
    ModifyProcess merge(BiConsumer<ProcessInstance, MergeProcess> merger);
    
    Uni<ProcessInstance> build();
  }
  
  interface MergeProcess {
    
    
    MergeProcess status(GrimProcessStatus status);
    MergeProcess taskId(String taskId);
    MergeProcess formBody(String formBody);
    MergeProcess flowBody(String flowBody);
    
    ProcessInstance getCurrentState();
    
    // skips the mod and returns current state without mods
    ProcessInstance skip();
    
    // apply modifications
    ProcessInstance build();
  }
  
  interface QueryTaskProcesess {
    QueryTaskProcesess includeQuestionnaire();
    
    Uni<ProcessInstance> getOneById(String processId);
    
    Uni<Optional<ProcessInstance>> findOneByTaskId(String taskId);
    Uni<Optional<ProcessInstance>> findOneById(String processId);
    Uni<Optional<ProcessInstance>> findOneByQuestionnaireId(String questionnaireId);
    
    Multi<ProcessInstance> findAll();
    Multi<ProcessInstance> findAllExpired();
    Multi<ProcessInstance> findAllInLast6Months();
    Multi<ProcessInstance> findAllStaleWithoutTasks(OffsetDateTime olderThen);
    Multi<ProcessInstance> findAllNotArchivedyUserId(String userId);
    Multi<ProcessInstance> findAllAnsweredFrom(OffsetDateTime pickupFrom);
    
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
  
  interface DeleteProcesses {
    DeleteProcesses commitMessage(String commitMessage);
    DeleteProcesses commitAuthor(String commitAuthor);
    Uni<ProcessInstance> deleteOne(String id);
  }
  
  interface TaskCommandBuilder {
    TaskCommandBuilder userId(String userId, String userEmail);
    
    Uni<Task> createTask(CreateTaskCommand command);
    Uni<TaskComment> createTaskComment(CreateTaskCommentCommand command);
    
    Uni<Task> modifyTask(String taskId, ModifyTaskCommand command);
    Uni<Task> deleteTask(String taskId);
    Uni<Task> transferTask(String taskId, TransferTaskCommand command);
    
    Uni<Task> completeCustomerAssignment(String taskId, CompleteCustomerAssignmentCommand command);
    Uni<Task> createCustomerAssignment(String taskId, List<CreateCustomerAssignmentCommand> command);
    Uni<Task> addFormToCustomerAssignment(String taskId, List<AddFormToCustomerAssignmentCommand> command);
    Uni<Task> deleteCustomerTaskAssignment(String taskId, List<String> assignmentId);
    Uni<Task> changeDocProperties(String taskId, ChangeDocPropertiesCommand command);
    
    Uni<Void> addWorkerCommitViewer(String taskId);
    Uni<Void> addCustomerCommitViewer(String taskId);

  }
  
  interface QueryTaskComments {
    Multi<TaskComment> findAllByTaskId(String taskId);
    Multi<TaskComment> findAllByReporterId(String reporterId);
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
    Uni<Optional<Task>> findOneById(String taskId);
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
    @Nullable String getProcessId();
    Map<String, String> getTransferProps();
  }
  
  
  @JsonSerialize(as = ImmutableCompleteCustomerAssignmentCommand.class)
  @JsonDeserialize(as = ImmutableCompleteCustomerAssignmentCommand.class)
  @Value.Immutable
  interface CompleteCustomerAssignmentCommand {
    String getAssignmentId(); 
    @Nullable String getTaskVersion(); // perform version check if not null
    ZonedDateTime getTargetDate();
  }
  
  @JsonSerialize(as = ImmutableCreateCustomerAssignmentCommand.class)
  @JsonDeserialize(as = ImmutableCreateCustomerAssignmentCommand.class)
  @Value.Immutable
  interface CreateCustomerAssignmentCommand {
    String getServiceId();
    String getTaskId();
    String getLocale();
    @Nullable String getTaskVersion(); // perform version check if not null
  }
  
  @JsonSerialize(as = ImmutableAddFormToCustomerAssignmentCommand.class)
  @JsonDeserialize(as = ImmutableAddFormToCustomerAssignmentCommand.class)
  @Value.Immutable
  interface AddFormToCustomerAssignmentCommand {
    String getAssignmentId();
    String getTaskId();
    @Nullable String getTaskVersion(); // perform version check if not null
    
    String getQuestionnaireId();
    String getProcessId();
  }
  
  
  
  enum TaskStatus { NEW, OPEN, COMPLETED, TRANSFERRED, REJECTED, DELEGATED, WAITING }
  enum TaskPriority { LOW, NORMAL, HIGH }
  enum TaskCommentSource { FRONTDESK, PORTAL }
  enum TaskAssignmentStatus { 
    NEW,  // waiting for the form to be created
    OPEN, // ready to be filled by the user 
    COMPLETED, // filled by the user
    CANCELLED  // cancelled, not gonna be filled
  }
  
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
    
    @JsonIgnore
    default boolean isNewCustomerAssignment() {
      return this.getCustomerAssignments().stream().filter(t -> t.getStatus() == TaskAssignmentStatus.NEW).findAny().isPresent();
    }
  }
  
  @JsonSerialize(as = ImmutableTaskCustomerAssignment.class)
  @JsonDeserialize(as = ImmutableTaskCustomerAssignment.class)
  @Value.Immutable
  interface TaskCustomerAssignment {
    String getId();
    String getServiceName();
    String getDescription();
    String getLocale();
    
    
    @Nullable String getQuestionnaireId();
    @Nullable String getProcessId();
    
    OffsetDateTime getCreated();
    TaskAssignmentStatus getStatus();
    String getExternalId();
  }
  
  
  @JsonSerialize(as = ImmutableFormAssignment.class)
  @JsonDeserialize(as = ImmutableFormAssignment.class)
  @Value.Immutable
  interface FormAssignment {
    String getId();
    String getLocale();
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
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableProcessInstance.class)
  @JsonDeserialize(as = ImmutableProcessInstance.class)
  interface ProcessInstance {
    Long getId();
    GrimProcessStatus getStatus();
    OffsetDateTime getCreated();
    OffsetDateTime getUpdated();
    
    @Nullable OffsetDateTime getExpiresAt();
    @Nullable Long getExpiresInSeconds();
    
    String getWorkflowName();
    @Nullable String getFormName();
    @Nullable String getFlowName();
    @Nullable String getArticleName();
    @Nullable String getParentArticleName();
    
    // Entity links
    @Nullable String getQuestionnaireId();
    // only when explicitly loaded 
    @Nullable JsonObject getQuestionnaire();
    
    @Nullable String getTaskId();
    @Nullable String getTaskRef();
    @Nullable String getUserId();
    @Nullable GrimProcessType getType();

    
    
    Boolean getAnon();
    
    // Asset links
    @Nullable String getFormTagName();
    @Nullable String getStencilTagName();
    @Nullable String getWrenchTagName();
    
    // Additional tenant based configuration is provided
    @Nullable String getCockpitId();
  }
  
  
  @JsonSerialize(as = ImmutableChangeDocPropertiesCommand.class)
  @JsonDeserialize(as = ImmutableChangeDocPropertiesCommand.class)
  @Value.Immutable
  interface ChangeDocPropertiesCommand  extends TaskUpdateCommand {
    Map<String, String> getDocumentProperties();
    
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeDocProperties; }
  }
}
