package io.resys.thena.tasks.client.spi.visitors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.tasks.client.api.model.ImmutableChecklist;
import io.resys.thena.tasks.client.api.model.ImmutableChecklistItem;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task.Checklist;
import io.resys.thena.tasks.client.api.model.Task.ChecklistItem;
import io.resys.thena.tasks.client.api.model.Task.TaskItem;
import io.resys.thena.tasks.client.api.model.TaskCommand;

public class VisitorUtil {

  public static <T extends TaskItem> List<T> replaceItemInList(final List<T> currentItems, final T newItem) {
    return new ReplaceItemVisitor<T>().start(currentItems).replaceItem(newItem).end();
  }

  
  public static <T extends TaskItem> List<T> replaceItemInList(final List<T> currentItems, final String itemId, final Function<T, T> callback) {
    return new ReplaceItemVisitor<T>().start(currentItems).replaceItem(itemId, callback).end();
  }
  
  public static <T extends TaskItem> List<T> removeItemInList(final List<T> currentItems, final String itemId) {
    return new ReplaceItemVisitor<T>().start(currentItems).replaceItem(itemId, null).end();
  }
  

  public static LocalDateTime requireTargetDate(TaskCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new UpdateTaskVisitorException("targetDate not found");
    }
    return targetDate;
  }
  
  
  private static class ReplaceItemVisitor<T extends TaskItem> {
    private final List<T> newItems = new ArrayList<>();
    private Class<?> itemType;
    private List<T> currentItems;
    private boolean found = false;
    private String itemId;
    
    public ReplaceItemVisitor<T> start(final List<T> currentItems) {
      this.currentItems = currentItems;
      return this;
    }
    
    public ReplaceItemVisitor<T> replaceItem(final String itemId, @Nullable final Function<T, T> callback) {
      this.itemId = itemId;
      for (final T item : currentItems) {
        if(itemType == null) {
          itemType = item.getClass();
        }
        
        if (item.getId().equals(itemId)) {
          if(callback != null) {
            newItems.add(callback.apply(item));
          }
          found = true;
        } else {
          newItems.add(item);
        }
      }
      return this;
    }

    public ReplaceItemVisitor<T> replaceItem(final T next) {
      return this.replaceItem(next.getId(), (prev) -> next);
    }
    
    public List<T> end() {
      if (!found) {
        final var msg = String.format("%s with id %s not found", itemType, itemId);
        throw new UpdateTaskVisitorException(msg);
      }
      return newItems;
    }
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
  
  
  public static class ChecklistMutator {
    private final ImmutableTask current;
    private List<Checklist> next;
    
    public ChecklistMutator(ImmutableTask current, TaskCommand command) {
      super();
      this.next = new ArrayList<>(current.getChecklist());
      this.current = current.withUpdated(requireTargetDate(command));
    }

    public ChecklistMutator start() {
      return this;
    }
    
    public ChecklistMutator add(Checklist checklist) {
      next.add(checklist);
      return this;
    }

    public ChecklistMutator add(String checklistId, ChecklistItem item) {
      this.change(checklistId, (ImmutableChecklist.Builder next) -> next.addItems(item).build());
      return this;
    }
    
    public ChecklistMutator delete(String checklistId) {
      this.next = removeItemInList(this.next, checklistId);
      return this;
    }

    public ChecklistMutator delete(String checklistId, String checklistItemId) {
      this.next = replaceItemInList(
          this.next, checklistId, 
          (previousChecklist) -> {
            final var items = removeItemInList(previousChecklist.getItems(), checklistItemId);
            return ImmutableChecklist.builder().from(previousChecklist).items(items).build();
          });
      return this;
    }
    
    public ChecklistMutator change(String checklistId, Function<ImmutableChecklist.Builder, ImmutableChecklist> mutator) {
      this.next = replaceItemInList(
          this.next, checklistId, 
          (previousChecklist) -> mutator.apply(ImmutableChecklist.builder().from(previousChecklist)));
      return this;
    }

    public ChecklistMutator change(String checklistId, String checklistItemId, Function<ImmutableChecklistItem.Builder, ImmutableChecklistItem> mutator) {      
      this.next = replaceItemInList(
          this.next, checklistId, 
          (previousChecklist) -> {

            final var items = replaceItemInList(
                previousChecklist.getItems(), checklistItemId, 
                (previousItem) -> mutator.apply(ImmutableChecklistItem.builder().from(previousItem)));
            
            return ImmutableChecklist.builder().from(previousChecklist).items(items).build();
          });
      return this;
    }
    
    public ImmutableTask end() {
      return this.current.withChecklist(next);
    }
  }
}
