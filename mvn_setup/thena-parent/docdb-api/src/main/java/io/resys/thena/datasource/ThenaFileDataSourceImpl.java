package io.resys.thena.datasource;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbCollections;

public class ThenaFileDataSourceImpl implements ThenaFileDataSource {

  private final Tenant tenant;
  private final DbCollections tenantTableNames;
    
  public ThenaFileDataSourceImpl(Tenant tenant, DbCollections tenantTableNames) {
    super();
    this.tenant = tenant;
    this.tenantTableNames = tenantTableNames.toRepo(tenant);
  }

  public boolean isLocked(Throwable t) {
    return false;
  }

  @Override
  public Tenant getTenant() {
    return tenant;
  }

  @Override
  public DbCollections getTenantTableNames() {
    return tenantTableNames;
  }

  @Override
  public ThenaFileDataSource withTenant(Tenant tenant) {
    return new ThenaFileDataSourceImpl(tenant, tenantTableNames);
  }
}
