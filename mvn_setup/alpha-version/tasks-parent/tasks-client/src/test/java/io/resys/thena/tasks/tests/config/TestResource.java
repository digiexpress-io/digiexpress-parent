package io.resys.thena.tasks.tests.config;

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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.rest.TaskRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class TestResource implements TaskRestApi {

  private final ImmutableTask mockTask = ImmutableTask.builder().id("task1").version("task-version1")
      .treeVersion("")
      .archived(TaskTestCase.getTargetDate()).created(TaskTestCase.getTargetDate())
      .updated(TaskTestCase.getTargetDate()).title("task-title1").priority(Task.Priority.HIGH)
      .status(Task.Status.CREATED).description("Very good task indeed").reporterId("John Smith").build();

  @Override
  public Uni<List<Task>> findTasks() {
    return Uni.createFrom().item(Arrays.asList(mockTask));
  }

  @Override
  public Uni<List<Task>> createTasks(List<CreateTask> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockTask).collect(Collectors.toList()));
  }

  @Override
  public Uni<List<Task>> updateTasks(List<TaskUpdateCommand> commands) {
    return Uni.createFrom().item(commands.stream().map(e -> mockTask).collect(Collectors.toList()));
  }

  @Override
  public Uni<Task> updateTask(String taskId, List<TaskUpdateCommand> commands) {
    return Uni.createFrom().item(mockTask);
  }

  @Override
  public Uni<List<Task>> findArchivedTasks(LocalDate fromCreatedOrUpdated) {
    return Uni.createFrom().item(Arrays.asList(mockTask, mockTask));
  }

  @Override
  public Uni<Task> deleteOneTask(String taskId, List<TaskUpdateCommand> command) {
    return Uni.createFrom().item(mockTask);
  }

  @Override
  public Uni<List<Task>> deleteTasks(List<TaskUpdateCommand> commands) {
    return Uni.createFrom().item(Arrays.asList(mockTask, mockTask));
  }

}
