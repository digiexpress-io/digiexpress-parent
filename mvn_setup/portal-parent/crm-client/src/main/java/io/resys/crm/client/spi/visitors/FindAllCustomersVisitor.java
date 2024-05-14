package io.resys.crm.client.spi.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand;
import io.resys.crm.client.api.model.CustomerTransaction;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.api.model.ImmutableCustomerTransaction;
import io.resys.crm.client.spi.CrmStore;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.actions.DocQueryActions.IncludeInQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;

public class FindAllCustomersVisitor implements DocObjectsVisitor<List<Customer>> {
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.docType(CrmStore.DOC_TYPE_CUSTOMER)
        .include(IncludeInQuery.COMMANDS)
        .findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_CUSTOMERS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }

  @Override
  public List<Customer> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> mapToCustomer(docBranch, commands));
  }
  
  
  public static ImmutableCustomer mapToCustomer(DocBranch docBranch, List<DocCommands> commands) {
    final var tx = new ArrayList<CustomerTransaction>();
    for(final var command : commands) {
      final var customerTx = ImmutableCustomerTransaction.builder()
          .id(command.getId())
          .targetDate(command.getCreatedAt())
          .userId(command.getCreatedBy())
          .commands(command.getCommands().stream().map(e -> e.mapTo(CustomerCommand.class)).toList())
          .build();
      tx.add(customerTx);
    }
    
    return docBranch.getValue()
      .mapTo(ImmutableCustomer.class)
      .withTransactions(tx)
      .withVersion(docBranch.getCommitId())
      .withCreated(docBranch.getCreatedAt().toInstant())
      .withUpdated(docBranch.getUpdatedAt().toInstant());
  }
}
