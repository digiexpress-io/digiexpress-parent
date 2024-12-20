package io.digiexpress.eveli.client.spi.task;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TaskClient {

  private final TaskNotificator notificator;
  private final TaskStore ctx;
  
  
  @Override
  public PaginateTasks paginateTasks() {
    return new PaginateTasksImpl(ctx);
  }
  @Override
  public QueryTasks queryTasks() {
    return new QueryTasks() {
      @Override
      public Uni<Task> getOneById(String taskId) {
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return ctx.getConfig().accept(new GetOneTaskByIdVisitor(taskId));
      }
      @Override
      public Uni<List<Task>> findAll(List<String> taskIds) {
        TaskAssert.notNull(taskIds, () -> "taskIds can't be empty!");
        return ctx.getConfig().accept(new FindAllTaskByIdsVisitor(taskIds));
      }
    };
  }

  @Override
  public TaskCommandBuilder taskBuilder() {
    return new TaskCommandBuilder() {
      private String userId, userEmail;
      @Override
      public TaskCommandBuilder userId(String userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
        return this;
      }
      @Override
      public Uni<TaskComment> createTaskComment(CreateTaskCommentCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        return ctx.getConfig().accept(new CreateOneTaskComment(userId, notificator, command));
      }
      @Override
      public Uni<Task> createTask(CreateTaskCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        return ctx.getConfig().accept(new CreateOneTask(userId, notificator, command));
      }
      @Override
      public Uni<Task> modifyTask(String taskId, ModifyTaskCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        return ctx.getConfig().accept(new ModifyOneTask(userId, userEmail, notificator, taskId, command));
      }
      @Override
      public Uni<Task> deleteTask(String taskId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        //TaskAssert.notEmpty(userEmail, () -> "userEmail can't be empty!");
        return ctx.getConfig().accept(new DeleteOneTask(userId, userEmail, taskId));
      }
      @Override
      public Uni<Task> addWorkerCommitViewer(String taskId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return ctx.getConfig().accept(new AddWorkerCommitViewer(userId, taskId));
      }
      @Override
      public Uni<Task> addCustomerCommitViewer(String taskId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        TaskAssert.notEmpty(taskId, () -> "taskId can't be empty!");
        return ctx.getConfig().accept(new AddCustomerCommitViewer(userId, taskId));
      }
    };
  }

  @Override
  public QueryTaskComments queryTaskComments() {
    return new QueryTaskComments() {
      @Override
      public Uni<TaskComment> getOneById(String commentId) {
        return ctx.getConfig().accept(new GetOneTaskCommentByIdVisitor(commentId));
      }
      @Override
      public Uni<List<TaskComment>> findAllByTaskId(String taskId) {        
        return ctx.getConfig().accept(new FindAllTaskCommentsByTaskIdVisitor(taskId));
      }
      @Override
      public Uni<List<TaskComment>> findAllByReporterId(String reporterId) {
        return ctx.getConfig().accept(new FindAllTaskCommentsByReporterIdVisitor(reporterId));
      }
    };
  }

  @Override
  public QueryTaskKeywords queryTaskKeywords() {
    return new QueryTaskKeywords() {
      @Override
      public Uni<List<String>> findAllKeywords() {
        final var config = ctx.getConfig();
        final var grim = config.getClient().grim(config.getTenantName());
        final Uni<List<String>> items = grim.find()
            .missionLabelQuery().findAllUnique()
            .map(e -> new ArrayList<>(e.stream()
                .map(x -> x.getLabelValue())
                .collect(Collectors.toSet()))
            );
        return items;
      }
    };
  }
  @Override
  public QueryUnreadUserTasks queryUnreadUserTasks() {
    return new QueryUnreadUserTasks() {
      private String userId;
      private final List<String> roles = new ArrayList<>();
      @Override
      public QueryUnreadUserTasks userId(String userId) {
        this.userId = userId;
        return this;
      }
      @Override
      public QueryUnreadUserTasks requireAnyRoles(List<String> roles) {
        TaskAssert.notEmpty("roles", () -> "roles can't be empty!");
        this.roles.addAll(roles);
        return this;
      }
      @Override
      public Uni<List<String>> findAll() {
        TaskAssert.notEmpty("userId", () -> "userId can't be empty!");
        return ctx.getConfig().accept(new FindAllUnreadTasksVisitor(userId, roles, TaskMapper.VIEWER_WORKER));
      }
    };
  }

}
