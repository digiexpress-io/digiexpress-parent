package io.resys.thena.projects.client.api.actions;

import io.resys.thena.projects.client.api.ProjectsClient;
import io.smallrye.mutiny.Uni;

public interface RepositoryQuery {
  RepositoryQuery repoName(String repoName);
  RepositoryQuery headName(String headName);
  ProjectsClient build();

  Uni<ProjectsClient> deleteAll();
  Uni<ProjectsClient> delete();
  Uni<ProjectsClient> create();
  Uni<ProjectsClient> createIfNot();
} 
