package io.resys.sysconfig.client.spi.executor;

import java.util.List;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient;
import io.resys.sysconfig.client.api.SysConfigClient.SysConfigReleaseQuery;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.spi.executor.builder.SysConfigInstanceBuilderImpl;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigProcesssFillBuilder processFillInstance() {
    // TODO Auto-generated method stub
    return null;
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
  public Uni<ExecutorClient> withTenantConfig(String tenantConfigId) {
    return this.assets.withTenantConfig(tenantConfigId)
        .onItem().transform(tenantAssets -> {
          final var store = this.store.withTenantConfig(tenantAssets.getConfig().getTenantConfigId(), tenantAssets.getConfig().getRepoConfigs());
          return new ExecutorClientImpl(store, tenantAssets);
        });
  }

  @Override
  public ExecutorClient withTenantConfig(String tenantConfigId, List<TenantRepoConfig> tenantConfig) {
    final var store = this.store.withTenantConfig(tenantConfigId, tenantConfig);
    final var tenantAssets = this.assets.withTenantConfig(tenantConfigId, tenantConfig);
    return new ExecutorClientImpl(store, tenantAssets);
  }

}
