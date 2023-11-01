package io.resys.thena.projects.client.tests.config;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.model.ImmutableProject;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.api.model.Project.RepoType;
import io.resys.thena.projects.client.api.model.ProjectCommand.CreateProject;
import io.resys.thena.projects.client.api.model.ProjectCommand.ProjectUpdateCommand;
import io.resys.thena.projects.client.rest.ProjectRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class ProjectResource implements ProjectRestApi {

  private final ImmutableProject mockProject = ImmutableProject.builder()
      .id("project1")
      .version("project-version1")
      .archived(ProjectTestCase.getTargetDate())
      .created(ProjectTestCase.getTargetDate())
      .updated(ProjectTestCase.getTargetDate())
      .title("project-title1")
      .description("Very good project indeed")
      .repoId("repo-1")
      .repoType(RepoType.TASKS)
      .build();

  @Override
  public Uni<List<Project>> findProjects() {
    return Uni.createFrom()
        .item(Arrays.asList(ImmutableProject.builder()
            .id("project1")
            .version("project-version1")
            .repoId("repo-1")
            .repoType(RepoType.DIALOB)
            .title("project-title")
            .description("project-desc")
            .created(Instant.now())
            .updated(Instant.now())
            .build()));
  }

  @Override
  public Uni<List<Project>> createProjects(List<CreateProject> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockProject).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<Project>> updateProjects(List<ProjectUpdateCommand> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockProject).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<Project>> deleteProjects(List<ProjectUpdateCommand> commands) {
    return Uni.createFrom().item(Arrays.asList(mockProject, mockProject));
  }

  @Override
  public Uni<Project> updateOneProject(String projectId, List<ProjectUpdateCommand> commands) {
    return Uni.createFrom().item(mockProject);
  }
}
