package io.resys.thena.spi;

import io.resys.thena.api.actions.TenantActions;
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
}
