package io.resys.thena.tasks.client.thenamission.support;

import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.Task;


public class EvaluateTaskAccess {
  private final TaskAccessEvaluator access;
  
  private EvaluateTaskAccess(TaskAccessEvaluator access) {
    super();
    this.access = access;
  }
  
  public static EvaluateTaskAccess of(TaskAccessEvaluator access) {
    return new EvaluateTaskAccess(access);
  }
  
  public Task isReadAccessGranted(Task task) {
    return task;
  }
  public void isCreateAccessGranted(Task task) {
    
  }  
  public void isUpdatedAccessGranted(Task task) {
    
  }
  
  public void isDeleteAccessGranted(Task task) {
    
  }
  
  public TaskAccessEvaluator getEvaluator() {
    return this.access;
  }
}
