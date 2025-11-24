package io.resys.thena.ledger.client.api;

import java.util.List;
import java.util.Optional;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerTreeNode;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.resys.thena.ledger.client.entities.ProjectionDetail;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LedgerTreeNode_Default implements LedgerTreeNode {
  private final LedgerContainer ledger;

  @Override
  public BlackBook getBlackBook() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProjectionDetail> getBlackBookDetails() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<LedgerTreeNode> getTill(String blackBookType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<LedgerTreeNode> getPrevious() {
    // TODO Auto-generated method stub
    return Optional.empty();
  }

  @Override
  public Optional<LedgerTreeNode> getNext() {
    // TODO Auto-generated method stub
    return Optional.empty();
  }
}
