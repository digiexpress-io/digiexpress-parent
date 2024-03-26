package io.resys.crm.client.spi.visitors;

import java.util.Collections;
import java.util.List;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.Document;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.store.DocumentConfig;
import io.resys.crm.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.api.actions.DocQueryActions.DocObjects;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.crm.client.spi.store.DocumentStoreException;
import io.resys.crm.client.spi.store.MainBranch;

public class FindAllCustomersVisitor implements DocObjectsVisitor<List<Customer>> {
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder
        .docType(Document.DocumentType.CUSTOMER.name())
        .branchName(MainBranch.HEAD_NAME);
  }
  @Override
  public DocQueryActions.DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocQueryActions.DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_CUSTOMERS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Customer> end(DocumentConfig config, DocQueryActions.DocObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> docBranch.getValue().mapTo(ImmutableCustomer.class));
  }
}
