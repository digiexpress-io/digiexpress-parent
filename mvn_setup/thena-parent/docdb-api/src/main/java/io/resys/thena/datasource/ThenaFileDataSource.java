package io.resys.thena.datasource;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbCollections;

public interface ThenaFileDataSource extends ThenaDataSource {
  Tenant getTenant();
  DbCollections getTenantTableNames();
  ThenaFileDataSource withTenant(Tenant tenant);
  boolean isLocked(Throwable t);
}
