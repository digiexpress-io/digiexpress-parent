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

import java.time.LocalDate;
import java.util.List;

import io.resys.thena.tasks.client.api.actions.TaskActions.ArchivedTasksQuery;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.visitors.GetArchivedTasksVisitor;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(fluent = true, chain = true)
@Getter(AccessLevel.NONE)
@RequiredArgsConstructor
@AllArgsConstructor
public class ArchivedTasksQueryImpl implements ArchivedTasksQuery {
  private final DocumentStore ctx;
  private String title;
  private String description;
  private String reporterId;

  @Override
  public Uni<List<Task>> findAll(LocalDate fromCreatedOrUpdated) {
    return ctx.getConfig().accept(new GetArchivedTasksVisitor(title, description, reporterId, fromCreatedOrUpdated));
  }

}
