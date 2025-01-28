package io.digiexpress.eveli.envir.spi;

import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirTenantQuery;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EveliEnvirClientImpl implements EveliEnvirClient {
  private final EveliEnvirStore ctx;
  
  public EveliEnvirStore getCtx() { return ctx; }
  
  public EveliEnvirClientImpl withTenant(String tenantId) {
    return new EveliEnvirClientImpl(ctx.withTenantId(tenantId));
  }
  public Uni<Tenant> getTenant() {
    return ctx.getTenant();
  }
  public EveliEnvirTenantQuery tenantQuery() {
    return new EveliEnvirTenantQueryImpl(ctx);
  }

  @Override
  public CreateOneDeployment createOneDeployment() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ModifyOneDeployment modifyOneDeployment() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EveliRuntimeQuery runtimeQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeploymentQuery deploymentQuery() {
    // TODO Auto-generated method stub
    return null;
  }
}
