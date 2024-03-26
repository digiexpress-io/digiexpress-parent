package io.resys.sysconfig.client.spi;

import java.util.List;
import java.util.Optional;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.spi.actions.CreateSysConfigActionImpl;
import io.resys.sysconfig.client.spi.actions.SysConfigQueryImpl;
import io.resys.sysconfig.client.spi.asset.exceptions.AssetClientException;
import io.resys.sysconfig.client.spi.store.DocumentStore;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.resys.thena.support.ErrorMsg;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SysConfigClientImpl implements SysConfigClient {
  private final DocumentStore ctx;
  private final AssetClient assets;
  private final TenantConfigClient tenantClient;
  public DocumentStore getCtx() { return ctx; }

  @Override public Uni<Tenant> getRepo() { return ctx.getRepo(); }
  @Override public SysConfigClient withRepoId(String repoId) { return new SysConfigClientImpl(ctx.withRepoId(repoId), assets, tenantClient); }

  @Override
  public RepositoryQuery repoQuery() {
    DocumentStore.DocumentRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      @Override public Uni<SysConfigClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new SysConfigClientImpl(doc, assets, tenantClient)); }
      @Override public Uni<SysConfigClient> create() { return repo.create().onItem().transform(doc -> new SysConfigClientImpl(doc, assets, tenantClient)); }
      @Override public SysConfigClient build() { return new SysConfigClientImpl(repo.build(), assets, tenantClient); }
      @Override public Uni<SysConfigClient> delete() { return repo.delete().onItem().transform(doc -> new SysConfigClientImpl(doc, assets, tenantClient)); }
      @Override public Uni<SysConfigClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new SysConfigClientImpl(ctx, assets, tenantClient)); }
      @Override
      public RepositoryQuery repoName(String repoName) {
        repo.repoName(repoName).headName(MainBranch.HEAD_NAME);
        return this;
      }
      @Override
      public Uni<Optional<SysConfigClient>> get(String repoName) {
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        
        final var client = ctx.getConfig().getClient();
        return client.tenants().find().id(repoName)
            .get().onItem().transform(existing -> {
              if(existing == null) {
                final Optional<SysConfigClient> result = Optional.empty();
                return result;
              }
              return Optional.of(new SysConfigClientImpl(repo.build(), assets, tenantClient));
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
  
  @Override
  public Uni<SysConfigClient> withTenantConfig(String tenantConfigId) {
    return tenantClient.queryActiveTenantConfig().get(tenantConfigId)
        .onItem().transform(tenant -> withTenantConfig(tenantConfigId, tenant.getRepoConfigs()));
  }
  @Override
  public SysConfigClientImpl withTenantConfig(String tenantConfigId, List<TenantRepoConfig> tenantConfig) {
    final var dialob = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.DIALOB).findFirst();
    final var wrench = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.WRENCH).findFirst();
    final var stencil = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.STENCIL).findFirst();
    final var sysConfig = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.SYS_CONFIG).findFirst();
    
    if(dialob.isEmpty() || wrench.isEmpty() || stencil.isEmpty() || sysConfig.isEmpty()) {
      throw new AssetClientException(ErrorMsg.builder()
          .withCode("TENANT_CONFIG_INCOMPLETE")
          .withProps(JsonObject.of(
              "dialobOk", dialob.isPresent(),
              "wrenchOk", wrench.isPresent(),
              "stencilOk", stencil.isPresent(),
              "sysConfigOk", sysConfig.isPresent()
              ))
          .withMessage("Can't get asset configuration because it's incomplete!")
          .toString());
    }
    
    final var assets = this.assets.withTenantConfig(tenantConfigId, tenantConfig);
    return new SysConfigClientImpl(this.ctx.withRepoId(sysConfig.get().getRepoId()), assets, tenantClient);
  }

  @Override
  public AssetClient getAssets() {
    return this.assets;
  }
}
