package io.resys.thena.projects.client.api.actions;

import io.resys.thena.projects.client.api.TenantConfigClient.ActiveTenantConfigQuery;
import io.resys.thena.projects.client.api.TenantConfigClient.CreateTenantConfigAction;
import io.resys.thena.projects.client.api.TenantConfigClient.UpdateTenantConfigAction;


public interface TenantConfigActions {

  CreateTenantConfigAction createTenantConfig();
  UpdateTenantConfigAction updateTenantConfig();
  ActiveTenantConfigQuery queryActiveTenantConfig();


}
