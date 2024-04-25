package io.resys.thena.projects.client.spi.actions;

import java.util.Collection;
import java.util.List;

import io.resys.thena.projects.client.api.TenantConfigClient.ActiveTenantConfigQuery;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.spi.store.DocumentStore;
import io.resys.thena.projects.client.spi.visitors.DeleteAllTenantsVisitor;
import io.resys.thena.projects.client.spi.visitors.FindAllTenantsVisitor;
import io.resys.thena.projects.client.spi.visitors.GetActiveTenantVisitor;
import io.resys.thena.projects.client.spi.visitors.GetTenantsByIdsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ActiveTenantConfigQueryImpl implements ActiveTenantConfigQuery {
  private final DocumentStore ctx;
  
  @Override
  public Uni<TenantConfig> get(String id) {
    return ctx.getConfig().accept(new GetActiveTenantVisitor(id));
  }
  
  @Override
  public Uni<List<TenantConfig>> findAll() {
    return ctx.getConfig().accept(new FindAllTenantsVisitor());
  }

  @Override
  public Uni<List<TenantConfig>> deleteAll() {
    return ctx.getConfig().accept(new DeleteAllTenantsVisitor())
        .onItem().transformToUni(unwrap -> unwrap);
  }
  
  @Override
  public Uni<List<TenantConfig>> findByIds(Collection<String> taskIds) {
    return ctx.getConfig().accept(new GetTenantsByIdsVisitor(taskIds));
  }
}
