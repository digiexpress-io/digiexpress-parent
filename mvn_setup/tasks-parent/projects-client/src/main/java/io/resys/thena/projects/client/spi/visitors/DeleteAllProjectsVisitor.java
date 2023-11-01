package io.resys.thena.projects.client.spi.visitors;

/*-
 * #%L
 * thena-Projects-client
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

import io.resys.thena.docdb.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObjects.PullObjects;
import io.resys.thena.projects.client.api.model.Document;
import io.resys.thena.projects.client.api.model.ImmutableArchiveProject;
import io.resys.thena.projects.client.api.model.ImmutableProject;
import io.resys.thena.projects.client.api.model.ImmutableProjectTransaction;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig.DocPullAndCommitVisitor;
import io.resys.thena.projects.client.spi.store.DocumentStoreException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllProjectsVisitor implements DocPullAndCommitVisitor<Project>{

  private final String userId;
  private final Instant targetDate;
  
  private CommitBuilder archiveCommand;
  private CommitBuilder removeCommand;
  
  @Override
  public PullObjectsQuery start(DocumentConfig config, PullObjectsQuery query) {
    // Create two commands: one for making changes by adding archive flag, the other for deleting Project from commit tree
    this.archiveCommand = visitCommitCommand(config).message("Archive Projects");
    this.removeCommand = visitCommitCommand(config).message("Delete Projects");
    
    // Build the blob criteria for finding all documents of type Project
    return query.matchBy(
          MatchCriteria.equalsTo("documentType", Document.DocumentType.PROJECT_META.name())
    );
  }

  @Override
  public PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_PROJECTS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<Project>> end(DocumentConfig config, PullObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var ProjectsRemoved = visitTree(ref);    
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
      .onItem().transform((commit) -> ProjectsRemoved);
  }

  
  
  private CommitBuilder visitCommitCommand(DocumentConfig config) {
    final var client = config.getClient();
    return client.commit().commitBuilder()
      .head(config.getProjectName(), config.getHeadName())
      .latestCommit()
      .author(config.getAuthor().get());
  }
  
  
  private List<Project> visitTree(PullObjects state) {
    return state.getBlob().stream()
      .map(blob -> blob.getValue().mapTo(ImmutableProject.class))
      .map(Project -> visitProject(Project))
      .collect(Collectors.toUnmodifiableList());
  }
  private Project visitProject(Project currentVersion) {
    final var projectId = currentVersion.getId();
    
    final var nextVersion = ImmutableProject.builder().from(currentVersion)
        .version(userId)
        .archived(targetDate)
        .addTransactions(ImmutableProjectTransaction.builder()
            .id(String.valueOf(currentVersion.getTransactions().size() +1))
            .addCommands(ImmutableArchiveProject.builder()
                .projectId(projectId)
                .userId(userId)
                .targetDate(targetDate)
                .build())
            .build())
        .build();
    final var json = JsonObject.mapFrom(nextVersion);
    archiveCommand.append(projectId, json);    
    removeCommand.remove(projectId);
    return nextVersion;
  }

}
