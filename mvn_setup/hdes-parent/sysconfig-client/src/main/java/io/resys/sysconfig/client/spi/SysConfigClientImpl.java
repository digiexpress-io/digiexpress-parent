package io.resys.sysconfig.client.spi;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.spi.actions.CreateSysConfigActionImpl;
import io.resys.sysconfig.client.spi.actions.SysConfigQueryImpl;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SysConfigClientImpl implements SysConfigClient {
  private final SysConfigStore ctx;
  private final AssetClient assets;
  public SysConfigStore getCtx() { return ctx; }

  @Override public Uni<Tenant> getRepo() { return ctx.getTenant(); }
  
  @Override 
  public SysConfigClient withTenantId(String repoId) { 
    return new SysConfigClientImpl(ctx.withTenantId(repoId), assets.withTenantId(repoId)); 
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
  public AssetClient getAssets() {
    return this.assets;
  }
}
