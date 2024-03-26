package io.resys.thena.models.org.actions;

import io.resys.thena.api.actions.OrgHistoryActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgHistoryActionsImpl implements OrgHistoryActions {
  private final DbState state;
  private final String repoId; 

}
