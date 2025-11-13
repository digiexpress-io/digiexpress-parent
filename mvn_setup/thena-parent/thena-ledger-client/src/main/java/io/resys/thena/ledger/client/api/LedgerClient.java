package io.resys.thena.ledger.client.api;


import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;


public interface LedgerClient {
  TenantActions tenants();
  
  LedgerTenant withTenant();
  LedgerTenant withTenant(String tenantIdOrName);
  LedgerTenant withTenant(TenantCommitResult repo);
  LedgerTenant withTenant(Tenant repo);

  
  // workflow/task like structure
  interface LedgerTenant {
    String getTenantId();
    LedgerCommitActions commit();
    LedgerQueryActions find();
  }

}
