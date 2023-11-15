package io.resys.thena.projects.client.spi.actions;

import io.resys.thena.projects.client.api.actions.TenantConfigActions;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TenantsActionsImpl implements TenantConfigActions {
  private final DocumentStore ctx;

  @Override
  public CreateTenantConfigAction createTenantConfig(){
    return new CreateTenantConfigImpl(ctx);
  }

  @Override
  public UpdateTenantConfigAction updateTenantConfig() {
    return new UpdateTenantConfigImpl(ctx);
  }

  @Override
  public ActiveTenantConfigQuery queryActiveTenantConfig() {
    return new ActiveTenantConfigQueryImpl(ctx);
  }
}
