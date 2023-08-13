package io.resys.thena.tasks.client.spi.visitors;

import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.resys.thena.tasks.client.api.model.ImmutableChecklist;
import io.resys.thena.tasks.client.api.model.ImmutableChecklistItem;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.TaskCommand;
import io.resys.thena.tasks.client.api.model.TaskCommand.AddChecklistItem;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemAssignees;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemCompleted;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemDueDate;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistItemTitle;
import io.resys.thena.tasks.client.api.model.TaskCommand.ChangeChecklistTitle;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateChecklist;
import io.resys.thena.tasks.client.api.model.TaskCommand.DeleteChecklist;
import io.resys.thena.tasks.client.api.model.TaskCommand.DeleteChecklistItem;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.visitors.VisitorUtil.ChecklistMutator;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TaskCommandChecklistVisitor {
  private final DocumentConfig ctx;
  private final ImmutableTask current;
  
  
  public ImmutableTask visitCreateChecklist(CreateChecklist command) { 
    final var gen = ctx.getGid();
    final var checklist = ImmutableChecklist.builder()
        .id(gen.getNextId(DocumentType.TASK))
        .title(command.getTitle())
        .items(command.getChecklist())
        .build();
    return this.mutator(command).add(checklist).end();
  }
  
  public ImmutableTask visitAddChecklistItem(AddChecklistItem command) {
    final var gen = ctx.getGid();
    final var item = ImmutableChecklistItem.builder()
      .id(gen.getNextId(DocumentType.TASK))
      .assigneeIds(command.getAssigneeIds())
      .dueDate(command.getDueDate())
      .completed(command.getCompleted())
      .title(command.getTitle())
      .build();
    return this.mutator(command).add(command.getChecklistId(), item).end();
  }

  public ImmutableTask visitChangeChecklistItemAssignees(ChangeChecklistItemAssignees command) { 
    return this.mutator(command).change(
        command.getChecklistId(), command.getChecklistItemId(), 
        (builder) -> builder.assigneeIds(command.getAssigneeIds()).build()).end();
  }

  public ImmutableTask visitChangeChecklistItemCompleted(ChangeChecklistItemCompleted command) { 
    return this.mutator(command).change(
        command.getChecklistId(), command.getChecklistItemId(), 
        (builder) -> builder.completed(command.getCompleted()).build()).end();
  }

  public ImmutableTask visitChangeChecklistItemDueDate(ChangeChecklistItemDueDate command) { 
    return this.mutator(command).change(
        command.getChecklistId(), command.getChecklistItemId(), 
        (builder) -> builder.dueDate(command.getDueDate()).build()).end();
  }

  public ImmutableTask visitChangeChecklistItemTitle(ChangeChecklistItemTitle command) {
    return this.mutator(command).change(
      command.getChecklistId(), command.getChecklistItemId(),
      (builder) -> builder.title(command.getTitle()).build()).end();
  }

  public ImmutableTask visitChangeChecklistTitle(ChangeChecklistTitle command) { 
    return this.mutator(command).change(
        command.getChecklistId(), 
        (builder) -> builder.title(command.getTitle()).build()).end();

  }

  public ImmutableTask visitDeleteChecklist(DeleteChecklist command) { 
    return this.mutator(command).delete(
        command.getChecklistId())
       .end();


  }

  public ImmutableTask visitDeleteChecklistItem(DeleteChecklistItem command) { 
    return this.mutator(command).delete(
        command.getChecklistId(), command.getChecklistItemId())
       .end();

  }
 
  private ChecklistMutator mutator(TaskCommand command) {
    return new ChecklistMutator(current, command).start();
  }

}
