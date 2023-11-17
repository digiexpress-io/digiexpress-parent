package io.resys.thena.projects.client.spi;

import java.util.Optional;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.support.RepoAssert;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.spi.actions.ActiveTenantConfigQueryImpl;
import io.resys.thena.projects.client.spi.actions.CreateTenantConfigImpl;
import io.resys.thena.projects.client.spi.actions.UpdateTenantConfigImpl;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectsClientImpl implements TenantConfigClient {
  private final DocumentStore ctx;
  

  public DocumentStore getCtx() {
    return ctx;
  }


  @Override
  public Uni<Repo> getRepo() {
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
    DocumentStore.DocumentRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      private String repoName;
      private TenantRepoConfigType type;
      
      @Override public Uni<TenantConfigClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public Uni<TenantConfigClient> create() { return repo.create().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public TenantConfigClient build() { return new ProjectsClientImpl(repo.build()); }
      @Override public Uni<TenantConfigClient> delete() { return repo.delete().onItem().transform(doc -> new ProjectsClientImpl(doc)); }
      @Override public Uni<TenantConfigClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new ProjectsClientImpl(ctx)); }
      @Override
      public RepositoryQuery repoName(String repoName, TenantRepoConfigType type) {
        this.repoName = repoName;
        this.type = type;
        repo.repoName(repoName).headName(MainBranch.HEAD_NAME);
        
        switch (type) {
        case CRM: { repo.repoType(RepoType.doc); break; }
        case DIALOB: { repo.repoType(RepoType.doc); break; }
        case TENANT: { repo.repoType(RepoType.doc); break; }

        case TASKS: { repo.repoType(RepoType.git); break; }
        case STENCIL: { repo.repoType(RepoType.git); break; }
        case WRENCH: { repo.repoType(RepoType.git); break; }
        }
        return this;
      }
      @Override
      public Uni<Optional<TenantConfig>> get(String tenantId) {
        RepoAssert.notEmpty(tenantId, () -> "tenantId must be defined!");
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        RepoAssert.isTrue(type == TenantRepoConfigType.TENANT, () -> "can only query from tenant config repo!");
        
        final var client = ctx.getConfig().getClient();
        return client.repo().projectsQuery().id(repoName)
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
}
