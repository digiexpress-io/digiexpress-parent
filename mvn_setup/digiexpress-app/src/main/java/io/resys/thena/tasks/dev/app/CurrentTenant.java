package io.resys.thena.tasks.dev.app;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as=CurrentTenant.class)
public interface CurrentTenant {
  String tenantId();

  default String getTenantId() {
    return this.tenantId();
  }

  String tenantsStoreId();

  default String getTenantStoreId() {
    return this.tenantsStoreId();
  }

}
