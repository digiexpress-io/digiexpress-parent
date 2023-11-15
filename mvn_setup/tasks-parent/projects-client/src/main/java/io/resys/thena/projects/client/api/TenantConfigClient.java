package io.resys.thena.projects.client.api;

import io.resys.thena.projects.client.api.actions.TenantConfigActions;
import io.resys.thena.projects.client.api.actions.RepositoryActions;

public interface TenantConfigClient {
  TenantConfigActions tenantConfig();
  RepositoryActions repo();
}
