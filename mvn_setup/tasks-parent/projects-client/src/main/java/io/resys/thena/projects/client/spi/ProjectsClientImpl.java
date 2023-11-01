package io.resys.thena.projects.client.spi;

import io.resys.thena.projects.client.api.ProjectsClient;
import io.resys.thena.projects.client.api.actions.ProjectsActions;
import io.resys.thena.projects.client.api.actions.RepositoryActions;
import io.resys.thena.projects.client.spi.actions.ProjectsActionsImpl;
import io.resys.thena.projects.client.spi.actions.RepositoryActionsImpl;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectsClientImpl implements ProjectsClient {
  private final DocumentStore ctx;
  
  @Override
  public ProjectsActions projects() {
    return new ProjectsActionsImpl(ctx);
  }
  @Override
  public RepositoryActions repo() {
    return new RepositoryActionsImpl(ctx);
  }
  public DocumentStore getCtx() {
    return ctx;
  }
}
