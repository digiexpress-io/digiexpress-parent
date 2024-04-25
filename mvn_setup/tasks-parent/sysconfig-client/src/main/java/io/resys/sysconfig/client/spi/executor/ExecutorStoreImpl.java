package io.resys.sysconfig.client.spi.executor;

import java.util.List;

import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient.ExecutorClientConfig;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.ImmutableExecutorClientConfig;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.spi.executor.visitors.GetDialobProgramFromReleaseVisitor;
import io.resys.sysconfig.client.spi.executor.visitors.GetFlowProgramFromReleaseVisitor;
import io.resys.sysconfig.client.spi.store.SysConfigStore;
import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import io.resys.sysconfig.client.spi.visitors.GetSysConfigReleaseByIdVisitor;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutorStoreImpl implements ExecutorStore {
  
  private final ProjectClient tenantClient;
  private final AssetClient assetClient;
  private final ExecutorClientConfig config;
  private final SysConfigStore ctx;
  
  @Override
  public SysConfigReleaseQuery queryReleases() {
    return new SysConfigReleaseQuery() {
      @Override
      public Uni<SysConfigRelease> get(String releaseId) {
        return ctx.getConfig().accept(new GetSysConfigReleaseByIdVisitor(releaseId));
      }
    };
  }
  @Override
  public WrenchFlowQuery queryFlows() {
    return new WrenchFlowQuery() {
      private String releaseId;
      @Override public WrenchFlowQuery releaseId(String releaseId) { this.releaseId = releaseId; return this; }
      @Override
      public Uni<WrenchFlow> get(String flowId) {
        SysConfigAssert.notEmpty(releaseId, () -> "releaseId can't be empty!");
        SysConfigAssert.notEmpty(flowId, () -> "flowId can't be empty!");
        return queryReleases().get(releaseId).onItem().transform(release -> new GetFlowProgramFromReleaseVisitor(assetClient, flowId).visit(release));
      }
    };
  }
  
  @Override
  public DialobFormQuery queryForms() {
    return new DialobFormQuery() {
      private String releaseId;
      @Override public DialobFormQuery releaseId(String releaseId) { this.releaseId = releaseId; return this; }
      @Override
      public Uni<ProgramWrapper> get(String formId) {
        SysConfigAssert.notEmpty(releaseId, () -> "releaseId can't be empty!");
        SysConfigAssert.notEmpty(formId, () -> "formId can't be empty!");
        return queryReleases().get(releaseId).onItem().transform(release -> new GetDialobProgramFromReleaseVisitor(assetClient, formId).visit(release));
      }
    };
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
    final var sysConfig = tenantConfig.stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.SYS_CONFIG).findFirst();
    final var config = ImmutableExecutorClientConfig.builder().tenantConfigId(tenantConfigId).repoConfigs(tenantConfig).build();
    final var ctx = this.ctx.withRepoId(sysConfig.get().getRepoId());
    return new ExecutorStoreImpl(tenantClient, assetClient.withTenantConfig(tenantConfigId, tenantConfig), config, ctx);
  }
}
