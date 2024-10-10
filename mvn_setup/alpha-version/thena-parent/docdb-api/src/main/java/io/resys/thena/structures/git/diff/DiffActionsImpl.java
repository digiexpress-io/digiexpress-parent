package io.resys.thena.structures.git.diff;

import java.util.function.Supplier;

import io.resys.thena.api.ThenaClient.GitTenantQuery;
import io.resys.thena.api.actions.GitCommitActions;
import io.resys.thena.api.actions.GitDiffActions;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DiffActionsImpl implements GitDiffActions {
  private final DbState state;
  private final GitPullActions objects;
  private final GitCommitActions commits;
  private final Supplier<GitTenantQuery> repos;
  
  @Override
  public DiffQuery diffQuery() {
    return new DiffQueryImpl(state, objects, commits, repos);
  }
}
