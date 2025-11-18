package io.resys.thena.ledger.client.spi;

import io.resys.thena.ledger.client.api.LedgerCommitActions;
import io.resys.thena.ledger.client.spi.actions.CreateOneLedgerImpl;
import io.resys.thena.ledger.client.spi.actions.ModifyOneLedgerImpl;
import io.resys.thena.ledger.client.tables.BbDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LedgerCommitActionsImpl implements LedgerCommitActions {

  private final BbDb startingState;
  private final String repoId;
  
  
  @Override
  public CreateOneLedger createOneLedger() {
    return new CreateOneLedgerImpl(startingState, repoId);
  }
  @Override
  public ModifyOneLedger modifyOneLedger() {
    return new ModifyOneLedgerImpl(startingState, repoId);
  }
  
}
