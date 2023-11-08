package io.resys.thena.docdb.spi.doc;

import io.resys.thena.docdb.api.actions.DocQueryActions;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.doc.queries.DocObjectsQueryImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocQueryActionsImpl implements DocQueryActions {
  private final DbState state;
  
  @Override
  public DocObjectsQuery docQuery() {
    return new DocObjectsQueryImpl(state);
  }

  @Override
  public DocBranchObjectsQuery docBranchQuery() {
    // TODO Auto-generated method stub
    return null;
  }

}
