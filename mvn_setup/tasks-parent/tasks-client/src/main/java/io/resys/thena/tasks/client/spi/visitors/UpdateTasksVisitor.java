package io.resys.thena.tasks.client.spi.visitors;

import java.util.ArrayList;

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
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.Repo.CommitResultStatus;
import io.resys.thena.api.models.ThenaGitObjects.PullObjects;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocPullAndCommitVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateTasksVisitor implements DocPullAndCommitVisitor<Task> {
  private final DocumentStore ctx;
  private final List<String> taskIds;
  private final CommitBuilder commitBuilder;
  private final Map<String, List<TaskUpdateCommand>> commandsByTaskId; 
  
  
  public UpdateTasksVisitor(List<TaskUpdateCommand> commands, DocumentStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByTaskId = commands.stream()
        .collect(Collectors.groupingBy(TaskUpdateCommand::getTaskId));
    this.taskIds = new ArrayList<>(commandsByTaskId.keySet());
    this.commitBuilder = config.getClient().git(config.getProjectName()).commit().commitBuilder()
        .branchName(config.getHeadName())
        .message("Update tasks: " + commandsByTaskId.size())
        .latestCommit()
        .author(config.getAuthor().get());
  }

  @Override
  public PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder) {
    return builder.docId(taskIds);
  }

  @Override
  public PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_TASKS_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_TASKS_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    if(taskIds.size() != result.getBlob().size()) {
      throw new DocumentStoreException("TASKS_UPDATE_FAIL_MISSING_TASKS", JsonObject.of("failedUpdates", taskIds));
    }
    return result;
  }

  @Override
  public Uni<List<Task>> end(DocumentConfig config, PullObjects blob) {
    final var updatedTasks = blob.accept((JsonObject blobValue) -> {
      final var start = blobValue.mapTo(ImmutableTask.class);
      final var commands = commandsByTaskId.get(start.getId());
      final var updated = new TaskCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
      this.commitBuilder.append(updated.getId(), JsonObject.mapFrom(updated));
      return updated;
    });
    
    return commitBuilder.build().onItem().transform(commit -> {
      if(commit.getStatus() != CommitResultStatus.OK) {
        final var failedUpdates = taskIds.stream().collect(Collectors.joining(",", "{", "}"));
        throw new DocumentStoreException("TASKS_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocumentStoreException.convertMessages(commit));
      }
      return updatedTasks;
    });
  }
}
