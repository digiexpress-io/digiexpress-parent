package io.resys.thena.spi;

import io.resys.thena.api.actions.TenantActions;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RepoActionsImpl implements TenantActions {
  private final DbState state;

  @Override
  public TenantQuery find() {
    return new RepoQueryImpl(state);
  }

  @Override
  public TenantBuilder commit() {
    return new RepoBuilderImpl(state);
  }
}
