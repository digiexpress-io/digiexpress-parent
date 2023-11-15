package io.resys.thena.projects.client.spi;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.actions.TenantConfigActions;
import io.resys.thena.projects.client.api.actions.RepositoryActions;
import io.resys.thena.projects.client.spi.actions.TenantsActionsImpl;
import io.resys.thena.projects.client.spi.actions.RepositoryActionsImpl;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectsClientImpl implements TenantConfigClient {
  private final DocumentStore ctx;
  
  @Override
  public TenantConfigActions tenantConfig() {
    return new TenantsActionsImpl(ctx);
  }
  @Override
  public RepositoryActions repo() {
    return new RepositoryActionsImpl(ctx);
  }
  public DocumentStore getCtx() {
    return ctx;
  }
}
