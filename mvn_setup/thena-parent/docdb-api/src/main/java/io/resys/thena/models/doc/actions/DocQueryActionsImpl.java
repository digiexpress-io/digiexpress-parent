package io.resys.thena.models.doc.actions;

import io.resys.thena.api.actions.DocQueryActions;
import io.resys.thena.models.doc.queries.DocObjectsQueryImpl;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
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
