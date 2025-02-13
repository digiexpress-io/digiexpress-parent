package io.resys.thena.tasks.client.api;

import io.resys.thena.tasks.client.api.actions.TaskTenantsActions;
import io.resys.thena.tasks.client.api.actions.TaskActions;


public interface TaskClient {
  TaskActions tasks();
  TaskTenantsActions tenants();
  TaskClient withRepoId(String repoId);
}
