package io.digiexpress.thena.cockpit.client.spi.actions;

import io.digiexpress.thena.cockpit.client.api.CockpitQueryActions;
import io.digiexpress.thena.cockpit.client.spi.queries.CockpitQueryImpl;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class CockpitQueryActionsImpl implements CockpitQueryActions {
  private final CockpitDb startingState;
  private final String repoId;
  
  @Override
  public CockpitQuery cockpitQuery() {
    return new CockpitQueryImpl(startingState.withTenant(repoId));
  }
}
