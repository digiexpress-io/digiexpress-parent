package io.resys.thena.datasource;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbCollections;

public interface ThenaDataSource {
  Tenant getTenant();
  DbCollections getTenantTableNames();
  ThenaDataSource withTenant(Tenant tenant);
  boolean isLocked(Throwable t);
}
