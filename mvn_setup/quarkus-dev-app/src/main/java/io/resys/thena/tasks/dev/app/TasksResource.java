package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.rest.TaskRestApi;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentTenant;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentUser;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
public class TasksResource implements TaskRestApi {

  @Inject TaskClient tasks;
  @Inject CurrentTenant currentProject;
  @Inject CurrentUser currentUser;
  

  @Override
  public Uni<List<Task>> findTasks(String projectId) {
    return tasks.withRepoId(projectId).tasks().queryActiveTasks().findAll();
  }
  @Override
  public Uni<List<Task>> createTasks(String projectId, List<CreateTask> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> ImmutableCreateTask.builder().from(command)
            .targetDate(Instant.now())
            .userId(currentUser.getUserId())
            .build())
        .collect(Collectors.toList());
    return tasks.withRepoId(projectId).tasks().createTask().createMany(modifiedCommands);
  }
  @Override
  public Uni<List<Task>> updateTasks(String projectId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return tasks.withRepoId(projectId).tasks().updateTask().updateMany(modifiedCommands);
  }
  @Override
  public Uni<Task> updateTask(String projectId, String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return tasks.withRepoId(projectId).tasks().updateTask().updateOne(modifiedCommands);
  }
  @Override
  public Uni<List<Task>> deleteTasks(String projectId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return tasks.withRepoId(projectId).tasks().updateTask().updateMany(modifiedCommands);
  }
  @Override
  public Uni<List<Task>> findArchivedTasks(String projectId, LocalDate fromCreatedOrUpdated) {
    return tasks.withRepoId(projectId).tasks().queryArchivedTasks().findAll(fromCreatedOrUpdated);
  }
  @Override
  public Uni<Task> deleteOneTask(String projectId, String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.getUserId()))
        .collect(Collectors.toList());
    return tasks.withRepoId(projectId).tasks().updateTask().updateOne(modifiedCommands);
  }  
}
