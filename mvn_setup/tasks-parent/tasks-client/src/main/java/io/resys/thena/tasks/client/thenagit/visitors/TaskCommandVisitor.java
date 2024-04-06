package io.resys.thena.tasks.client.thenagit.visitors;

import static io.resys.thena.tasks.client.thenagit.visitors.VisitorUtil.replaceItemInList;
import static io.resys.thena.tasks.client.thenagit.visitors.VisitorUtil.requireTargetDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Status;
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
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTaskExtension;
import io.resys.thena.tasks.client.api.model.TaskCommand.DeleteChecklist;
import io.resys.thena.tasks.client.api.model.TaskCommand.DeleteChecklistItem;
import io.resys.thena.tasks.client.thenagit.store.DocumentConfig;
import io.resys.thena.tasks.client.thenagit.visitors.VisitorUtil.UpdateTaskVisitorException;


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
      case AddChecklistItem:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitAddChecklistItem((AddChecklistItem)command);
        return this.current;
      case ChangeChecklistItemAssignees:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitChangeChecklistItemAssignees((ChangeChecklistItemAssignees)command);
        return this.current;
      case ChangeChecklistItemCompleted:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitChangeChecklistItemCompleted((ChangeChecklistItemCompleted)command);
        return this.current;
      case ChangeChecklistItemDueDate:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitChangeChecklistItemDueDate((ChangeChecklistItemDueDate)command);
        return this.current;
      case ChangeChecklistItemTitle:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitChangeChecklistItemTitle((ChangeChecklistItemTitle)command);
        return this.current;
      case ChangeChecklistTitle:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitChangeChecklistTitle((ChangeChecklistTitle)command);
        return this.current;
      case CreateChecklist:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitCreateChecklist((CreateChecklist)command);
        return this.current;
      case DeleteChecklist:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitDeleteChecklist((DeleteChecklist)command);
        return this.current;
      case DeleteChecklistItem:
        this.current = new TaskCommandChecklistVisitor(ctx, current).visitDeleteChecklistItem((DeleteChecklistItem)command);
        return this.current;
    }
    throw new UpdateTaskVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }
  
  
  private Task visitCreateTask(CreateTask command) {
    final var gen = ctx.getGid();
    final var targetDate = requireTargetDate(command);
    this.current = ImmutableTask.builder()
        .id(gen.getNextId(DocumentType.TASK))
        .version(gen.getNextVersion(DocumentType.TASK))
        .addAllAssigneeIds(command.getAssigneeIds().stream().distinct().toList())
        .addAllRoles(command.getRoles().stream().distinct().toList())
        .reporterId(command.getReporterId())
        .labels(command.getLabels().stream().distinct().toList())
        .extensions(command.getExtensions())
        .comments(command.getComments())
        .checklist(command.getChecklist())
        .title(command.getTitle())
        .description(command.getDescription())
        .priority(command.getPriority())
        .dueDate(command.getDueDate())
        .startDate(command.getStartDate())
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
        .withUpdated(requireTargetDate(command));
    return this.current;
  }
  
  private Task visitChangeTaskPriority(ChangeTaskPriority command) {
    this.current = this.current
        .withPriority(command.getPriority())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }
    
  private Task visitAssignTaskReporter(AssignTaskReporter command) {
    this.current = this.current
        .withReporterId(command.getReporterId())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitArchiveTask(ArchiveTask command) {
    final var targetDate = requireTargetDate(command);
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
        .created(requireTargetDate(command))
        .build());
    this.current = this.current
        .withComments(comments)
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitChangeTaskComment(ChangeTaskComment command) {
    final var id = command.getCommentId();
    final var newComment = ImmutableTaskComment.builder()
        .id(id)
        .commentText(command.getCommentText())
        .replyToId(command.getReplyToCommentId())
        .username(command.getUserId())
        .created(requireTargetDate(command))
        .build();
    final var newCommentList = replaceItemInList(current.getComments(), newComment);
    this.current = this.current
        .withComments(newCommentList)
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitAssignTaskRoles(AssignTaskRoles command) {
    this.current = this.current
        .withRoles(command.getRoles().stream().distinct().sorted().toList())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitAssignTask(AssignTask command) {
    this.current = this.current
        .withAssigneeIds(command.getAssigneeIds().stream().distinct().sorted().toList())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitChangeTaskStartDate(ChangeTaskStartDate command) {
    this.current = this.current
        .withStartDate(command.getStartDate().orElse(null))
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitChangeTaskDueDate(ChangeTaskDueDate command) {
    this.current = this.current
        .withDueDate(command.getDueDate().orElse(null))
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitChangeTaskInfo(ChangeTaskInfo command) {
    this.current = this.current
        .withTitle(command.getTitle())
        .withDescription(command.getDescription())
        .withUpdated(requireTargetDate(command));
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
        .created(requireTargetDate(command))
        .updated(requireTargetDate(command))
        .build());
    this.current = this.current
        .withExtensions(extensions)
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitChangeTaskExtension(ChangeTaskExtension command) {
    final var id = command.getId();
    final var oldExtension = current.getExtensions().stream()
        .filter(e -> e.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> new UpdateTaskVisitorException(String.format("Extension with id: %s not found!", id)));
    final var newExtension = ImmutableTaskExtension.builder()
        .id(id)
        .name(command.getName())
        .type(command.getType())
        .body(command.getBody())
        .created(oldExtension.getCreated())
        .updated(requireTargetDate(command))
        .build();
    final var newExtensionList = replaceItemInList(current.getExtensions(), newExtension);
    this.current = this.current
        .withExtensions(newExtensionList)
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Task visitAssignTaskParent(AssignTaskParent command) {
    this.current = this.current
        .withParentId(command.getParentId())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

}
