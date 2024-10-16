package io.resys.thena.tasks.client.api.actions;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;

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

import java.util.List;

import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.smallrye.mutiny.Uni;


public interface TaskActions {

  CreateTasks createTask();
  UpdateTasks updateTask();
  ActiveTasksQuery queryActiveTasks();
  ArchivedTasksQuery queryArchivedTasks();

  interface CreateTasks {
    CreateTasks evalAccess(TaskAccessEvaluator eval);
    Uni<Task> createOne(CreateTask command);
    Uni<List<Task>> createMany(List<? extends CreateTask> commands);
  }

  interface UpdateTasks {
    UpdateTasks evalAccess(TaskAccessEvaluator eval);
    Uni<Task> updateOne(TaskUpdateCommand command);
    Uni<Task> updateOne(List<TaskUpdateCommand> commands);
    Uni<List<Task>> updateMany(List<TaskUpdateCommand> commands);
  }

  interface ActiveTasksQuery {
    ActiveTasksQuery evalAccess(TaskAccessEvaluator eval);
    Uni<List<Task>> findAll();
    Uni<List<Task>> findByCustomer(String customerId);
    Uni<List<Task>> findByTaskIds(Collection<String> taskIds);
    Uni<List<Task>> findByRoles(Collection<String> roles);
    Uni<List<Task>> findByAssignee(Collection<String> assignees);
    Uni<Task> get(String id);
    Uni<List<Task>> deleteAll(String userId, Instant targetDate);
  }
  
  interface ArchivedTasksQuery {
    ArchivedTasksQuery evalAccess(TaskAccessEvaluator eval);
    ArchivedTasksQuery title(String likeTitle); // like == doesn't need to match exactly. If a title contains "bob", then it can be matched by bob
    ArchivedTasksQuery description(String likeDescription);
    ArchivedTasksQuery reporterId(String reporterId);
    Uni<List<Task>> findAll(LocalDate fromCreatedOrUpdated);
  }
 
  interface TaskAccessEvaluator {
    TaskAccess getReadAccess(Task task);
    TaskAccess getCreateAccess(Task task);
    TaskAccess getUpdatedAccess(Task task);
    TaskAccess getDeleteAccess(Task task);
  }
  
  interface TaskAccess {
    boolean isAccessGranted();
    String getMessage();
    List<String> getRequired();
  }
}
