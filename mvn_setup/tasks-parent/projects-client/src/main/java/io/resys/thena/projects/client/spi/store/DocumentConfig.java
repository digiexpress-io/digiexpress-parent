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

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.projects.client.api.model.Document.DocumentType;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface DocumentConfig {
  ThenaClient getClient();
  String getRepoId();
  String getBranchName();
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
  interface DocVisitor {}
  
  interface DocObjectsVisitor<T> extends DocVisitor { 
    DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder);
    @Nullable DocQueryActions.DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope);
    T end(DocumentConfig config, @Nullable DocQueryActions.DocObjects ref);
  }
  
  interface DocObjectVisitor<T> extends DocVisitor { 
    DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder);
    @Nullable DocQueryActions.DocObject visitEnvelope(DocumentConfig config, QueryEnvelope<DocQueryActions.DocObject> envelope);
    T end(DocumentConfig config, @Nullable DocQueryActions.DocObject ref);
  }
  
  interface DocCreateVisitor<T> extends DocVisitor { 
    CreateManyDocs start(DocumentConfig config, CreateManyDocs builder);
    List<DocBranch> visitEnvelope(DocumentConfig config, ManyDocsEnvelope envelope);
    List<T> end(DocumentConfig config, List<DocBranch> commit);
  }
  
  
  default <T> Uni<List<T>> accept(DocCreateVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getRepoId())
        .commit().createManyDocs()
        .branchName(getBranchName()));
    
    return builder.build()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(DocObjectsVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getRepoId())
        .find().docQuery()
        .branchName(getBranchName()));
    
    return builder.findAll()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(DocObjectVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getRepoId())
        .find().docQuery()
        .branchName(getBranchName()));
    
    return builder.get()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
}
