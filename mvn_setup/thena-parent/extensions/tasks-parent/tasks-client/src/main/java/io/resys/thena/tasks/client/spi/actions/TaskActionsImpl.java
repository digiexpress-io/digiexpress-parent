package io.resys.thena.tasks.client.spi.actions;

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

import io.resys.thena.tasks.client.api.actions.TaskActions;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TaskActionsImpl implements TaskActions {
  private final DocumentStore ctx;

  @Override
  public CreateTasks createTask(){
    return new CreateTasksImpl(ctx);
  }

  @Override
  public UpdateTasks updateTask() {
    return new UpdateTasksImpl(ctx);
  }

  @Override
  public ActiveTasksQuery queryActiveTasks() {
    return new ActiveTasksQueryImpl(ctx);
  }

  @Override
  public ArchivedTasksQuery queryArchivedTasks() {
    return new ArchivedTasksQueryImpl(ctx);
  }

  @Override
  public TaskHistoryQuery queryTaskHistory() {
    return new TaskHistoryQueryImpl(ctx);
  }
}
