package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Project;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.rest.DigiExpressRestApi;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentProject;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentUser;
import io.resys.thena.tasks.dev.app.DemoResource.HeadState;
import io.smallrye.mutiny.Uni;

@Path("q/digiexpress/api")
public class DigiExpressResource implements DigiExpressRestApi {

  @Inject TaskClient client;
  @Inject CurrentProject currentProject;
  @Inject CurrentUser currentUser;
  
  @Override
  public Uni<List<Project>> findProjects() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<List<Task>> findTasks(String projectId) {
    return client.tasks().queryActiveTasks().findAll();
  }
  @Override
  public Uni<List<Task>> createTasks(String projectId, List<CreateTask> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> ImmutableCreateTask.builder().from(command)
            .targetDate(Instant.now())
            .userId(currentUser.getUserId())
            .build())
        .collect(Collectors.toList());
    return client.tasks().createTask().createMany(modifiedCommands);
  }
  @Override
  public Uni<List<Task>> updateTasks(String projectId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return client.tasks().updateTask().updateMany(modifiedCommands);
  }
  @Override
  public Uni<Task> updateTask(String projectId, String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return client.tasks().updateTask().updateOne(modifiedCommands);
  }
  @Override
  public Uni<List<Task>> deleteTasks(String projectId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return client.tasks().updateTask().updateMany(modifiedCommands);
  }
  @Override
  public Uni<List<Task>> findArchivedTasks(String projectId, LocalDate fromCreatedOrUpdated) {
    return client.tasks().queryArchivedTasks().findAll(fromCreatedOrUpdated);
  }
  @Override
  public Uni<Task> deleteOneTask(String projectId, String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return client.tasks().updateTask().updateOne(modifiedCommands);
  }  
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("init")
  public Uni<HeadState> init() {
    return client.repo().query().repoName(currentProject.getProjectId()).headName(currentProject.getHead()).createIfNot()
        .onItem().transform(created -> HeadState.builder().created(true).build());
  }

}
