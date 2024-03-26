package io.resys.thena.tasks.client.spi.visitors;

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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.api.entities.Tenant.CommitResultStatus;
import io.resys.thena.api.entities.git.ThenaGitObjects.PullObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.tasks.client.api.model.Document;
import io.resys.thena.tasks.client.api.model.ImmutableArchiveTask;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskTransaction;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocPullAndCommitVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllTasksVisitor implements DocPullAndCommitVisitor<Task>{

  private final String userId;
  private final Instant targetDate;
  
  private CommitBuilder archiveCommand;
  private CommitBuilder removeCommand;
  
  @Override
  public PullObjectsQuery start(DocumentConfig config, PullObjectsQuery query) {
    // Create two commands: one for making changes by adding archive flag, the other for deleting task from commit tree
    this.archiveCommand = visitCommitCommand(config).message("Archive tasks");
    this.removeCommand = visitCommitCommand(config).message("Delete tasks");
    
    // Build the blob criteria for finding all documents of type task
    return query.matchBy(
          MatchCriteria.equalsTo("documentType", Document.DocumentType.TASK.name())
    );
  }

  @Override
  public PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TASKS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<Task>> end(DocumentConfig config, PullObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tasksRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((CommitResultEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("ARCHIVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((CommitResultEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("REMOVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> tasksRemoved);
  }

  
  
  private CommitBuilder visitCommitCommand(DocumentConfig config) {
    final var client = config.getClient();
    return client.git(config.getProjectName()).commit().commitBuilder()
      .branchName(config.getHeadName())
      .latestCommit()
      .author(config.getAuthor().get());
  }
  
  
  private List<Task> visitTree(PullObjects state) {
    return state.getBlob().stream()
      .map(blob -> blob.getValue().mapTo(ImmutableTask.class))
      .map(task -> visitTask(task))
      .collect(Collectors.toUnmodifiableList());
  }
  private Task visitTask(Task currentVersion) {
    final var taskId = currentVersion.getId();
    
    final var nextVersion = ImmutableTask.builder().from(currentVersion)
        .version(userId)
        .archived(targetDate)
        .addTransactions(ImmutableTaskTransaction.builder()
            .id(String.valueOf(currentVersion.getTransactions().size() +1))
            .addCommands(ImmutableArchiveTask.builder()
                .taskId(taskId)
                .userId(userId)
                .targetDate(targetDate)
                .build())
            .build())
        .build();
    final var json = JsonObject.mapFrom(nextVersion);
    archiveCommand.append(taskId, json);    
    removeCommand.remove(taskId);
    return nextVersion;
  }

}
