package io.resys.thena.tasks.client.thenagit.actions;

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

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import io.resys.thena.tasks.client.api.actions.TaskActions.ActiveTasksQuery;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.thenagit.store.DocumentStore;
import io.resys.thena.tasks.client.thenagit.visitors.DeleteAllTasksVisitor;
import io.resys.thena.tasks.client.thenagit.visitors.FindAllActiveTasksVisitor;
import io.resys.thena.tasks.client.thenagit.visitors.GetActiveTaskVisitor;
import io.resys.thena.tasks.client.thenagit.visitors.GetActiveTasksByIdsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ActiveTasksQueryImpl implements ActiveTasksQuery {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Task> get(String id) {
    return ctx.getConfig().accept(new GetActiveTaskVisitor(id));
  }
  
  @Override
  public Uni<List<Task>> findAll() {
    return ctx.getConfig().accept(new FindAllActiveTasksVisitor());
  }

  @Override
  public Uni<List<Task>> deleteAll(String userId, Instant targetDate) {
    return ctx.getConfig().accept(new DeleteAllTasksVisitor(userId, targetDate));
  }
  
  @Override
  public Uni<List<Task>> findByTaskIds(Collection<String> taskIds) {
    return ctx.getConfig().accept(new GetActiveTasksByIdsVisitor(taskIds));
  }
  


  @Override
  public Uni<List<Task>> findByRoles(Collection<String> roles) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Uni<List<Task>> findByAssignee(Collection<String> assignees) {
    // TODO Auto-generated method stub
    return null;
  }
}
