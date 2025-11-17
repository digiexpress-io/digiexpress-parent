package io.resys.thena.ledger.client.spi;

import io.resys.thena.ledger.client.api.LedgerQueryActions;
import io.resys.thena.ledger.client.tables.BbDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LedgerQueryActionsImpl implements LedgerQueryActions {
  private final BbDb startingState;
  private final String repoId;
  
  @Override
  public LedgerQuery ledgerQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ReferenceNumberQuery referenceNumberQuery() {
    // TODO Auto-generated method stub
    return null;
  }
}
