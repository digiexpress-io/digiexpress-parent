package io.resys.thena.spi;

import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.models.Repo;
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
  public Multi<Repo> findAll() {
   return state.project().findAll(); 
  }

  @Override
  public Uni<Repo> get() {
    RepoAssert.notEmpty(id, () -> "Define id or name!");
    return state.project().getByNameOrId(id);
  }

  @Override
  public Uni<Repo> delete() {
    return get().onItem().transformToUni(repo -> state.project().delete(repo));
  }
}
