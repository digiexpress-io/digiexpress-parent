package io.resys.thena.structures.git.objects;

import io.resys.thena.api.actions.BranchActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BranchActionsImpl implements BranchActions {
  private final DbState state;
  private final String repoId;

  @Override
  public BranchObjectsQuery branchQuery() {
    return new BranchObjectsQueryImpl(state, repoId);
  }
}
