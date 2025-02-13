package io.resys.thena.api.registry;

import io.resys.thena.datasource.TenantTableNames;

public interface ThenaRegistry extends TenantTableNames.WithTenant<ThenaRegistry> {
  TenantRegistry tenant();
  OrgRegistry org();
  GitRegistry git();
  DocRegistry doc();
  GrimRegistry grim();
}
