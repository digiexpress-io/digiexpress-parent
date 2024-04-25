package io.resys.thena.projects.client.spi.visitors;

import java.util.List;
import java.util.Map;

import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.spi.store.ProjectStoreConfig;
import io.resys.thena.projects.client.spi.store.ProjectStoreConfig.DocObjectVisitor;
import io.resys.thena.projects.client.spi.store.ProjectStoreException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetActiveTenantVisitor implements DocObjectVisitor<TenantConfig>{
  private final String id;
  
  @Override
  public Uni<QueryEnvelope<DocObject>> start(ProjectStoreConfig config, DocObjectsQuery query) {
    return query.get(id);
  }

  @Override
  public DocObject visitEnvelope(ProjectStoreConfig config, QueryEnvelope<DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw ProjectStoreException.builder("GET_TENANT_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw ProjectStoreException.builder("GET_TENANT_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }

  @Override
  public TenantConfig end(ProjectStoreConfig config, DocObject ref) {
    return ref.accept((Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> FindAllTenantsVisitor.mapToUserProfile(docBranch)).get(0);
  }
}
