package io.resys.sysconfig.client.spi.executor;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient;
import io.resys.sysconfig.client.api.SysConfigClient.SysConfigReleaseQuery;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.spi.executor.builder.SysConfigFillBuilderImpl;
import io.resys.sysconfig.client.spi.executor.builder.SysConfigInstanceBuilderImpl;
import io.resys.sysconfig.client.spi.executor.builder.SysConfigProcesssFillBuilderImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ExecutorClientImpl implements ExecutorClient {
  private final ExecutorStore store;
  private final AssetClient assets;
  
  @Override
  public SysConfigSessionQuery querySession() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigReleaseQuery queryReleases() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigSessionBuilder createSession() {
    return new SysConfigInstanceBuilderImpl(store, assets);
  }

  @Override
  public SysConfigFillBuilder fillInstance() {
    return new SysConfigFillBuilderImpl(assets.getConfig().getDialob().getConfig());
  }

  @Override
  public SysConfigProcesssFillBuilder processFillInstance() {
    return new SysConfigProcesssFillBuilderImpl(store, assets);
  }

  @Override
  public Uni<SysConfigSession> save(SysConfigSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<SysConfigRelease> save(SysConfigRelease release) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExecutorClient withTenantId(String tenantId) {
    final var store = this.store.withTenantId(tenantId);
    final var tenantAssets = this.assets.withTenantId(tenantId);
    return new ExecutorClientImpl(store, tenantAssets);
  }

}
