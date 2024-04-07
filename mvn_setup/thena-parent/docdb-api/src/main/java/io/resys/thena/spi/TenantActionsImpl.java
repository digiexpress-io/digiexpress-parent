package io.resys.thena.spi;

import io.resys.thena.api.actions.TenantActions;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TenantActionsImpl implements TenantActions {
  private final DbState state;

  @Override
  public TenantQuery find() {
    return new TenantQueryImpl(state);
  }

  @Override
  public TenantBuilder commit() {
    return new TenantBuilderImpl(state);
  }

  @Override
  public Uni<Void> delete() {

    final var existingRepos = find().findAll();
    return existingRepos.onItem().transformToUni((repo) -> {
      
      final var repoId = repo.getId();
      final var rev = repo.getRev();
      
      return find().id(repoId).rev(rev).delete();
    })
    .concatenate().collect().asList()
    .onItem().transformToUni(junk -> state.tenant().delete());
    
  }
}
