package io.resys.thena.ledger.client.spi;

import io.resys.thena.ledger.client.api.LedgerQueryActions;
import io.resys.thena.ledger.client.spi.queries.LedgerQueryImpl;
import io.resys.thena.ledger.client.tables.BbDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LedgerQueryActionsImpl implements LedgerQueryActions {
  private final BbDb startingState;
  private final String repoId;
  
  @Override
  public LedgerQuery ledgerQuery() {
    return new LedgerQueryImpl(startingState.withTenant(repoId));
  }
}
