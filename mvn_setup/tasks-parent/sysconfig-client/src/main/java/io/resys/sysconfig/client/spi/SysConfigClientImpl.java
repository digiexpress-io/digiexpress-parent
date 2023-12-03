package io.resys.sysconfig.client.spi;

import java.util.Optional;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.spi.actions.CreateSysConfigActionImpl;
import io.resys.sysconfig.client.spi.actions.SysConfigQueryImpl;
import io.resys.sysconfig.client.spi.store.DocumentStore;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.support.RepoAssert;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SysConfigClientImpl implements SysConfigClient {
  private final DocumentStore ctx;
  private final AssetClient assets;
  public DocumentStore getCtx() { return ctx; }

  @Override public Uni<Repo> getRepo() { return ctx.getRepo(); }
  @Override public SysConfigClient withRepoId(String repoId) { return new SysConfigClientImpl(ctx.withRepoId(repoId), assets); }
  
  @Override
  public RepositoryQuery repoQuery() {
    DocumentStore.DocumentRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      @Override public Uni<SysConfigClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new SysConfigClientImpl(doc, assets)); }
      @Override public Uni<SysConfigClient> create() { return repo.create().onItem().transform(doc -> new SysConfigClientImpl(doc, assets)); }
      @Override public SysConfigClient build() { return new SysConfigClientImpl(repo.build(), assets); }
      @Override public Uni<SysConfigClient> delete() { return repo.delete().onItem().transform(doc -> new SysConfigClientImpl(doc, assets)); }
      @Override public Uni<SysConfigClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new SysConfigClientImpl(ctx, assets)); }
      @Override
      public RepositoryQuery repoName(String repoName) {
        repo.repoName(repoName).headName(MainBranch.HEAD_NAME);
        return this;
      }
      @Override
      public Uni<Optional<SysConfigClient>> get(String repoName) {
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        
        final var client = ctx.getConfig().getClient();
        return client.repo().projectsQuery().id(repoName)
            .get().onItem().transform(existing -> {
              if(existing == null) {
                final Optional<SysConfigClient> result = Optional.empty();
                return result;
              }
              return Optional.of(new SysConfigClientImpl(repo.build(), assets));
            });
        
      }
    };
  }

  @Override
  public CreateSysConfigAction createConfig() {
    return new CreateSysConfigActionImpl(ctx, assets);
  }

  @Override
  public UpdateSysConfigAction updateConfig() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CreateSysConfigDeploymentAction createDeployment() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigReleaseQuery releaseQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigQuery configQuery() {
    return new SysConfigQueryImpl(ctx);
  }
  
}
