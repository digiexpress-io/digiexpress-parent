package io.resys.thena.datasource;

import io.resys.thena.api.entities.Tenant;

public class ThenaFileDataSourceImpl implements ThenaFileDataSource {

  private final Tenant tenant;
  private final TenantTableNames tenantTableNames;
    
  public ThenaFileDataSourceImpl(Tenant tenant, TenantTableNames tenantTableNames) {
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
  public TenantTableNames getTenantTableNames() {
    return tenantTableNames;
  }

  @Override
  public ThenaFileDataSource withTenant(Tenant tenant) {
    return new ThenaFileDataSourceImpl(tenant, tenantTableNames);
  }
}
