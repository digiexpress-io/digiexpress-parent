package io.resys.avatar.client.spi.store;

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
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface AvatarStoreConfig {
  public static String HEAD_NAME = "main";
  public static String DOC_TYPE = "AVATAR";
  
  ThenaClient getClient();
  String getTenantId();
  AuthorProvider getAuthor();
  
  @FunctionalInterface
  interface AuthorProvider {
    String get();
  }
  interface AvatarDocVisitor {}
  
  interface AvatarDocObjectsVisitor<T> extends AvatarDocVisitor { 
    DocObjectsQuery start(AvatarStoreConfig config, DocObjectsQuery builder);
    @Nullable DocQueryActions.DocObjects visitEnvelope(AvatarStoreConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope);
    T end(AvatarStoreConfig config, @Nullable DocQueryActions.DocObjects ref);
  }
  
  interface AvatarDocObjectVisitor<T> extends AvatarDocVisitor { 
    DocObjectsQuery start(AvatarStoreConfig config, DocObjectsQuery builder);
    @Nullable DocQueryActions.DocObject visitEnvelope(AvatarStoreConfig config, QueryEnvelope<DocQueryActions.DocObject> envelope);
    T end(AvatarStoreConfig config, @Nullable DocQueryActions.DocObject ref);
  }
  
  interface AvatarDocCreateVisitor<T> extends AvatarDocVisitor { 
    CreateManyDocs start(AvatarStoreConfig config, CreateManyDocs builder);
    List<DocBranch> visitEnvelope(AvatarStoreConfig config, ManyDocsEnvelope envelope);
    List<T> end(AvatarStoreConfig config, List<DocBranch> commit);
  }
  
  
  default <T> Uni<List<T>> accept(AvatarDocCreateVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getTenantId())
        .commit().createManyDocs()
        .docType(DOC_TYPE)
        .branchName(HEAD_NAME))
        .author(getAuthor().get());
    
    return builder.build()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(AvatarDocObjectsVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getTenantId())
        .find().docQuery()
        .docType(DOC_TYPE)
        .branchName(HEAD_NAME));
    
    return builder.findAll()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(AvatarDocObjectVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getTenantId())
        .find().docQuery()
        .docType(DOC_TYPE)
        .branchName(HEAD_NAME));
    
    return builder.get()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
}
