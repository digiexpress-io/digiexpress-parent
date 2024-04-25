package io.resys.thena.projects.client.spi;

import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.spi.actions.ActiveTenantConfigQueryImpl;
import io.resys.thena.projects.client.spi.actions.CreateTenantConfigImpl;
import io.resys.thena.projects.client.spi.actions.UpdateTenantConfigImpl;
import io.resys.thena.projects.client.spi.store.ProjectStore;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectsClientImpl implements ProjectClient {
  private final ProjectStore ctx;
  

  public ProjectStore getCtx() {
    return ctx;
  }


  @Override
  public Uni<Tenant> getRepo() {
    return ctx.getRepo();
  }
  
  @Override
  public CreateTenantConfigAction createTenantConfig(){
    return new CreateTenantConfigImpl(ctx);
  }

  @Override
  public UpdateTenantConfigAction updateTenantConfig() {
    return new UpdateTenantConfigImpl(ctx);
  }

  @Override
  public ActiveTenantConfigQuery queryActiveTenantConfig() {
    return new ActiveTenantConfigQueryImpl(ctx);
  }
  
  @Override
  public RepositoryQuery query() {
    ProjectStore.DocumentRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      private String repoName;
      private TenantRepoConfigType type;
      
      @Override public Uni<ProjectClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public Uni<ProjectClient> create() { return repo.create().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public ProjectClient build() { return new ProjectsClientImpl(repo.build()); }
      @Override public Uni<ProjectClient> delete() { return repo.delete().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public Uni<ProjectClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new ProjectsClientImpl(ctx)); }
      @Override
      public RepositoryQuery repoName(String repoName, TenantRepoConfigType type) {
        this.repoName = repoName;
        this.type = type;
        repo.repoName(type.name()).externalId(repoName);
        
        switch (type) {
        case CRM: { repo.repoType(StructureType.doc); break; }
        case TENANT: { repo.repoType(StructureType.doc); break; }
        case SYS_CONFIG: { repo.repoType(StructureType.doc); break; }
        case PERMISSIONS: { repo.repoType(StructureType.org); break; }
        case USER_PROFILE: { repo.repoType(StructureType.doc); break; }

        case DIALOB: { repo.repoType(StructureType.git); break; }
        case TASKS: { repo.repoType(StructureType.grim); break; }
        case STENCIL: { repo.repoType(StructureType.git); break; }
        case WRENCH: { repo.repoType(StructureType.git); break; }
        }
        return this;
      }
      @Override
      public Uni<Optional<TenantConfig>> get(String tenantId) {
        RepoAssert.notEmpty(tenantId, () -> "tenantId must be defined!");
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        RepoAssert.isTrue(type == TenantRepoConfigType.TENANT, () -> "can only query from tenant config repo!");
        
        final var client = ctx.getConfig().getClient();
        return client.tenants().find().id(repoName)
            .get().onItem().transformToUni(existing -> {
              if(existing == null) {
                final Optional<TenantConfig> result = Optional.empty();
                return Uni.createFrom().item(result);
              }
              
              return new ProjectsClientImpl(repo.build())
                  .queryActiveTenantConfig().get(tenantId)
                  .onItem().transform(config -> Optional.of(config));
            });
        
      }
    };
  }


  @Override
  public ProjectClient withRepoId(String repoId) {
    return new ProjectsClientImpl(ctx.withRepoId(repoId));
  }
}
