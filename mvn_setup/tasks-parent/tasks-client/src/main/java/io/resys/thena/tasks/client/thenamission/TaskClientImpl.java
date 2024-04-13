package io.resys.thena.tasks.client.thenamission;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.actions.TaskTenantsActions;
import io.resys.thena.tasks.client.api.actions.TaskTenantQuery;
import io.resys.thena.tasks.client.api.actions.TaskActions;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.thenamission.visitors.GetArchivedTasksVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.CreateTasksVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.DeleteAllTasksVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.FindAllTasksByAssigneesVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.FindAllTasksByIdVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.FindAllTasksByRolesVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.FindAllTasksVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.GetOneTaskVisitor;
import io.resys.thena.tasks.client.thenamission.visitors.UpdateTasksVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class TaskClientImpl implements TaskClient {
  private final TaskStore ctx;
  
  @Override
  public TaskActions tasks() {
    return new TaskActions() {
      @Override
      public CreateTasks createTask() {

        return new CreateTasks() {
          private TaskAccessEvaluator access;
          @Override
          public Uni<Task> createOne(CreateTask command) {
            return this.createMany(Arrays.asList(command)).onItem().transform(tasks -> tasks.get(0));
          }
          @Override
          public Uni<List<Task>> createMany(List<? extends CreateTask> commands) {
            return ctx.getConfig().accept(new CreateTasksVisitor(commands, access));
          }
          @Override
          public CreateTasks evalAccess(TaskAccessEvaluator eval) {
            this.access = eval;
            return this;
          }
        };
      }
      @Override
      public ActiveTasksQuery queryActiveTasks() {
        return new ActiveTasksQuery() {
          private TaskAccessEvaluator access;
          @Override
          public Uni<Task> get(String id) {
            return ctx.getConfig().accept(new GetOneTaskVisitor(id, this.access));
          }
          @Override
          public Uni<List<Task>> findByTaskIds(Collection<String> taskIds) {
            return ctx.getConfig().accept(new FindAllTasksByIdVisitor(taskIds, this.access));
          }
          @Override
          public Uni<List<Task>> findAll() {
            return ctx.getConfig().accept(new FindAllTasksVisitor(this.access));
          }
          @Override
          public Uni<List<Task>> findByRoles(Collection<String> roles) {
            return ctx.getConfig().accept(new FindAllTasksByRolesVisitor(roles, this.access));
          }
          @Override
          public Uni<List<Task>> findByAssignee(Collection<String> assignees) {
            return ctx.getConfig().accept(new FindAllTasksByAssigneesVisitor(assignees, this.access));
          }
         
          @Override
          public Uni<List<Task>> deleteAll(String userId, Instant targetDate) {
            return ctx.getConfig().accept(new DeleteAllTasksVisitor(userId, targetDate, ctx, this.access));
          }
          @Override
          public ActiveTasksQuery evalAccess(TaskAccessEvaluator eval) {
            this.access = eval;
            return this;
          }
        };
      }    
      @Override
      public UpdateTasks updateTask() {
        return new UpdateTasks() {
          private TaskAccessEvaluator access;
          @Override
          public Uni<Task> updateOne(TaskUpdateCommand command) {        
            return updateOne(Arrays.asList(command));
          }
          @Override
          public Uni<Task> updateOne(List<TaskUpdateCommand> commands) {
            RepoAssert.notNull(commands, () -> "commands must be defined!");
            RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
            
            final var uniqueTaskIds = commands.stream().map(command -> command.getTaskId()).distinct().collect(Collectors.toList());
            RepoAssert.isTrue(uniqueTaskIds.size() == 1, () -> "Task id-s must be same, but got: %s!", uniqueTaskIds);
            
            return ctx.getConfig().accept(new UpdateTasksVisitor(commands, ctx, access))
                .onItem().transform(tasks -> tasks.get(0));
          }
          @Override
          public Uni<List<Task>> updateMany(List<TaskUpdateCommand> commands) {
            RepoAssert.notNull(commands, () -> "commands must be defined!");
            RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");

            return ctx.getConfig().accept(new UpdateTasksVisitor(commands, ctx, access));
          }
          @Override
          public UpdateTasks evalAccess(TaskAccessEvaluator eval) {
            this.access = eval;
            return this;
          }
        };
      }
      @Override
      public ArchivedTasksQuery queryArchivedTasks() {
        return new ArchivedTasksQuery() {
          private TaskAccessEvaluator access;
          private String likeTitle, reporterId, likeDescription;
          @Override public ArchivedTasksQuery title(String likeTitle) { this.likeTitle = likeTitle; return this; }
          @Override public ArchivedTasksQuery reporterId(String reporterId) { this.reporterId = reporterId; return this; }
          @Override public ArchivedTasksQuery description(String likeDescription) { this.likeDescription = likeDescription; return this; }          
          @Override public Uni<List<Task>> findAll(LocalDate fromCreatedOrUpdated) {
            return ctx.getConfig().accept(new GetArchivedTasksVisitor(likeTitle, likeDescription, reporterId, fromCreatedOrUpdated, access));
          }
          @Override
          public ArchivedTasksQuery evalAccess(TaskAccessEvaluator eval) {
            this.access = eval;
            return this;
          }
        };
      }
    };
  }

  @Override
  public TaskTenantsActions tenants() {
    return new TaskTenantsActions() {
      @Override
      public Uni<Tenant> getRepo() { return ctx.getRepo(); }
      @Override
      public TaskTenantQuery query() {
        final var query = ctx.query();
        return new TaskTenantQuery() {
          @Override public TaskTenantQuery repoName(String repoName) { query.tenantName(repoName); return this; }
          @Override public TaskTenantQuery headName(String headName) { return this; }
          @Override public Uni<TaskClient> createIfNot() { return query.createIfNot().onItem().transform(doc -> new TaskClientImpl(doc)); }
          @Override public Uni<TaskClient> create() { return query.create().onItem().transform(doc -> new TaskClientImpl(doc)); }
          @Override public TaskClient build() { return new TaskClientImpl(query.build()); }
          @Override public Uni<TaskClient> delete() { return query.delete().onItem().transform(doc -> new TaskClientImpl(doc)); }
        };
      }
    }
;
  }
  public TaskStore getCtx() {
    return ctx;
  }
  @Override
  public TaskClient withRepoId(String repoId) {
    return new TaskClientImpl(ctx.withTenantId(repoId));
  }
}
