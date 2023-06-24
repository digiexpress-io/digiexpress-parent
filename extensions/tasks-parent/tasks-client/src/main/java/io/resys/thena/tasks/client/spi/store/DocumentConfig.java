package io.resys.thena.tasks.client.spi.store;

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
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobStateBuilder;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefStateBuilder;
import io.resys.thena.docdb.api.models.ObjectsResult;
import io.resys.thena.tasks.client.api.model.Document.DocumentType;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface DocumentConfig {
  DocDB getClient();
  String getRepoName();
  String getHeadName();
  DocumentGidProvider getGid();
  DocumentAuthorProvider getAuthor();

  default <T> Uni<T> accept(DocRefVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient()
        .objects().refState()
        .repo(getRepoName())
        .ref(getHeadName()));
    
    return builder.build()
        .onItem().transform(envelope -> visitor.visit(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(DocBlobVisitor<T> visitor) {
    final BlobStateBuilder builder = visitor.start(this, getClient()
        .objects().blobState()
        .repo(getRepoName())
        .ref(getHeadName()));
    
    return builder.get()
        .onItem().transform(envelope -> visitor.visit(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }

  
  default <T> Uni<List<T>> accept(DocBlobsVisitor<T> visitor) {
    final BlobStateBuilder builder = visitor.start(this, getClient()
        .objects().blobState()
        .repo(getRepoName())
        .ref(getHeadName()));
    
    return builder.list()
        .onItem().transform(envelope -> visitor.visit(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }

  
  interface DocumentGidProvider {
    String getNextId(DocumentType entity);
    String getNextVersion(DocumentType entity);
  }
  
  @FunctionalInterface
  interface DocumentAuthorProvider {
    String get();
  }
  
  interface DocRefVisitor<T> { 
    RefStateBuilder start(DocumentConfig config, RefStateBuilder builder);
    @Nullable RefObjects visit(DocumentConfig config, ObjectsResult<RefObjects> envelope);
    T end(DocumentConfig config, @Nullable RefObjects ref);
  }
  
  interface DocBlobVisitor<T> { 
    BlobStateBuilder start(DocumentConfig config, BlobStateBuilder builder);
    @Nullable BlobObject visit(DocumentConfig config, ObjectsResult<BlobObject> envelope);
    T end(DocumentConfig config, @Nullable BlobObject blob);
  }
  
  interface DocBlobsVisitor<T> { 
    BlobStateBuilder start(DocumentConfig config, BlobStateBuilder builder);
    @Nullable BlobObjects visit(DocumentConfig config, ObjectsResult<BlobObjects> envelope);
    List<T> end(DocumentConfig config, @Nullable BlobObjects blobs);
  }
  
  
  
}
