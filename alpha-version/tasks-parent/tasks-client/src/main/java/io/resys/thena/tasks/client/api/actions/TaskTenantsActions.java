package io.resys.thena.tasks.client.api.actions;

import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface TaskTenantsActions {
  TaskTenantQuery query();
  Uni<Tenant> getRepo();
}
