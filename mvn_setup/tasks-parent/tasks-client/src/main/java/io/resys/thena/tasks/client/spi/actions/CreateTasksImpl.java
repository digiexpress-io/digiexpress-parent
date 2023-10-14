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

import java.util.Arrays;
import java.util.List;

import io.resys.thena.tasks.client.api.actions.TaskActions.CreateTasks;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.visitors.CreateTasksVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateTasksImpl implements CreateTasks {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Task> createOne(CreateTask command) {
    return this.createMany(Arrays.asList(command))
       .onItem().transform(tasks -> tasks.get(0)) ;
  }
  
  @Override
  public Uni<List<Task>> createMany(List<? extends CreateTask> commands) {
    return ctx.getConfig().accept(new CreateTasksVisitor(commands));
  }

}
