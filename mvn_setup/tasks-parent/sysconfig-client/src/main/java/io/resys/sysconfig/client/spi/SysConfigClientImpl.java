package io.resys.sysconfig.client.spi;

import java.util.List;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.spi.actions.CreateSysConfigActionImpl;
import io.resys.sysconfig.client.spi.actions.SysConfigQueryImpl;
import io.resys.sysconfig.client.spi.asset.exceptions.AssetClientException;
import io.resys.sysconfig.client.spi.store.SysConfigStore;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.support.ErrorMsg;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SysConfigClientImpl implements SysConfigClient {
  private final SysConfigStore ctx;
  private final AssetClient assets;
  private final ProjectClient tenantClient;
  public SysConfigStore getCtx() { return ctx; }

  @Override public Uni<Tenant> getRepo() { return ctx.getTenant(); }
  @Override public SysConfigClient withRepoId(String repoId) { return new SysConfigClientImpl(ctx.withTenantId(repoId), assets, tenantClient); }

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
    return new SysConfigClientImpl(this.ctx.withTenantId(sysConfig.get().getRepoId()), assets, tenantClient);
  }

  @Override
  public AssetClient getAssets() {
    return this.assets;
  }
}
