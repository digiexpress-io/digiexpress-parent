package io.resys.thena.projects.client.spi.store;

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

import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.BranchActions.BranchObjectsQuery;
import io.resys.thena.docdb.api.actions.CommitActions.CommitBuilder;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryQuery;
import io.resys.thena.docdb.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.api.models.ThenaObjects.BranchObjects;
import io.resys.thena.docdb.api.models.ThenaObjects.HistoryObjects;
import io.resys.thena.docdb.api.models.ThenaObjects.PullObject;
import io.resys.thena.docdb.api.models.ThenaObjects.PullObjects;
import io.resys.thena.projects.client.api.model.Document.DocumentType;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface DocumentConfig {
  DocDB getClient();
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
    @Nullable BranchObjects visitEnvelope(DocumentConfig config, QueryEnvelope<BranchObjects> envelope);
    T end(DocumentConfig config, @Nullable BranchObjects ref);
  }
  
  interface DocPullObjectVisitor<T> extends DocVisitor { 
    PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder);
    @Nullable PullObject visitEnvelope(DocumentConfig config, QueryEnvelope<PullObject> envelope);
    T end(DocumentConfig config, @Nullable PullObject blob);
  }
  
  interface DocPullObjectsVisitor<T> extends DocVisitor { 
    PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder);
    @Nullable PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope);
    List<T> end(DocumentConfig config, @Nullable PullObjects blobs);
  }
  
  interface DocPullAndCommitVisitor<T> extends DocVisitor { 
    PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder);
    @Nullable PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope);
    Uni<List<T>> end(DocumentConfig config, @Nullable PullObjects blobs);
  }
  
  interface DocCommitVisitor<T> extends DocVisitor { 
    CommitBuilder start(DocumentConfig config, CommitBuilder builder);
    @Nullable Commit visitEnvelope(DocumentConfig config, CommitResultEnvelope envelope);
    List<T> end(DocumentConfig config, @Nullable Commit commit);
  }
  
  interface DocHistoryVisitor<T> extends DocVisitor { 
    BlobHistoryQuery start(DocumentConfig config, BlobHistoryQuery builder);
    @Nullable HistoryObjects visitEnvelope(DocumentConfig config, QueryEnvelope<HistoryObjects> envelope);
    List<T> end(DocumentConfig config, @Nullable HistoryObjects values);
  }

  default <T> Uni<T> accept(DocBranchVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient()
        .branch().branchQuery()
        .projectName(getProjectName())
        .branchName(getHeadName()));
    
    return builder.get()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(DocPullObjectVisitor<T> visitor) {
    final PullObjectsQuery builder = visitor.start(this, getClient()
        .pull().pullQuery()
        .projectName(getProjectName())
        .branchNameOrCommitOrTag(getHeadName()));
    
    return builder.get()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }

  
  default <T> Uni<List<T>> accept(DocPullObjectsVisitor<T> visitor) {
    final PullObjectsQuery builder = visitor.start(this, getClient()
        .pull().pullQuery()
        .projectName(getProjectName())
        .branchNameOrCommitOrTag(getHeadName()));
    
    return builder.findAll()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }

  
  default <T> Uni<List<T>> accept(DocPullAndCommitVisitor<T> visitor) {
    final var prefilled = getClient()
        .pull().pullQuery()
        .projectName(getProjectName())
        .branchNameOrCommitOrTag(getHeadName());
    
    final Uni<QueryEnvelope<PullObjects>> query = visitor.start(this, prefilled).findAll();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transformToUni(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<List<T>> accept(DocCommitVisitor<T> visitor) {
    final var prefilled = getClient()
        .commit().commitBuilder()
        .latestCommit()
        .author(getAuthor().get())
        .head(getProjectName(),getHeadName());
    
    final Uni<CommitResultEnvelope> query = visitor.start(this, prefilled).build();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<List<T>> accept(DocHistoryVisitor<T> visitor) {
    final var prefilled = getClient()
        .history()
        .blobQuery()
        .head(getProjectName(), getHeadName());
    
    final Uni<QueryEnvelope<HistoryObjects>> query = visitor.start(this, prefilled).get();
    return query
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
}
