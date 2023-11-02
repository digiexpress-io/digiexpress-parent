package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.ProjectsClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateProject;
import io.resys.thena.projects.client.api.model.ProjectCommand.CreateProject;
import io.resys.thena.projects.client.api.model.ProjectCommand.ProjectUpdateCommand;
import io.resys.thena.projects.client.rest.ProjectRestApi;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentProject;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentUser;
import io.resys.thena.tasks.dev.app.DemoResource.HeadState;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("q/digiexpress/api")
public class DigiExpressProjects implements ProjectRestApi {

  @Inject ProjectsClient projectsClient;
  @Inject CurrentProject currentProject;
  @Inject CurrentUser currentUser;
  
  
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.Project>> findProjects() {
    return projectsClient.projects().queryActiveProjects().findAll();
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.Project>> createProjects(List<CreateProject> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> ImmutableCreateProject.builder().from(command)
            .targetDate(Instant.now())
            .userId(currentUser.getUserId())
            .build())
        .collect(Collectors.toList());
    return projectsClient.projects().createProject().createMany(modifiedCommands)
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()))
        .onItem().transformToUni(project -> {
            return projectsClient.repo().query()
                .repoName(currentProject.getProjectId())
                .headName(currentProject.getHead())
                .createIfNot()
                .onItem().transform(created ->  project);
        }).concatenate().collect().asList();
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.Project>> updateProjects(List<ProjectUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return projectsClient.projects().updateProject().updateMany(modifiedCommands);
  }
  @Override
  public Uni<List<io.resys.thena.projects.client.api.model.Project>> deleteProjects(List<ProjectUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return projectsClient.projects().updateProject().updateMany(modifiedCommands);
  }
  @Override
  public Uni<io.resys.thena.projects.client.api.model.Project> updateOneProject(String projectId, List<ProjectUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return projectsClient.projects().updateProject().updateOne(modifiedCommands);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("init")
  public Uni<HeadState> init() {
    return projectsClient.repo().query().repoName(currentProject.getProjectId()).headName(currentProject.getHead()).createIfNot()
        .onItem().transform(created -> HeadState.builder().created(true).build());
  }
}
