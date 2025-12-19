package io.digiexpress.thena.cockpit.client.spi.actions;

import io.digiexpress.thena.cockpit.client.api.CockpitCommitActions;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CockpitCommitActionsImpl implements CockpitCommitActions {
  private final CockpitDb state;
  private final String tenantId;
  
  @Override
  public CreateOneCockpitConfig createOneCockpitConfig() {
    return new CreateOneCockpitConfigImpl(state, tenantId);
  }
  @Override
  public ModifyOneCockpitConfig modifyOneCockpitConfig() {
    return new ModifyOneCockpitConfigImpl(state, tenantId);
  }
}
