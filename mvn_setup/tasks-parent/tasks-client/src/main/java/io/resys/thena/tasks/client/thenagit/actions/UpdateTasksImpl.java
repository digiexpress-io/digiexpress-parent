package io.resys.thena.tasks.client.thenagit.actions;

import java.util.Arrays;

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
import java.util.stream.Collectors;

import io.resys.thena.support.RepoAssert;
import io.resys.thena.tasks.client.api.actions.TaskActions.UpdateTasks;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.thenagit.store.DocumentStore;
import io.resys.thena.tasks.client.thenagit.visitors.UpdateTasksVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateTasksImpl implements UpdateTasks {

  private final DocumentStore ctx;

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
    
    return ctx.getConfig().accept(new UpdateTasksVisitor(commands, ctx))
        .onItem().transform(tasks -> tasks.get(0));
  }

  @Override
  public Uni<List<Task>> updateMany(List<TaskUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    return ctx.getConfig().accept(new UpdateTasksVisitor(commands, ctx));
  }
}

