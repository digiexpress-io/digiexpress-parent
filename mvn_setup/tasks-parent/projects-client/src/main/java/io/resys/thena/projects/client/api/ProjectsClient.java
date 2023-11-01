package io.resys.thena.projects.client.api;

import io.resys.thena.projects.client.api.actions.ProjectsActions;
import io.resys.thena.projects.client.api.actions.RepositoryActions;

public interface ProjectsClient {
  ProjectsActions projects();
  RepositoryActions repo();
}
