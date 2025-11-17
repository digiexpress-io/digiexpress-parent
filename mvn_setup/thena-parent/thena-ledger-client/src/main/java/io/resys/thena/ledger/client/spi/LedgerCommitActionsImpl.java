package io.resys.thena.ledger.client.spi;

import io.resys.thena.ledger.client.api.LedgerCommitActions;
import io.resys.thena.ledger.client.tables.BbDb;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LedgerCommitActionsImpl implements LedgerCommitActions {

  private final BbDb startingState;
  private final String repoId;
  
  
  @Override
  public CreateOneLedger createOneLedger() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ModifyOneLedger modifyOneLedger() {
    // TODO Auto-generated method stub
    return null;
  }
  
}
