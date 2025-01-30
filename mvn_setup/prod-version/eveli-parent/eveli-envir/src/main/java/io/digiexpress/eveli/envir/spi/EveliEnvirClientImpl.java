package io.digiexpress.eveli.envir.spi;

import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirTenantQuery;
import io.digiexpress.eveli.envir.spi.actions.CreateOneDeploymentImpl;
import io.digiexpress.eveli.envir.spi.actions.DeploymentQueryImpl;
import io.digiexpress.eveli.envir.spi.actions.EveliDeploymentCompilerImpl;
import io.digiexpress.eveli.envir.spi.actions.ModifyOneDeploymentImpl;
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
    return new CreateOneDeploymentImpl(ctx);
  }
  @Override
  public ModifyOneDeployment modifyOneDeployment() {
    return new ModifyOneDeploymentImpl(ctx);
  }
  @Override
  public DeploymentQuery deploymentQuery() {
    return new DeploymentQueryImpl(ctx);
  }
  @Override
  public EveliRuntimeQuery runtimeQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public EveliDeploymentCompiler deploymentCompiler() {
    return new EveliDeploymentCompilerImpl(ctx);
  }

}
