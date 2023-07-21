package io.resys.thena.tasks.client.api.model;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.tasks.client.api.model.Task.Priority;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.api.model.Task.TaskComment;
import io.resys.thena.tasks.client.api.model.Task.TaskExtension;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateTask.class, name = "CreateTask"),
  @Type(value = ImmutableChangeTaskStatus.class, name = "ChangeTaskStatus"),
  @Type(value = ImmutableChangeTaskPriority.class, name = "ChangeTaskPriority"),
  @Type(value = ImmutableAssignTaskReporter.class, name = "AssignTaskReporter"),
  
  @Type(value = ImmutableArchiveTask.class, name = "ArchiveTask"),
  @Type(value = ImmutableCommentOnTask.class, name = "CommentOnTask"),
  @Type(value = ImmutableChangeTaskComment.class, name = "ChangeTaskComment"),
  @Type(value = ImmutableAssignTaskRoles.class, name = "AssignTaskRoles"),
  @Type(value = ImmutableAssignTask.class, name = "AssignTask"),
  
  @Type(value = ImmutableChangeTaskDueDate.class, name = "ChangeTaskDueDate"),
  @Type(value = ImmutableChangeTaskInfo.class, name = "ChangeTaskInfo"),
  @Type(value = ImmutableCreateTaskExtension.class, name = "CreateTaskExtension"),
  @Type(value = ImmutableChangeTaskExtension.class, name = "ChangeTaskExtension"),
  @Type(value = ImmutableAssignTaskParent.class, name = "AssignTaskParent"),

})
public interface TaskCommand extends Serializable {
  String getUserId();
  @Nullable LocalDateTime getTargetDate();
  TaskCommandType getCommandType();
  
  enum TaskCommandType {
    CreateTask, ChangeTaskStatus, ChangeTaskPriority, AssignTaskReporter, 
    ArchiveTask, CommentOnTask, ChangeTaskComment, AssignTaskRoles, AssignTask, ChangeTaskStartDate,
    ChangeTaskDueDate, ChangeTaskInfo, CreateTaskExtension, ChangeTaskExtension, AssignTaskParent
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateTask.class) @JsonDeserialize(as = ImmutableCreateTask.class)
  interface CreateTask extends TaskCommand {
    List<String> getRoles();
    List<String> getAssigneeIds();
    String getReporterId();
    
    @Nullable Status getStatus();
    @Nullable LocalDate getStartDate();
    @Nullable LocalDate getDueDate();
    String getTitle();
    String getDescription();
    Priority getPriority();
    
    List<String> getLabels();
    List<TaskExtension> getExtensions();
    List<TaskComment> getComments();
    
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.CreateTask; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangeTaskStatus.class, name = "ChangeTaskStatus"),
    @Type(value = ImmutableChangeTaskPriority.class, name = "ChangeTaskPriority"),
    @Type(value = ImmutableAssignTaskReporter.class, name = "AssignTaskReporter"),
    
    @Type(value = ImmutableArchiveTask.class, name = "ArchiveTask"),
    @Type(value = ImmutableCommentOnTask.class, name = "CommentOnTask"),
    @Type(value = ImmutableChangeTaskComment.class, name = "ChangeTaskComment"),
    @Type(value = ImmutableAssignTaskRoles.class, name = "AssignTaskRoles"),
    @Type(value = ImmutableAssignTask.class, name = "AssignTask"),

    @Type(value = ImmutableChangeTaskStartDate.class, name = "ImmutableChangeTaskStartDate"),
    @Type(value = ImmutableChangeTaskDueDate.class, name = "ChangeTaskDueDate"),
    @Type(value = ImmutableChangeTaskInfo.class, name = "ChangeTaskInfo"),
    @Type(value = ImmutableCreateTaskExtension.class, name = "CreateTaskExtension"),
    @Type(value = ImmutableChangeTaskExtension.class, name = "ChangeTaskExtension"),
    @Type(value = ImmutableAssignTaskParent.class, name = "AssignTaskParent"),
  })
  interface TaskUpdateCommand extends TaskCommand {
    String getTaskId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableAssignTaskReporter.class) @JsonDeserialize(as = ImmutableAssignTaskReporter.class)
  interface AssignTaskReporter extends TaskUpdateCommand {
    String getReporterId();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.AssignTaskReporter; }

  }

  @Value.Immutable @JsonSerialize(as = ImmutableArchiveTask.class) @JsonDeserialize(as = ImmutableArchiveTask.class)
  interface ArchiveTask extends TaskUpdateCommand {
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ArchiveTask; }
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskStatus.class) @JsonDeserialize(as = ImmutableChangeTaskStatus.class)
  interface ChangeTaskStatus extends TaskUpdateCommand {
    Task.Status getStatus();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskStatus; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskPriority.class) @JsonDeserialize(as = ImmutableChangeTaskPriority.class)
  interface ChangeTaskPriority extends TaskUpdateCommand {
    Priority getPriority();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskPriority; }
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableAssignTaskParent.class) @JsonDeserialize(as = ImmutableAssignTaskParent.class)
  interface AssignTaskParent extends TaskUpdateCommand {
    @Nullable String getParentId(); 
    @Override default TaskCommandType getCommandType() { return TaskCommandType.AssignTaskParent; }
  }
    
  @Value.Immutable @JsonSerialize(as = ImmutableCommentOnTask.class) @JsonDeserialize(as = ImmutableCommentOnTask.class)
  interface CommentOnTask extends TaskUpdateCommand {
    @Nullable String getReplyToCommentId();
    String getCommentText();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.CommentOnTask; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskComment.class) @JsonDeserialize(as = ImmutableChangeTaskComment.class)
  interface ChangeTaskComment extends TaskUpdateCommand {
    String getCommentId();
    @Nullable String getReplyToCommentId();
    String getCommentText();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskComment; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableAssignTaskRoles.class) @JsonDeserialize(as = ImmutableAssignTaskRoles.class)
  interface AssignTaskRoles extends TaskUpdateCommand {
    List<String> getRoles();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.AssignTaskRoles; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableAssignTask.class) @JsonDeserialize(as = ImmutableAssignTask.class)
  interface AssignTask extends TaskUpdateCommand {
    List<String> getAssigneeIds();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.AssignTask; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskStartDate.class) @JsonDeserialize(as = ImmutableChangeTaskStartDate.class)
  interface ChangeTaskStartDate extends TaskUpdateCommand {
    Optional<LocalDate> getStartDate();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskStartDate; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskDueDate.class) @JsonDeserialize(as = ImmutableChangeTaskDueDate.class)
  interface ChangeTaskDueDate extends TaskUpdateCommand {
    Optional<LocalDate> getDueDate();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskDueDate; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskInfo.class) @JsonDeserialize(as = ImmutableChangeTaskInfo.class)
  interface ChangeTaskInfo extends TaskUpdateCommand {
    String getTitle();
    String getDescription();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskInfo; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateTaskExtension.class) @JsonDeserialize(as = ImmutableCreateTaskExtension.class)
  interface CreateTaskExtension extends TaskUpdateCommand {
    String getType();
    String getName();
    String getBody();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.CreateTaskExtension; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangeTaskExtension.class) @JsonDeserialize(as = ImmutableChangeTaskExtension.class)
  interface ChangeTaskExtension extends TaskUpdateCommand {
    String getId();
    String getType();
    String getName();
    String getBody();
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeTaskExtension; }
  }
}
