package io.resys.thena.tasks.dev.app;


import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.rest.TaskRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
public class TasksResource implements TaskRestApi {

  @Inject TaskClient tasks;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject TenantConfigClient tenantClient;

  @Override
  public Uni<List<Task>> findTasks() {
    return getClient().onItem().transformToUni(client -> client.tasks().queryActiveTasks().findAll());
  }
  @Override
  public Uni<List<Task>> createTasks(List<CreateTask> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> ImmutableCreateTask.builder().from(command)
            .targetDate(Instant.now())
            .userId(currentUser.userId())
            .build())
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(client -> client.tasks().createTask().createMany(modifiedCommands));
  }
  @Override
  public Uni<List<Task>> updateTasks(List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(client -> client.tasks().updateTask().updateMany(modifiedCommands));
  }
  @Override
  public Uni<Task> updateTask(String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(client -> client.tasks().updateTask().updateOne(modifiedCommands));
  }
  @Override
  public Uni<List<Task>> deleteTasks(List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(client -> client.tasks().updateTask().updateMany(modifiedCommands));
  }
  @Override
  public Uni<List<Task>> findArchivedTasks(LocalDate fromCreatedOrUpdated) {
    return getClient().onItem().transformToUni(client -> client.tasks().queryArchivedTasks().findAll(fromCreatedOrUpdated));
  }
  @Override
  public Uni<Task> deleteOneTask(String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(client -> client.tasks().updateTask().updateOne(modifiedCommands));
  }  

  private Uni<TaskClient> getClient() {
    return getTaskConfig().onItem().transform(config -> tasks.withRepoId(config.getRepoId()));
  }
  
  private Uni<TenantRepoConfig> getTaskConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transform(config -> {
      final var crmConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.TASKS).findFirst().get();
      return crmConfig;
    });
  }

}
