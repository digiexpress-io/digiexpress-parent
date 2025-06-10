package io.resys.thena.grim.api;

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimProjectObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface GrimClient {  
  TenantActions tenants();

  GrimStructuredTenant grim(String tenantIdOrName);
  GrimStructuredTenant grim(TenantCommitResult repo);
  GrimStructuredTenant grim(Tenant repo);

  
  // workflow/task like structure
  interface GrimStructuredTenant {
    String getTenantId();
    GrimCommitActions commit();
    GrimQueryActions find();
    GrimProjectQuery tenants();
  }
  // build world state
  interface GrimProjectQuery {
    Uni<QueryEnvelope<GrimProjectObjects>> get();
  }
}
