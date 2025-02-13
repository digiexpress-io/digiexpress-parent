package io.resys.thena.datasource;

import io.resys.thena.api.entities.Tenant;

public interface ThenaDataSource {
  Tenant getTenant();
  TenantTableNames getTenantTableNames();
  ThenaDataSource withTenant(Tenant tenant);
  boolean isLocked(Throwable t);
}
