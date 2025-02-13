package io.resys.sysconfig.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.sysconfig.client.api.model.Document;
import io.resys.sysconfig.client.api.model.ImmutableSysConfig;
import io.resys.sysconfig.client.api.model.SysConfig;
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
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllSysConfigsVisitor implements DocObjectsVisitor<Uni<List<SysConfig>>>{

  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    this.removeCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
        .commitAuthor(config.getAuthor().get())
        .commitMessage("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return builder.docType(Document.DocumentType.SYS_CONFIG.name()).findAll();
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_SYS_CONFIGS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<SysConfig>> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tenantsRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocStoreException("SYS_CONFIG_ARCHIVE_FAIL", DocStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocStoreException("SYS_CONFIG_REMOVE_FAIL", DocStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> tenantsRemoved);
  }

  
  
  
  private List<SysConfig> visitTree(DocTenantObjects state) {
    return state.getBranches().values().stream()
      .map(blob -> blob.getValue().mapTo(ImmutableSysConfig.class))
      .map(document -> visitDocument(document))
      .collect(Collectors.toUnmodifiableList());
  }
  private SysConfig visitDocument(SysConfig customer) {
    final var json = JsonObject.mapFrom(customer);

    removeCommand.item().docId(customer.getId()).remove();
    return customer;
  }

}
