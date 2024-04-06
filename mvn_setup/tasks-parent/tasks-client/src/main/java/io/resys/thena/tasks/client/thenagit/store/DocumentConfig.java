package io.resys.thena.tasks.client.thenagit.store;

import java.util.List;

import javax.annotation.Nullable;

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

import org.immutables.value.Value;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.GitBranchActions;
import io.resys.thena.api.actions.GitBranchActions.BranchObjectsQuery;
import io.resys.thena.api.actions.GitCommitActions.CommitBuilder;
import io.resys.thena.api.actions.GitCommitActions.CommitResultEnvelope;
import io.resys.thena.api.actions.GitHistoryActions;
import io.resys.thena.api.actions.GitHistoryActions.BlobHistoryQuery;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GitPullActions.PullObjectsQuery;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface DocumentConfig {
  ThenaClient getClient();
  String getProjectName();
  String getHeadName();
  DocumentGidProvider getGid();
  DocumentAuthorProvider getAuthor();
  
  interface DocumentGidProvider {
    String getNextId(DocumentType entity);
    String getNextVersion(DocumentType entity);
  }
  
  @FunctionalInterface
  interface DocumentAuthorProvider {
    String get();
  }
  
  interface DocVisitor {
    
  }
  
  interface DocBranchVisitor<T> extends DocVisitor { 
    BranchObjectsQuery start(DocumentConfig config, BranchObjectsQuery builder);
    @Nullable GitBranchActions.BranchObjects visitEnvelope(DocumentConfig config, QueryEnvelope<GitBranchActions.BranchObjects> envelope);
    T end(DocumentConfig config, @Nullable GitBranchActions.BranchObjects ref);
  }
  
  interface DocPullObjectVisitor<T> extends DocVisitor { 
    PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder);
    @Nullable GitPullActions.PullObject visitEnvelope(DocumentConfig config, QueryEnvelope<GitPullActions.PullObject> envelope);
    T end(DocumentConfig config, @Nullable GitPullActions.PullObject blob);
  }
  
  interface DocPullObjectsVisitor<T> extends DocVisitor { 
    PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder);
    @Nullable GitPullActions.PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<GitPullActions.PullObjects> envelope);
    List<T> end(DocumentConfig config, @Nullable GitPullActions.PullObjects blobs);
  }
  
  interface DocPullAndCommitVisitor<T> extends DocVisitor { 
    PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder);
    @Nullable GitPullActions.PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<GitPullActions.PullObjects> envelope);
    Uni<List<T>> end(DocumentConfig config, @Nullable GitPullActions.PullObjects blobs);
  }
  
  interface DocCommitVisitor<T> extends DocVisitor { 
    CommitBuilder start(DocumentConfig config, CommitBuilder builder);
    @Nullable Commit visitEnvelope(DocumentConfig config, CommitResultEnvelope envelope);
    List<T> end(DocumentConfig config, @Nullable Commit commit);
  }
  
  interface DocHistoryVisitor<T> extends DocVisitor { 
    BlobHistoryQuery start(DocumentConfig config, BlobHistoryQuery builder);
    @Nullable GitHistoryActions.HistoryObjects visitEnvelope(DocumentConfig config, QueryEnvelope<GitHistoryActions.HistoryObjects> envelope);
    List<T> end(DocumentConfig config, @Nullable GitHistoryActions.HistoryObjects values);
  }

  default <T> Uni<T> accept(DocBranchVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().git(getProjectName())
        .branch().branchQuery()
        .branchName(getHeadName()));
    
    return builder.get()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(DocPullObjectVisitor<T> visitor) {
    final PullObjectsQuery builder = visitor.start(this, getClient().git(getProjectName())
        .pull().pullQuery()
        .branchNameOrCommitOrTag(getHeadName()));
    
    return builder.get()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }

  
  default <T> Uni<List<T>> accept(DocPullObjectsVisitor<T> visitor) {
    final PullObjectsQuery builder = visitor.start(this, getClient().git(getProjectName())
        .pull().pullQuery()
        .branchNameOrCommitOrTag(getHeadName()));
    
    return builder.findAll()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }

  
  default <T> Uni<List<T>> accept(DocPullAndCommitVisitor<T> visitor) {
    final var prefilled = getClient().git(getProjectName())
        .pull().pullQuery()
        .branchNameOrCommitOrTag(getHeadName());
    
    final Uni<QueryEnvelope<GitPullActions.PullObjects>> query = visitor.start(this, prefilled).findAll();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transformToUni(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<List<T>> accept(DocCommitVisitor<T> visitor) {
    final var prefilled = getClient().git(getProjectName())
        .commit().commitBuilder()
        .latestCommit()
        .author(getAuthor().get())
        .branchName(getHeadName());
    
    final Uni<CommitResultEnvelope> query = visitor.start(this, prefilled).build();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<List<T>> accept(DocHistoryVisitor<T> visitor) {
    final var prefilled = getClient().git(getProjectName())
        .history()
        .blobQuery()
        .branchName(getHeadName());
    
    final Uni<QueryEnvelope<GitHistoryActions.HistoryObjects>> query = visitor.start(this, prefilled).get();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
}
