package io.resys.thena.tasks.dev.app;


import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.rest.TaskRestApi;
import io.resys.thena.tasks.dev.app.security.SecurityPolicyData;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@Singleton
public class TasksResource implements TaskRestApi {

  @Inject TaskClient tasks;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject ProjectClient tenantClient;
  @Inject SecurityPolicyData accessPolicy;
  
  @Override
  public Uni<List<Task>> findTasks() {
    return getClient().onItem().transformToUni(tuple -> {
      final var client = tuple.getItem1();
      final var access = tuple.getItem2();
      return client.tasks().queryActiveTasks().evalAccess(access).findAll();
    });
  }
  @Override
  public Uni<List<Task>> createTasks(List<CreateTask> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> ImmutableCreateTask.builder().from(command)
            .targetDate(Instant.now())
            .userId(currentUser.userId())
            .build())
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(tuple -> {
      final var client = tuple.getItem1();
      final var access = tuple.getItem2();
      return client.tasks()
          .createTask().evalAccess(access)
          .createMany(modifiedCommands);
        
    });
  }
  @Override
  public Uni<List<Task>> updateTasks(List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(tuple -> { 
      
      final var client = tuple.getItem1();
      final var access = tuple.getItem2();
      
      return client.tasks()
        .updateTask().evalAccess(access)
        .updateMany(modifiedCommands);
    });
  }
  @Override
  public Uni<Task> updateTask(String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(tuple -> {
      
      final var client = tuple.getItem1();
      final var access = tuple.getItem2();
      
      return client.tasks()
        .updateTask().evalAccess(access)
        .updateOne(modifiedCommands);
    }
   );
  }
  @Override
  public Uni<List<Task>> deleteTasks(List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(tuple -> { 
      final var client = tuple.getItem1();
      final var access = tuple.getItem2();
      
      return client.tasks()
        .updateTask().evalAccess(access)
        .updateMany(modifiedCommands);
    });
  }
  @Override
  public Uni<List<Task>> findArchivedTasks(LocalDate fromCreatedOrUpdated) {
    return getClient().onItem().transformToUni(tuple -> { 
      final var client = tuple.getItem1();
      final var access = tuple.getItem2();
      
      return client.tasks()
        .queryArchivedTasks().evalAccess(access)
        .findAll(fromCreatedOrUpdated); 
    });
  }
  @Override
  public Uni<Task> deleteOneTask(String taskId, List<TaskUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command
            .withTargetDate(Instant.now())
            .withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    return getClient().onItem().transformToUni(tuple -> { 
      final var client = tuple.getItem1();
      final var access = tuple.getItem2();
      
      return client.tasks()
        .updateTask().evalAccess(access)
        .updateOne(modifiedCommands);
    });
  }  

  private Uni<Tuple2<TaskClient, TaskAccessEvaluator>> getClient() {
    return Uni.combine().all().unis(
        getTaskConfig().onItem().transform(config -> tasks.withRepoId(config.getRepoId())),
        accessPolicy.getTaskAccessEvaluator(currentUser)
    ).asTuple();
  }
  
  private Uni<TenantRepoConfig> getTaskConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transform(config -> {
      final var crmConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.TASKS).findFirst().get();
      return crmConfig;
    });
  }

}
