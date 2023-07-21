package io.resys.thena.tasks.client.spi.visitors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.TaskItem;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskParent;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskExtension;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTaskExtension;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskInfo;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskDueDate;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStartDate;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskRoles;
import io.resys.thena.tasks.client.api.model.TaskCommand.CommentOnTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskComment;
import io.resys.thena.tasks.client.api.model.TaskCommand.ArchiveTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.AssignTaskReporter;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskPriority;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeTaskStatus;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;

public class TaskCommandVisitor {
  private final DocumentConfig ctx;
  private final Task start;
  private final List<TaskCommand> visitedCommands = new ArrayList<>();
  private ImmutableTask current;
  
  public TaskCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public TaskCommandVisitor(Task start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableTask.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public Task visitTransaction(List<? extends TaskCommand> commands) {
    commands.forEach(this::visitCommand);
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableTaskTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
  }
  
  private Task visitCommand(TaskCommand command) {    
    visitedCommands.add(command);
    switch (command.getCommandType()) {
      case AssignTaskParent:
        return visitAssignTaskParent((AssignTaskParent) command);
      case ChangeTaskExtension:
        return visitChangeTaskExtension((ChangeTaskExtension) command);
      case CreateTaskExtension:
        return visitCreateTaskExtension((CreateTaskExtension) command);
      case ChangeTaskInfo:
        return visitChangeTaskInfo((ChangeTaskInfo) command);
      case ChangeTaskDueDate:
        return visitChangeTaskDueDate((ChangeTaskDueDate) command);
      case ChangeTaskStartDate:
        return visitChangeTaskStartDate((ChangeTaskStartDate) command);
      case AssignTask:
        return visitAssignTask((AssignTask) command);
      case AssignTaskRoles:
        return visitAssignTaskRoles((AssignTaskRoles) command);
      case ChangeTaskComment:
        return visitChangeTaskComment((ChangeTaskComment) command);
      case CommentOnTask:
        return visitCommentOnTask((CommentOnTask) command);
      case ArchiveTask:
        return visitArchiveTask((ArchiveTask) command);
      case AssignTaskReporter:
        return visitAssignTaskReporter((AssignTaskReporter) command);
      case ChangeTaskPriority:
        return visitChangeTaskPriority((ChangeTaskPriority) command);
      case ChangeTaskStatus:
        return visitChangeTaskStatus((ChangeTaskStatus) command);
      case CreateTask:
        return visitCreateTask((CreateTask)command);
    }
    throw new UpdateTaskVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }
  
  private Task visitCreateTask(CreateTask command) {
    final var gen = ctx.getGid();
    final var targetDate = requireTargetDate(command.getTargetDate());
    this.current = ImmutableTask.builder()
        .id(gen.getNextId(DocumentType.TASK))
        .version(gen.getNextVersion(DocumentType.TASK))
        .addAllAssigneeIds(command.getAssigneeIds().stream().distinct().toList())
        .addAllRoles(command.getRoles().stream().distinct().toList())
        .reporterId(command.getReporterId())
        .labels(command.getLabels().stream().distinct().toList())
        .extensions(command.getExtensions())
        .comments(command.getComments())
        .title(command.getTitle())
        .description(command.getDescription())
        .priority(command.getPriority())
        .dueDate(command.getDueDate())
        .created(targetDate)
        .updated(targetDate)
        .status(command.getStatus() == null ? Status.CREATED : command.getStatus())
        .addTransactions(ImmutableTaskTransaction.builder().id(String.valueOf(1)).addCommands(command).build())
        .build();
    return this.current;
  }
  
  private Task visitChangeTaskStatus(ChangeTaskStatus command) {
    this.current = this.current
        .withStatus(command.getStatus())
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }
  
  private Task visitChangeTaskPriority(ChangeTaskPriority command) {
    this.current = this.current
        .withPriority(command.getPriority())
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }
    
  private Task visitAssignTaskReporter(AssignTaskReporter command) {
    this.current = this.current
        .withReporterId(command.getReporterId())
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitArchiveTask(ArchiveTask command) {
    final var targetDate = requireTargetDate(command.getTargetDate());
    this.current = this.current
        .withArchived(targetDate)
        .withUpdated(targetDate);
    return this.current;
  }

  private Task visitCommentOnTask(CommentOnTask command) {
    final var comments = new ArrayList<>(current.getComments());
    final var id = ctx.getGid().getNextId(DocumentType.TASK);
    comments.add(ImmutableTaskComment.builder()
        .id(id)
        .commentText(command.getCommentText())
        .replyToId(command.getReplyToCommentId())
        .username(command.getUserId())
        .created(requireTargetDate(command.getTargetDate()))
        .build());
    this.current = this.current
        .withComments(comments)
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitChangeTaskComment(ChangeTaskComment command) {
    final var id = command.getCommentId();
    final var newComment = ImmutableTaskComment.builder()
        .id(id)
        .commentText(command.getCommentText())
        .replyToId(command.getReplyToCommentId())
        .username(command.getUserId())
        .created(requireTargetDate(command.getTargetDate()))
        .build();
    final var newCommentList = replaceItemInList(current.getComments(), newComment);
    this.current = this.current
        .withComments(newCommentList)
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitAssignTaskRoles(AssignTaskRoles command) {
    this.current = this.current
        .withRoles(command.getRoles().stream().distinct().sorted().toList())
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitAssignTask(AssignTask command) {
    this.current = this.current
        .withAssigneeIds(command.getAssigneeIds().stream().distinct().sorted().toList())
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitChangeTaskStartDate(ChangeTaskStartDate command) {
    this.current = this.current
        .withStartDate(command.getStartDate().orElse(null))
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitChangeTaskDueDate(ChangeTaskDueDate command) {
    this.current = this.current
        .withDueDate(command.getDueDate().orElse(null))
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitChangeTaskInfo(ChangeTaskInfo command) {
    this.current = this.current
        .withTitle(command.getTitle())
        .withDescription(command.getDescription())
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitCreateTaskExtension(CreateTaskExtension command) {
    final var extensions = new ArrayList<>(current.getExtensions());
    final var id = ctx.getGid().getNextId(DocumentType.TASK);
    extensions.add(ImmutableTaskExtension.builder()
        .id(id)
        .name(command.getName())
        .type(command.getType())
        .body(command.getBody())
        .build());
    this.current = this.current
        .withExtensions(extensions)
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private Task visitChangeTaskExtension(ChangeTaskExtension command) {
    final var id = command.getId();
    final var newExtension = ImmutableTaskExtension.builder()
        .id(id)
        .name(command.getName())
        .type(command.getType())
        .body(command.getBody())
        .build();
    final var newExtensionList = replaceItemInList(current.getExtensions(), newExtension);
    this.current = this.current
        .withExtensions(newExtensionList)
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private <T extends TaskItem> List<T> replaceItemInList(final List<T> currentItems, final T newItem) {
    final var newItems = new ArrayList<T>();
    boolean found = false;
    for (final var item : currentItems) {
      if (item.getId().equals(newItem.getId())) {
        newItems.add(newItem);
        found = true;
      } else {
        newItems.add(item);
      }
    }
    if (!found) {
      final var msg = String.format("%s with id %s not found", newItem.getClass(), newItem.getId());
      throw new UpdateTaskVisitorException(msg);
    }
    return newItems;
  }

  private Task visitAssignTaskParent(AssignTaskParent command) {
    this.current = this.current
        .withParentId(command.getParentId())
        .withUpdated(requireTargetDate(command.getTargetDate()));
    return this.current;
  }

  private LocalDateTime requireTargetDate(LocalDateTime targetDate) {
    if (targetDate == null) {
      throw new UpdateTaskVisitorException("targetDate not found");
    }
    return targetDate;
  }
  
  public static class UpdateTaskVisitorException extends RuntimeException {

    private static final long serialVersionUID = -1385190644836838881L;

    public UpdateTaskVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public UpdateTaskVisitorException(String message) {
      super(message);
    }
  }
}
