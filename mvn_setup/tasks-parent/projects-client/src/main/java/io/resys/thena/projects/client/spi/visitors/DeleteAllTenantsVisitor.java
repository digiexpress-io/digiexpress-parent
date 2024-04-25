package io.resys.thena.projects.client.spi.visitors;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.spi.store.ProjectStoreConfig;
import io.resys.thena.projects.client.spi.store.ProjectStoreConfig.DocObjectsVisitor;
import io.resys.thena.projects.client.spi.store.ProjectStoreException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllTenantsVisitor implements DocObjectsVisitor<Uni<List<TenantConfig>>>{

  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ProjectStoreConfig config, DocObjectsQuery query) {
    // Create two commands: one for making changes by adding archive flag, the other for deleting Project from commit tree
    this.archiveCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyBranches()
        .commitAuthor(config.getAuthor().get())
        .commitMessage("Archive Tenants");
    this.removeCommand = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
        .commitAuthor(config.getAuthor().get())
        .commitMessage("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return query.docType(TenantConfig.TENANT_CONFIG).findAll();
  }

  @Override
  public DocTenantObjects visitEnvelope(ProjectStoreConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw ProjectStoreException.builder("FIND_ALL_TENANTS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<TenantConfig>> end(ProjectStoreConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tenantsRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new ProjectStoreException("TENANT_ARCHIVE_FAIL", ProjectStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new ProjectStoreException("TENANT_REMOVE_FAIL", ProjectStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> tenantsRemoved);
  }
  private List<TenantConfig> visitTree(DocTenantObjects state) {
    return state.getBranches().values().stream()
      .map(blob -> blob.getValue().mapTo(ImmutableTenantConfig.class))
      .map(TenantConfig -> visitTenantConfig(TenantConfig))
      .collect(Collectors.toUnmodifiableList());
  }
  private TenantConfig visitTenantConfig(TenantConfig tenantConfig) {
    final var tenantId = tenantConfig.getId();
    
    final var nextVersion = ImmutableTenantConfig.builder().from(tenantConfig).archived(Instant.now()).build();
    final var json = JsonObject.mapFrom(nextVersion);
    archiveCommand.item().docId(tenantId).replace(json).next();
    removeCommand.item().docId(tenantId).remove();
    return nextVersion;
  }

}
