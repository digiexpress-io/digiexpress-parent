package io.resys.thena.structures.git.objects;

import io.resys.thena.api.actions.PullActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ObjectsActionsImpl implements PullActions {
  private final DbState state;
  private final String repoId;
  @Override
  public PullObjectsQuery pullQuery() {
    return new PullObjectsQueryImpl(state, repoId);
  }
}
