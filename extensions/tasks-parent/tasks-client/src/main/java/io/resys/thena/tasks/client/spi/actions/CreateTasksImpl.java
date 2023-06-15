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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.resys.thena.tasks.client.api.actions.TaskActions.CreateTasks;
import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.Task.Status;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CreateTasksImpl implements CreateTasks {
  private final DocumentStore ctx;
  
  @Override
  public Uni<Task> createOne(CreateTask command) {
    final var entity = mapTask(command);
    final var json = JsonObject.mapFrom(entity);
    final var config = ctx.getConfig();
    return config.getClient().commit().builder()
      .head(config.getRepoName(), config.getHeadName())
      .message("Creating task")
      .parentIsLatest()
      .append(entity.getId(), json)
      .author(config.getAuthor().get())
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return entity;
        }
        throw new DocumentStoreException("SAVE_FAIL", json, DocumentStoreException.convertMessages(commit));
      });
  }

  @Override
  public Uni<List<Task>> createMany(List<CreateTask> commands) {
    final var config = ctx.getConfig();
    final var client = config.getClient().commit().builder();
    final var entities = new ArrayList<Task>();
    for(final var command : commands) {
      final var entity = mapTask(command);
      final var json = JsonObject.mapFrom(entity);
      client.append(entity.getId(), json);
      entities.add(entity);
    }
    return client
      .head(config.getRepoName(), config.getHeadName())
      .message("Creating task")
      .parentIsLatest()
      .author(config.getAuthor().get())
      .build().onItem().transform(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return entities;
        }
        throw new DocumentStoreException("SAVE_FAIL", DocumentStoreException.convertMessages(commit));
      });
  }
  
  private Task mapTask(CreateTask command) {
    final var gen = ctx.getConfig().getGid();
    final var targetDate = Optional.ofNullable(command.getTargetDate()).orElseGet(() -> LocalDateTime.now());
    return ImmutableTask.builder()
        .id(gen.getNextId(DocumentType.TASK))
        .version(gen.getNextVersion(DocumentType.TASK))
        .documentType(DocumentType.TASK)
        .addAllAssigneeIds(command.getAssigneeIds().stream().distinct().toList())
        .addAllRoles(command.getRoles().stream().distinct().toList())
        .reporterId(command.getReporterId())
        .labels(command.getLabels().stream().distinct().toList())
        .extensions(command.getExtensions())
        .comments(command.getComments())
        .title(command.getTitle())
        .description(command.getDescription())
        .priority(command.getPriority())
        .dueDate(command.getDueDate())
        .created(targetDate)
        .status(command.getStatus() == null ? Status.CREATED : command.getStatus())
        .addTransactions(ImmutableTaskTransaction.builder().id(String.valueOf(1)).addCommands(command).build())
        .build();

  }
}
