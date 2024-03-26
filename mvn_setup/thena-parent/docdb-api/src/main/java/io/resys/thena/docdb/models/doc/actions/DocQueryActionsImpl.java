package io.resys.thena.docdb.models.doc.actions;

import io.resys.thena.docdb.api.actions.DocQueryActions;
import io.resys.thena.docdb.models.doc.queries.DocObjectsQueryImpl;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocQueryActionsImpl implements DocQueryActions {
  private final DbState state;
  private final String repoId;
  
  @Override
  public DocObjectsQuery docQuery() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    return new DocObjectsQueryImpl(state, repoId);
  }
}
