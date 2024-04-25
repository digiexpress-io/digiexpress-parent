package io.resys.crm.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.store.CrmStoreConfig;
import io.resys.crm.client.spi.store.CrmStoreConfig.DocObjectsVisitor;
import io.resys.crm.client.spi.store.CrmStoreException;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllCustomersVisitor implements DocObjectsVisitor<Uni<List<Customer>>>{

  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(CrmStoreConfig config, DocObjectsQuery query) {
    this.removeCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
        .commitAuthor(config.getAuthor().get())
        .commitMessage("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return query.docType(CrmStoreConfig.DOC_TYPE_CUSTOMER).findAll();
  }

  @Override
  public DocTenantObjects visitEnvelope(CrmStoreConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw CrmStoreException.builder("FIND_ALL_TENANTS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<Customer>> end(CrmStoreConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tenantsRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new CrmStoreException("TENANT_ARCHIVE_FAIL", CrmStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new CrmStoreException("TENANT_REMOVE_FAIL", CrmStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> tenantsRemoved);
  }

  
  
  
  private List<Customer> visitTree(DocTenantObjects state) {
    return state.getBranches().values().stream()
      .map(blob -> blob.getValue().mapTo(ImmutableCustomer.class))
      .map(customer -> archiveCustomer(customer))
      .collect(Collectors.toUnmodifiableList());
  }
  private Customer archiveCustomer(Customer customer) {
    removeCommand.item().docId(customer.getId()).remove();
    return customer;
  }

}
