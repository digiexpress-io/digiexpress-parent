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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.tasks.client.api.model.Task.Checklist;
import io.resys.thena.tasks.client.api.model.Task.ChecklistItem;
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
  
  @Type(value = ImmutableChangeTaskStartDate.class, name = "ChangeTaskStartDate"),
  @Type(value = ImmutableChangeTaskDueDate.class, name = "ChangeTaskDueDate"),
  @Type(value = ImmutableChangeTaskInfo.class, name = "ChangeTaskInfo"),
  @Type(value = ImmutableCreateTaskExtension.class, name = "CreateTaskExtension"),
  @Type(value = ImmutableChangeTaskExtension.class, name = "ChangeTaskExtension"),
  @Type(value = ImmutableAssignTaskParent.class, name = "AssignTaskParent"),
  
  @Type(value = ImmutableCreateChecklist.class, name = "CreateChecklist"),
  @Type(value = ImmutableChangeChecklistTitle.class, name = "ChangeChecklistTitle"),
  @Type(value = ImmutableDeleteChecklist.class, name = "DeleteChecklist"),
  @Type(value = ImmutableAddChecklistItem.class, name = "AddChecklistItem"),
  @Type(value = ImmutableDeleteChecklistItem.class, name = "DeleteChecklistItem"),
  @Type(value = ImmutableChangeChecklistItemAssignees.class, name = "ChangeChecklistItemAssignees"),
  @Type(value = ImmutableChangeChecklistItemCompleted.class, name = "ChangeChecklistItemCompleted"),
  @Type(value = ImmutableChangeChecklistItemDueDate.class, name = "ChangeChecklistItemDueDate"),
  @Type(value = ImmutableChangeChecklistItemTitle.class, name = "ChangeChecklistItemTitle")
})
public interface TaskCommand extends Serializable {
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  TaskCommandType getCommandType();

  TaskCommand withUserId(String userId);
  TaskCommand withTargetDate(Instant targetDate);
  
  enum TaskCommandType {
    CreateTask, ChangeTaskStatus, ChangeTaskPriority, AssignTaskReporter, 
    ArchiveTask, CommentOnTask, ChangeTaskComment, AssignTaskRoles, AssignTask, ChangeTaskStartDate,
    ChangeTaskDueDate, ChangeTaskInfo, CreateTaskExtension, ChangeTaskExtension, AssignTaskParent,
    CreateChecklist, ChangeChecklistTitle, DeleteChecklist, AddChecklistItem, DeleteChecklistItem,
    ChangeChecklistItemAssignees, ChangeChecklistItemCompleted, ChangeChecklistItemDueDate, ChangeChecklistItemTitle
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
    List<Checklist> getChecklist();
    
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

    @Type(value = ImmutableChangeTaskStartDate.class, name = "ChangeTaskStartDate"),
    @Type(value = ImmutableChangeTaskDueDate.class, name = "ChangeTaskDueDate"),
    @Type(value = ImmutableChangeTaskInfo.class, name = "ChangeTaskInfo"),
    @Type(value = ImmutableCreateTaskExtension.class, name = "CreateTaskExtension"),
    @Type(value = ImmutableChangeTaskExtension.class, name = "ChangeTaskExtension"),
    @Type(value = ImmutableAssignTaskParent.class, name = "AssignTaskParent"),
    
    @Type(value = ImmutableCreateChecklist.class, name = "CreateChecklist"),
    @Type(value = ImmutableChangeChecklistTitle.class, name = "ChangeChecklistTitle"),
    @Type(value = ImmutableDeleteChecklist.class, name = "DeleteChecklist"),
    @Type(value = ImmutableAddChecklistItem.class, name = "AddChecklistItem"),
    @Type(value = ImmutableDeleteChecklistItem.class, name = "DeleteChecklistItem"),
    @Type(value = ImmutableChangeChecklistItemAssignees.class, name = "ChangeChecklistItemAssignees"),
    @Type(value = ImmutableChangeChecklistItemCompleted.class, name = "ChangeChecklistItemCompleted"),
    @Type(value = ImmutableChangeChecklistItemDueDate.class, name = "ChangeChecklistItemDueDate"),
    @Type(value = ImmutableChangeChecklistItemTitle.class, name = "ChangeChecklistItemTitle")
  })
  interface TaskUpdateCommand extends TaskCommand {
    String getTaskId();
    TaskUpdateCommand withUserId(String userId);
    TaskUpdateCommand withTargetDate(Instant targetDate);
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateChecklist.class) @JsonDeserialize(as = ImmutableCreateChecklist.class)
  interface CreateChecklist extends TaskUpdateCommand {
    String getTitle();
    List<ChecklistItem> getChecklist();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.CreateChecklist; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangeChecklistTitle.class) @JsonDeserialize(as = ImmutableChangeChecklistTitle.class)
  interface ChangeChecklistTitle extends TaskUpdateCommand {
    String getChecklistId();
    String getTitle();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeChecklistTitle; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableDeleteChecklist.class) @JsonDeserialize(as = ImmutableDeleteChecklist.class)
  interface DeleteChecklist extends TaskUpdateCommand {
    String getChecklistId();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.DeleteChecklist; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableAddChecklistItem.class) @JsonDeserialize(as = ImmutableAddChecklistItem.class)
  interface AddChecklistItem extends TaskUpdateCommand {
    String getChecklistId();
    String getTitle();
    List<String> getAssigneeIds();
    @Nullable LocalDate getDueDate();
    Boolean getCompleted();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.AddChecklistItem; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableDeleteChecklistItem.class) @JsonDeserialize(as = ImmutableDeleteChecklistItem.class)
  interface DeleteChecklistItem extends TaskUpdateCommand {
    String getChecklistId();
    String getChecklistItemId();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.DeleteChecklistItem; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeChecklistItemAssignees.class) @JsonDeserialize(as = ImmutableChangeChecklistItemAssignees.class)
  interface ChangeChecklistItemAssignees extends TaskUpdateCommand {
    String getChecklistId();
    String getChecklistItemId();
    List<String> getAssigneeIds();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeChecklistItemAssignees; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangeChecklistItemCompleted.class) @JsonDeserialize(as = ImmutableChangeChecklistItemCompleted.class)
  interface ChangeChecklistItemCompleted extends TaskUpdateCommand {
    String getChecklistId();
    String getChecklistItemId();
    Boolean getCompleted();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeChecklistItemCompleted; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeChecklistItemDueDate.class) @JsonDeserialize(as = ImmutableChangeChecklistItemDueDate.class)
  interface ChangeChecklistItemDueDate extends TaskUpdateCommand {
    String getChecklistId();
    String getChecklistItemId();
    @Nullable LocalDate getDueDate();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeChecklistItemDueDate; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangeChecklistItemTitle.class) @JsonDeserialize(as = ImmutableChangeChecklistItemTitle.class)
  interface ChangeChecklistItemTitle extends TaskUpdateCommand {
    String getChecklistId();
    String getChecklistItemId();
    String getTitle();
    @Value.Default
    @Override default TaskCommandType getCommandType() { return TaskCommandType.ChangeChecklistItemTitle; }
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
