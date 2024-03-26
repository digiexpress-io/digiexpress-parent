package io.resys.crm.client.spi.visitors;

import java.util.List;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.store.DocumentConfig;
import io.resys.crm.client.spi.store.DocumentConfig.DocObjectVisitor;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.ThenaDocObject.Doc;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocCommit;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocLog;
import io.resys.thena.api.entities.doc.ThenaDocObjects.DocObject;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.crm.client.spi.store.DocumentStoreException;
import io.resys.crm.client.spi.store.MainBranch;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetActiveCustomerVisitor implements DocObjectVisitor<Customer>{
  private final String id;
  
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery query) {
    return query.matchId(id).branchName(MainBranch.HEAD_NAME);
  }

  @Override
  public DocObject visitEnvelope(DocumentConfig config, QueryEnvelope<DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_CUSTOMER_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_CUSTOMER_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }

  @Override
  public Customer end(DocumentConfig config, DocObject ref) {
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> 
        docBranch.getValue()
        .mapTo(ImmutableCustomer.class).withVersion(docBranch.getCommitId())
        ).iterator().next();
  }
}
