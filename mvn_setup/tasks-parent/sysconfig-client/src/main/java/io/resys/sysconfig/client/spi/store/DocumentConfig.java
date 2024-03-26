package io.resys.sysconfig.client.spi.store;

import java.util.List;

import javax.annotation.Nullable;



import org.immutables.value.Value;

import io.resys.sysconfig.client.api.model.Document.DocumentType;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.ThenaDocObjects.DocObject;
import io.resys.thena.api.entities.doc.ThenaDocObjects.DocObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
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
    @Nullable DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocObjects> envelope);
    T end(DocumentConfig config, @Nullable DocObjects ref);
  }
  
  interface DocObjectVisitor<T> extends DocVisitor { 
    DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder);
    @Nullable DocObject visitEnvelope(DocumentConfig config, QueryEnvelope<DocObject> envelope);
    T end(DocumentConfig config, @Nullable DocObject ref);
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
