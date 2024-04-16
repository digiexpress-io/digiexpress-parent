package io.resys.thena.tasks.client.thenamission.support;

import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccess;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;


public class EvaluateTaskAccess {
  private final TaskAccessEvaluator access;
  
  private EvaluateTaskAccess(TaskAccessEvaluator access) {
    super();
    this.access = access == null ? new AllAccess() : access;
  }
  
  public static EvaluateTaskAccess of(TaskAccessEvaluator access) {
    return new EvaluateTaskAccess(access);
  }
  
  public Task isReadAccessGranted(Task task) {
    final var eval = this.access.getReadAccess(task);
    if(eval.isAccessGranted()) {
      return task;      
    }
    
    return ImmutableTask.builder()
        .assigneeIds(task.getAssigneeIds())
        .roles(task.getRoles())
        .labels(task.getLabels())
        .archived(task.getArchived())
        .created(task.getCreated())
        .updated(task.getUpdated())
        .status(task.getStatus())
        .priority(task.getPriority())
        .startDate(task.getStartDate())
        .dueDate(task.getDueDate())
        .version(task.getVersion())
        .treeVersion(task.getTreeVersion())
        .parentId(task.getParentId())
        .reporterId("")
        .id(task.getId())
        .title("### NO-ACCESS ###")
        .description("### NO-ACCESS ###")
        .build();

  }
  public void isCreateAccessGranted(Task task) {
    final var eval = this.access.getCreateAccess(task);
    if(!eval.isAccessGranted()) {
      throw new TaskAccessException(eval);
    }
  }  
  public void isUpdatedAccessGranted(Task task) {
    final var eval = this.access.getUpdatedAccess(task);
    if(!eval.isAccessGranted()) {
      throw new TaskAccessException(eval);
    } 
  }
  public void isDeleteAccessGranted(Task task) {
    final var eval = this.access.getDeleteAccess(task);
    if(!eval.isAccessGranted()) {
      throw new TaskAccessException(eval);
    }
  }
  
  public TaskAccessEvaluator getEvaluator() {
    return this.access;
  }
  
  private final static class AllAccess implements TaskAccessEvaluator {
    private final static AccessGranted result = new AccessGranted();
    @Override public AccessGranted getReadAccess(Task task)  { return result; }
    @Override public AccessGranted getCreateAccess(Task task)  { return result; }
    @Override public AccessGranted getUpdatedAccess(Task task)  { return result; }
    @Override public AccessGranted getDeleteAccess(Task task) { return result; }
  }
  
  private final static class AccessGranted implements TaskAccess {
    @Override public boolean isAccessGranted() { return true; }
    @Override public String getMessage() { return "all-access-always"; }
  }
}
