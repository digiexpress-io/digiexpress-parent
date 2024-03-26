package io.resys.thena.structures.git.objects;

import io.resys.thena.api.actions.GitBranchActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BranchActionsImpl implements GitBranchActions {
  private final DbState state;
  private final String repoId;

  @Override
  public BranchObjectsQuery branchQuery() {
    return new BranchObjectsQueryImpl(state, repoId);
  }
}
