package io.resys.thena.tasks.client.thenamission.visitors;

import java.time.ZoneOffset;
import java.util.ArrayList;

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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.actions.GrimCommitActions.ModifyManyMissions;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.ChecklistItem;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.AddChecklistItem;
import io.resys.thena.tasks.client.api.model.TaskCommand.ArchiveTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskParent;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskReporter;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskRoles;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemAssignees;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemCompleted;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemDueDate;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemTitle;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistTitle;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskComment;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskDueDate;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskExtension;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskInfo;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskPriority;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStartDate;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStatus;
import io.resys.thena.tasks.client.api.model.TaskCommand.CommentOnTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateChecklist;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTaskExtension;
import io.resys.thena.tasks.client.api.model.TaskCommand.DeleteChecklist;
import io.resys.thena.tasks.client.api.model.TaskCommand.DeleteChecklistItem;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.thenagit.store.DocumentStoreException;
import io.resys.thena.tasks.client.thenagit.visitors.VisitorUtil.UpdateTaskVisitorException;
import io.resys.thena.tasks.client.thenamission.TaskStore;
import io.resys.thena.tasks.client.thenamission.TaskStoreConfig.MergeTasksVisitor;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateTasksVisitor implements MergeTasksVisitor<List<Task>> {
  private final TaskStore ctx;
  private final List<String> taskIds;
  private final Map<String, List<TaskUpdateCommand>> commandsByTaskId; 
  
  
  public UpdateTasksVisitor(List<TaskUpdateCommand> commands, TaskStore ctx) {
    super();
    this.ctx = ctx;
    this.commandsByTaskId = commands.stream()
        .collect(Collectors.groupingBy(TaskUpdateCommand::getTaskId));
    this.taskIds = new ArrayList<>(commandsByTaskId.keySet());
  }

  @Override
  public ModifyManyMissions start(GrimStructuredTenant config, ModifyManyMissions builder) {
    final var result = builder.commitMessage("merging tasks from: " + UpdateTasksVisitor.class.getSimpleName());
    this.commandsByTaskId.forEach((id, commands) -> builder.modifyMission(id, modify -> modifyOneTask(commands, modify)));
    return result;
  }
  
  private void modifyOneTask(List<TaskUpdateCommand> commands, MergeMission merge) {
    merge.addCommands(commands.stream().map(JsonObject::mapFrom).toList());
    commands.forEach(c -> visitCommand(c, merge));
    merge.build();
  }
 
  private UpdateTasksVisitor visitCommand(TaskCommand command, MergeMission merge) {
    switch (command.getCommandType()) {
      case AssignTaskParent:
        return visitAssignTaskParent((AssignTaskParent) command, merge);
      case ChangeTaskExtension:
        return visitChangeTaskExtension((ChangeTaskExtension) command, merge);
      case CreateTaskExtension:
        return visitCreateTaskExtension((CreateTaskExtension) command, merge);
      case ChangeTaskInfo:
        return visitChangeTaskInfo((ChangeTaskInfo) command, merge);
      case ChangeTaskDueDate:
        return visitChangeTaskDueDate((ChangeTaskDueDate) command, merge);
      case ChangeTaskStartDate:
        return visitChangeTaskStartDate((ChangeTaskStartDate) command, merge);
      case AssignTask:
        return visitAssignTask((AssignTask) command, merge);
      case AssignTaskRoles:
        return visitAssignTaskRoles((AssignTaskRoles) command, merge);
      case ChangeTaskComment:
        return visitChangeTaskComment((ChangeTaskComment) command, merge);
      case CommentOnTask:
        return visitCommentOnTask((CommentOnTask) command, merge);
      case ArchiveTask:
        return visitArchiveTask((ArchiveTask) command, merge);
      case AssignTaskReporter:
        return visitAssignTaskReporter((AssignTaskReporter) command, merge);
      case ChangeTaskPriority:
        return visitChangeTaskPriority((ChangeTaskPriority) command, merge);
      case ChangeTaskStatus:
        return visitChangeTaskStatus((ChangeTaskStatus) command, merge);
      case AddChecklistItem:
        return visitAddChecklistItem((AddChecklistItem)command, merge);
      case ChangeChecklistItemAssignees:
        return visitChangeChecklistItemAssignees((ChangeChecklistItemAssignees)command, merge);
      case ChangeChecklistItemCompleted:
        return visitChangeChecklistItemCompleted((ChangeChecklistItemCompleted)command, merge);
      case ChangeChecklistItemDueDate:
        return visitChangeChecklistItemDueDate((ChangeChecklistItemDueDate)command, merge);
      case ChangeChecklistItemTitle:
        return visitChangeChecklistItemTitle((ChangeChecklistItemTitle)command, merge);
      case ChangeChecklistTitle:
        return visitChangeChecklistTitle((ChangeChecklistTitle)command, merge);
      case CreateChecklist:
        return visitCreateChecklist((CreateChecklist)command, merge);
      case DeleteChecklist:
        return visitDeleteChecklist((DeleteChecklist)command, merge);
      case DeleteChecklistItem:
        return visitDeleteChecklistItem((DeleteChecklistItem)command, merge);
    
      default: throw new UpdateTaskVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
    }
    
  }
  
  private UpdateTasksVisitor visitChangeTaskStatus(ChangeTaskStatus command, MergeMission merge) {
    merge.status(command.getStatus().toString());
    return this;
  }
  
  private UpdateTasksVisitor visitChangeTaskPriority(ChangeTaskPriority command, MergeMission merge) {
    merge.priority(command.getPriority().toString());
    return this;
  }
    
  private UpdateTasksVisitor visitAssignTaskReporter(AssignTaskReporter command, MergeMission merge) {
    merge.reporterId(command.getReporterId());
    return this;
  }

  private UpdateTasksVisitor visitArchiveTask(ArchiveTask command, MergeMission merge) {
    merge.archivedAt(command.getTargetDate().atOffset(ZoneOffset.UTC));
    return this;
  }

  private UpdateTasksVisitor visitCommentOnTask(CommentOnTask command, MergeMission merge) {
    merge.addRemark(newRemark -> newRemark 
      .remarkText(command.getCommentText())
      .parentId(command.getReplyToCommentId())
      .reporterId(command.getUserId())
      .build()
    );
    return this;
  }

  private UpdateTasksVisitor visitChangeTaskComment(ChangeTaskComment command, MergeMission merge) {
    merge.modifyRemark(command.getCommentId(), modifyRemark -> 
      modifyRemark 
        .remarkText(command.getCommentText())
        .parentId(command.getReplyToCommentId())
        .reporterId(command.getUserId())
        .build()
    );
    return this;
  }

  private UpdateTasksVisitor visitAssignTaskRoles(AssignTaskRoles command, MergeMission merge) {
    merge.setAllAssignees(
        CreateTasksVisitor.ASSIGNMENT_TYPE_TASK_ROLE,
        command.getRoles().stream().distinct().sorted().toList(), 
        roleId -> (newAssignee -> newAssignee.assignee(roleId).assignmentType(CreateTasksVisitor.ASSIGNMENT_TYPE_TASK_ROLE).build()
    ));
    return this;
  }

  private UpdateTasksVisitor visitAssignTask(AssignTask command, MergeMission merge) {
    
    merge.setAllAssignees(
        CreateTasksVisitor.ASSIGNMENT_TYPE_TASK_USER,
        command.getAssigneeIds().stream().distinct().sorted().toList(), 
        roleId -> (newAssignee -> newAssignee.assignee(roleId).assignmentType(CreateTasksVisitor.ASSIGNMENT_TYPE_TASK_USER).build()
    ));
    return this;
  }

  private UpdateTasksVisitor visitChangeTaskStartDate(ChangeTaskStartDate command, MergeMission merge) {
    merge.startDate(command.getStartDate().orElse(null));
    return this;
  }

  private UpdateTasksVisitor visitChangeTaskDueDate(ChangeTaskDueDate command, MergeMission merge) {
    merge.dueDate(command.getDueDate().orElse(null));    
    return this;
  }

  private UpdateTasksVisitor visitChangeTaskInfo(ChangeTaskInfo command, MergeMission merge) {
    merge.title(command.getTitle()).description(command.getDescription());
    return this;
  }

  private UpdateTasksVisitor visitCreateTaskExtension(CreateTaskExtension extension, MergeMission merge) {
    merge.addLink(newLink -> 
      newLink
      .linkType(CreateTasksVisitor.LINK_TYPE_TASK_EXTENSION)
      .linkValue(extension.getName())
      .linkBody(JsonObject.of(
          CreateTasksVisitor.LINK_TYPE_TASK_EXTENSION_TYPE, extension.getType(),
          CreateTasksVisitor.LINK_TYPE_TASK_EXTENSION_BODY, extension.getBody()
       ))
      .build()
    );
    return this;
  }

  private UpdateTasksVisitor visitChangeTaskExtension(ChangeTaskExtension extension, MergeMission merge) {
  
    merge.modifyLink(extension.getId(), newLink -> 
      newLink
      .linkType(CreateTasksVisitor.LINK_TYPE_TASK_EXTENSION)
      .linkValue(extension.getName())
      .linkBody(JsonObject.of(
          CreateTasksVisitor.LINK_TYPE_TASK_EXTENSION_TYPE, extension.getType(),
          CreateTasksVisitor.LINK_TYPE_TASK_EXTENSION_BODY, extension.getBody()
       ))
      .build()
    );
    return this;
  }

  private UpdateTasksVisitor visitAssignTaskParent(AssignTaskParent command, MergeMission merge) {
    merge.parentId(command.getParentId());
    return this;
  }

  public UpdateTasksVisitor visitCreateChecklist(CreateChecklist command, MergeMission merge) { 
    command.getChecklist().forEach(checklist -> merge.addObjective(newObjective -> {
      command.getChecklist().forEach(checklistItem -> newObjective.addGoal(newGoal -> createGoal(checklistItem, newGoal)));
      newObjective.title(checklist.getTitle()).build();
    }));
   
    return this;
  }
  
  public UpdateTasksVisitor visitAddChecklistItem(AddChecklistItem command, MergeMission merge) {    
    merge.modifyObjective(command.getChecklistId(), modifyObjective -> modifyObjective.addGoal(newGoal -> {

      command.getAssigneeIds().forEach(assigneeId -> newGoal.addAssignees(newAssignee ->
        newAssignee.assignee(assigneeId).assignmentType(CreateTasksVisitor.ASSIGNMENT_TYPE_GOAL_USER).build()
      ));

      newGoal
      .dueDate(command.getDueDate())
      .title(command.getTitle())
      .status(command.getCompleted().toString())
      .build();
      
    }));
    return this;
  }

  public UpdateTasksVisitor visitChangeChecklistItemAssignees(ChangeChecklistItemAssignees command, MergeMission merge) {
    
    merge.modifyGoal(command.getChecklistItemId(), mergeGoal -> {

      mergeGoal.setAllAssignees(
          CreateTasksVisitor.ASSIGNMENT_TYPE_GOAL_USER,
          command.getAssigneeIds().stream().distinct().sorted().toList(), 
          roleId -> (newAssignee -> newAssignee.assignee(roleId).assignmentType(CreateTasksVisitor.ASSIGNMENT_TYPE_GOAL_USER).build()
      ));
      
      mergeGoal.build();
    });
    return this;
  }

  public UpdateTasksVisitor visitChangeChecklistItemCompleted(ChangeChecklistItemCompleted command, MergeMission merge) { 
    merge.modifyGoal(command.getChecklistItemId(), mergeGoal -> 
      mergeGoal.status(command.getCompleted().toString()).build()
    );
    return this;
  }

  public UpdateTasksVisitor visitChangeChecklistItemDueDate(ChangeChecklistItemDueDate command, MergeMission merge) { 
    merge.modifyGoal(command.getChecklistItemId(), mergeGoal -> 
      mergeGoal.dueDate(command.getDueDate()).build()
    );
    return this;
  }

  public UpdateTasksVisitor visitChangeChecklistItemTitle(ChangeChecklistItemTitle command, MergeMission merge) {
    merge.modifyGoal(command.getChecklistItemId(), mergeGoal -> 
      mergeGoal.title(command.getTitle()).build()
    );
    return this;
  }

  public UpdateTasksVisitor visitChangeChecklistTitle(ChangeChecklistTitle command, MergeMission merge) { 
    merge.modifyObjective(command.getChecklistId(), mergeGoal -> 
      mergeGoal.title(command.getTitle()).build()
    );
    return this;

  }

  public UpdateTasksVisitor visitDeleteChecklist(DeleteChecklist command, MergeMission merge) { 
    merge.removeObjective(command.getChecklistId());
    return this;
  }

  public UpdateTasksVisitor visitDeleteChecklistItem(DeleteChecklistItem command, MergeMission merge) { 
    merge.removeObjective(command.getChecklistItemId());
    return this;
  }
  
  private void createGoal(ChecklistItem item, NewGoal newGoal) {
    item.getAssigneeIds().forEach(assigneeId -> newGoal.addAssignees(newAssignee ->
      newAssignee.assignee(assigneeId).assignmentType(CreateTasksVisitor.ASSIGNMENT_TYPE_GOAL_USER).build()
    ));
  
    newGoal
    .dueDate(item.getDueDate())
    .title(item.getTitle())
    .status(item.getCompleted().toString())
    .build();
  }
  

  @Override
  public List<GrimMission> visitEnvelope(GrimStructuredTenant config, ManyMissionsEnvelope envelope) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw DocumentStoreException.builder("TASKS_UPDATE_FAIL").add(config, envelope).build();
    }
    return envelope.getMissions();
  }

  @Override
  public Uni<List<Task>> end(GrimStructuredTenant config, List<GrimMission> commit) {
    return ctx.getConfig().accept(new FindAllTasksByIdVisitor(taskIds));
  }

}
