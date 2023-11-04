package io.resys.thena.docdb.spi.doc.repo;

import io.resys.thena.docdb.api.actions.DocAppendActions;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocAppendActionsImpl implements DocAppendActions {
  private final DbState state;

  @Override
  public DocAppendBuilder appendBuilder() {
    // TODO Auto-generated method stub
    return null;
  }
}
