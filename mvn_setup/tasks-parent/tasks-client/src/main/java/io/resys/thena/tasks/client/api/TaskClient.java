package io.resys.thena.tasks.client.api;

import io.resys.thena.tasks.client.api.actions.RepositoryActions;
import io.resys.thena.tasks.client.api.actions.TaskActions;


public interface TaskClient {
  TaskActions tasks();
  RepositoryActions repo();
  TaskClient withRepoId(String repoId);
}
