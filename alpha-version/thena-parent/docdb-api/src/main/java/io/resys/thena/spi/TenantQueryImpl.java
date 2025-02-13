package io.resys.thena.spi;

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class TenantQueryImpl implements TenantActions.TenantQuery {

  private final DbState state;
  private String id;
  private String rev;

  @Override
  public Multi<Tenant> findAll() {
   return state.tenant().findAll(); 
  }

  @Override
  public Uni<Tenant> get() {
    RepoAssert.notEmpty(id, () -> "Define id or name!");
    return state.tenant().getByNameOrId(id);
  }

  @Override
  public Uni<Tenant> delete() {
    return get().onItem().transformToUni(repo -> state.tenant().delete(repo));
  }
}
