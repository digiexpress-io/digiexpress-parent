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
public class RepoQueryImpl implements TenantActions.RepoQuery {

  private final DbState state;
  private String id;
  private String rev;
  

  @Override
  public Multi<Tenant> findAll() {
   return state.project().findAll(); 
  }

  @Override
  public Uni<Tenant> get() {
    RepoAssert.notEmpty(id, () -> "Define id or name!");
    return state.project().getByNameOrId(id);
  }

  @Override
  public Uni<Tenant> delete() {
    return get().onItem().transformToUni(repo -> state.project().delete(repo));
  }
}
