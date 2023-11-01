package io.resys.thena.projects.client.spi.actions;

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

import io.resys.thena.projects.client.api.actions.ProjectsActions.ActiveProjectsQuery;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import io.resys.thena.projects.client.spi.visitors.DeleteAllProjectsVisitor;
import io.resys.thena.projects.client.spi.visitors.FindAllActiveProjectsVisitor;
import io.resys.thena.projects.client.spi.visitors.GetActiveProjectVisitor;
import io.resys.thena.projects.client.spi.visitors.GetActiveProjectsByIdsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ActiveProjectsQueryImpl implements ActiveProjectsQuery {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Project> get(String id) {
    return ctx.getConfig().accept(new GetActiveProjectVisitor(id));
  }
  
  @Override
  public Uni<List<Project>> findAll() {
    return ctx.getConfig().accept(new FindAllActiveProjectsVisitor());
  }

  @Override
  public Uni<List<Project>> deleteAll(String userId, Instant targetDate) {
    return ctx.getConfig().accept(new DeleteAllProjectsVisitor(userId, targetDate));
  }
  
  @Override
  public Uni<List<Project>> findByProjectIds(Collection<String> taskIds) {
    return ctx.getConfig().accept(new GetActiveProjectsByIdsVisitor(taskIds));
  }
}
