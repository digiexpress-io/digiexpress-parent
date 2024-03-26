package io.resys.thena.spi;

import io.resys.thena.api.actions.TenantActions;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RepoActionsImpl implements TenantActions {
  private final DbState state;

  @Override
  public RepoQuery find() {
    return new RepoQueryImpl(state);
  }

  @Override
  public RepoBuilder commit() {
    return new RepoBuilderImpl(state);
  }
}
