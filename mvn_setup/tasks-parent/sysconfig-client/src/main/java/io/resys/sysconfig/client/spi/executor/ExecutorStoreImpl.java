package io.resys.sysconfig.client.spi.executor;

import java.util.List;

import io.resys.sysconfig.client.api.ExecutorClient.ExecutorClientConfig;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.ImmutableExecutorClientConfig;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutorStoreImpl implements ExecutorStore {
  
  private final TenantConfigClient tenantClient;
  private final ExecutorClientConfig config;

  @Override
  public SysConfigReleaseQuery queryReleases() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigInstanceQuery queryInstances() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SysConfigSessionQuery querySessions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DialobFormQuery queryForms() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<SysConfigSession> save(SysConfigSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ExecutorStore> withTenantConfig(String tenantConfigId) {
    return tenantClient.queryActiveTenantConfig().get(tenantConfigId)
        .onItem().transform(tenant -> withTenantConfig(tenantConfigId, tenant.getRepoConfigs()));
  }

  @Override
  public ExecutorStore withTenantConfig(String tenantConfigId, List<TenantRepoConfig> tenantConfig) {
    final var config = ImmutableExecutorClientConfig.builder().tenantConfigId(tenantConfigId).repoConfigs(tenantConfig).build();
    return new ExecutorStoreImpl(tenantClient, config);
  }

}
