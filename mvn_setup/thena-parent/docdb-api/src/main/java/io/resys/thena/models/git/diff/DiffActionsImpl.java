package io.resys.thena.models.git.diff;

import java.util.function.Supplier;

import io.resys.thena.api.ThenaClient.GitStructuredTenant.GitRepoQuery;
import io.resys.thena.api.actions.CommitActions;
import io.resys.thena.api.actions.DiffActions;
import io.resys.thena.api.actions.PullActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DiffActionsImpl implements DiffActions {
  private final DbState state;
  private final PullActions objects;
  private final CommitActions commits;
  private final Supplier<GitRepoQuery> repos;
  
  @Override
  public DiffQuery diffQuery() {
    return new DiffQueryImpl(state, objects, commits, repos);
  }
}
