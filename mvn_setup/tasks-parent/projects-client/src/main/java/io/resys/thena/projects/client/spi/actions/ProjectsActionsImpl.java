package io.resys.thena.projects.client.spi.actions;

import io.resys.thena.projects.client.api.actions.ProjectsActions;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ProjectsActionsImpl implements ProjectsActions {
  private final DocumentStore ctx;

  @Override
  public CreateProjectAction createProject(){
    return new CreateProjectImpl(ctx);
  }

  @Override
  public UpdateProjectAction updateProject() {
    return new UpdateProjectImpl(ctx);
  }

  @Override
  public ActiveProjectsQuery queryActiveProjects() {
    return new ActiveProjectsQueryImpl(ctx);
  }
}
