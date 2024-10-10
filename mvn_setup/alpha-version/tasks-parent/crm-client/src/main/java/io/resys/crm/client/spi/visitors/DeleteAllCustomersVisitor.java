package io.resys.crm.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.CrmStore;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllCustomersVisitor implements DocObjectsVisitor<Uni<List<Customer>>>{

  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery query) {
    this.removeCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
        .commitAuthor(config.getAuthor().get())
        .commitMessage("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return query.docType(CrmStore.DOC_TYPE_CUSTOMER).findAll();
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_TENANTS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<Customer>> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tenantsRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocStoreException("TENANT_ARCHIVE_FAIL", DocStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocStoreException("TENANT_REMOVE_FAIL", DocStoreException.convertMessages(commit));
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
