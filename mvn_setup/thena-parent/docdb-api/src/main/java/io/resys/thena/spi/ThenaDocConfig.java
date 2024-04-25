package io.resys.thena.spi;

import java.util.List;

import javax.annotation.Nullable;

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
public interface ThenaDocConfig {
  
  ThenaClient getClient();
  String getRepoId();
  AuthorProvider getAuthor();
  
  @FunctionalInterface
  interface AuthorProvider {
    String get();
  }
  
  interface DocVisitor {}
  

  interface DocObjectsVisitor<T> extends DocVisitor {
    Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder);
    @Nullable DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope);
    T end(ThenaDocConfig config, @Nullable DocTenantObjects ref);
  }
  
  interface DocObjectVisitor<T> extends DocVisitor { 
    Uni<QueryEnvelope<DocObject>> start(ThenaDocConfig config, DocObjectsQuery builder);
    @Nullable DocObject visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocObject> envelope);
    T end(ThenaDocConfig config, @Nullable DocObject ref);
  }
  
  interface DocCreateVisitor<T> extends DocVisitor { 
    CreateManyDocs start(ThenaDocConfig config, CreateManyDocs builder);
    List<DocBranch> visitEnvelope(ThenaDocConfig config, ManyDocsEnvelope envelope);
    List<T> end(ThenaDocConfig config, List<DocBranch> commit);
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