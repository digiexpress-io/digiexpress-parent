package io.resys.crm.client.spi.store;

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
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;


@Value.Immutable
public interface CrmStoreConfig {
  public final String DOC_TYPE_CUSTOMER = "CUSTOMER";
  
  ThenaClient getClient();
  String getRepoId();
  CrmAuthorProvider getAuthor();
  
  @FunctionalInterface
  interface CrmAuthorProvider {
    String get();
  }
  interface DocVisitor {}
  
  interface DocObjectsVisitor<T> extends DocVisitor { 
    Uni<QueryEnvelope<DocTenantObjects>> start(CrmStoreConfig config, DocObjectsQuery builder);
    @Nullable DocTenantObjects visitEnvelope(CrmStoreConfig config, QueryEnvelope<DocTenantObjects> envelope);
    T end(CrmStoreConfig config, @Nullable DocTenantObjects ref);
  }
  
  interface DocObjectVisitor<T> extends DocVisitor { 
    Uni<QueryEnvelope<DocObject>> start(CrmStoreConfig config, DocObjectsQuery builder);
    @Nullable DocObject visitEnvelope(CrmStoreConfig config, QueryEnvelope<DocObject> envelope);
    T end(CrmStoreConfig config, @Nullable DocObject ref);
  }
  
  interface DocCreateVisitor<T> extends DocVisitor { 
    CreateManyDocs start(CrmStoreConfig config, CreateManyDocs builder);
    List<DocBranch> visitEnvelope(CrmStoreConfig config, ManyDocsEnvelope envelope);
    List<T> end(CrmStoreConfig config, List<DocBranch> commit);
  }
  
  
  default <T> Uni<List<T>> accept(DocCreateVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getRepoId()).commit().createManyDocs());
    
    return builder.build()
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(DocObjectsVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getRepoId()).find().docQuery());
    
    return builder
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
  
  default <T> Uni<T> accept(DocObjectVisitor<T> visitor) {
    final var builder = visitor.start(this, getClient().doc(getRepoId()).find().docQuery());
    
    return builder
        .onItem().transform(envelope -> visitor.visitEnvelope(this, envelope))
        .onItem().transform(ref -> visitor.end(this, ref));
  }
}
