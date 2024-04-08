package io.resys.thena.tasks.dev.app.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as=CurrentTenant.class)
public interface CurrentTenant {
  String tenantId();
  String tenantsStoreId();
  
  default String getTenantId() {
    return this.tenantId();
  }
  default String getTenantStoreId() {
    return this.tenantsStoreId();
  }
}
