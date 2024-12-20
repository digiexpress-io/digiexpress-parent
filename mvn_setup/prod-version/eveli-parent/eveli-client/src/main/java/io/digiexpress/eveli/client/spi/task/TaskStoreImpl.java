package io.digiexpress.eveli.client.spi.task;

import java.util.Collections;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.ImmutableDocumentExceptionMsg;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class TaskStoreImpl implements TaskStore {
  private final TaskStoreConfig config;
  
  @Override
  public TaskStore withTenantId(String repoId) {
    return new TaskStoreImpl(ImmutableTaskStoreConfig.builder().from(config).tenantName(repoId).build());
  }
  @Override
  public Uni<Tenant> getRepo() {
    final var client = config.getClient();
    return client.tenants().find().id(config.getTenantName()).get();
  }
  @Override public TaskStoreConfig getConfig() { return config; }
  @Override public TaskTenantQuery query() {
    return new TaskTenantQuery() {
      private String tenantName;
      @Override public TaskTenantQuery tenantName(String tenantName) { this.tenantName = tenantName; return this; }
      @Override public Uni<TaskStore> create() { return createRepo(tenantName); }
      @Override public TaskStore build() { return createClientStore(tenantName); }
      @Override public Uni<TaskStore> createIfNot() { return createRepoOrGetRepo(tenantName); }
      @Override public Uni<TaskStore> delete() { return deleteRepo(tenantName); }
    };
  }
  
  private Uni<TaskStore> createRepoOrGetRepo(String repoName) {
    final var client = config.getClient();
    
    return client.tenants().find().id(repoName).get()
        .onItem().transformToUni(repo -> {        
          if(repo == null) {
            return createRepo(repoName); 
          }
          return Uni.createFrom().item(createClientStore(repoName));
    });
  }
  private Uni<TaskStore> deleteRepo(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var existingRepo = client.git(repoName).tenants().get();
    
    
    return existingRepo.onItem().transformToUni((repoResult) -> {
      if(repoResult.getStatus() != QueryEnvelopeStatus.OK) {
        throw new TaskException("REPO_GET_FOR_DELETE_FAIL",
            Collections.emptyList(),
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      final var repoId = repoResult.getRepo().getId();
      final var rev = repoResult.getRepo().getRev();
      final var docStore = createClientStore(repoName);
      
      return client.tenants().find().id(repoId).rev(rev).delete()
          .onItem().transform(junk -> docStore);
    });
  }
    
  private Uni<TaskStore> createRepo(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var newRepo = client.tenants().commit().name(repoName, StructureType.grim).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != CommitStatus.OK) {
        throw new TaskException("REPO_CREATE_FAIL",
            Collections.emptyList(),
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return createClientStore(repoName);
    });
  }
  
  private TaskStore createClientStore(String repoName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    return new TaskStoreImpl(ImmutableTaskStoreConfig.builder()
        .from(config)
        .tenantName(repoName)
        .build());
    
  }
}
