package io.resys.sysconfig.client.spi.executor;

import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.spi.SysConfigStore;
import io.resys.sysconfig.client.spi.executor.visitors.GetDialobProgramFromReleaseVisitor;
import io.resys.sysconfig.client.spi.executor.visitors.GetFlowProgramFromReleaseVisitor;
import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import io.resys.sysconfig.client.spi.visitors.GetSysConfigReleaseByIdVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutorStoreImpl implements ExecutorStore {
  
  private final AssetClient assetClient;
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
  public ExecutorStore withTenantId(String tenantId) {
    final var ctx = this.ctx.withTenantId(tenantId);
    return new ExecutorStoreImpl(assetClient.withTenantId(tenantId), ctx);
  }
}
