package io.resys.crm.client.spi.visitors;

import java.util.List;
import java.util.Map;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.spi.store.CrmStoreConfig;
import io.resys.crm.client.spi.store.CrmStoreConfig.DocObjectVisitor;
import io.resys.crm.client.spi.store.CrmStoreException;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.actions.DocQueryActions.IncludeInQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetActiveCustomerVisitor implements DocObjectVisitor<Customer>{
  private final String id;
  
  @Override
  public Uni<QueryEnvelope<DocObject>> start(CrmStoreConfig config, DocObjectsQuery builder) {
    return builder.include(IncludeInQuery.COMMANDS).get(id);
  }

  @Override
  public DocObject visitEnvelope(CrmStoreConfig config, QueryEnvelope<DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw CrmStoreException.builder("GET_CUSTOMER_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw CrmStoreException.builder("GET_CUSTOMER_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }

  @Override
  public Customer end(CrmStoreConfig config, DocObject ref) {
    return ref.accept((Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees
    ) -> FindAllCustomersVisitor.mapToCustomer(docBranch, commands)).iterator().next();
  }
}
