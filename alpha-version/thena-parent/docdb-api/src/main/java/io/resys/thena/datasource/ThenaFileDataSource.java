package io.resys.thena.datasource;

import io.resys.thena.api.entities.Tenant;

public interface ThenaFileDataSource extends ThenaDataSource {
  Tenant getTenant();
  TenantTableNames getTenantTableNames();
  ThenaFileDataSource withTenant(Tenant tenant);
  boolean isLocked(Throwable t);
}
