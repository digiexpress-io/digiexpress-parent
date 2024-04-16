package io.resys.thena.tasks.client.thenamission.support;

import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccess;

public class TaskAccessException extends RuntimeException {
  private static final long serialVersionUID = -2159506397630330731L;
  private final TaskAccess access;
  
  public TaskAccessException(TaskAccess access) {
    super(access.getMessage());
    this.access = access;
  }
  public TaskAccess getAccess() {
    return access;
  }
}
