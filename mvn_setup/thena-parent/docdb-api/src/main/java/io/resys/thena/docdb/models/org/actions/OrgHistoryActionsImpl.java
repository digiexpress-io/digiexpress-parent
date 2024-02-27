package io.resys.thena.docdb.models.org.actions;

import io.resys.thena.docdb.api.actions.OrgHistoryActions;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgHistoryActionsImpl implements OrgHistoryActions {
  private final DbState state;


}
