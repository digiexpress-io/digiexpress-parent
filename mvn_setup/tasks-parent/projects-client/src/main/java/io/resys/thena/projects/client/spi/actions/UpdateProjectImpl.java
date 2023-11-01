package io.resys.thena.projects.client.spi.actions;

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

import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.projects.client.api.actions.ProjectsActions.UpdateProjectAction;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.api.model.ProjectCommand.ProjectUpdateCommand;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import io.resys.thena.projects.client.spi.visitors.UpdateProjectsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateProjectImpl implements UpdateProjectAction {

  private final DocumentStore ctx;

  @Override
  public Uni<Project> updateOne(ProjectUpdateCommand command) {        
    return updateOne(Arrays.asList(command));
  }

  @Override
  public Uni<Project> updateOne(List<ProjectUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    final var uniqueTaskIds = commands.stream().map(command -> command.getProjectId()).distinct().collect(Collectors.toList());
    RepoAssert.isTrue(uniqueTaskIds.size() == 1, () -> "Task id-s must be same, but got: %s!", uniqueTaskIds);
    
    return ctx.getConfig().accept(new UpdateProjectsVisitor(commands, ctx))
        .onItem().transform(tasks -> tasks.get(0));
  }

  @Override
  public Uni<List<Project>> updateMany(List<ProjectUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    return ctx.getConfig().accept(new UpdateProjectsVisitor(commands, ctx));
  }
}

