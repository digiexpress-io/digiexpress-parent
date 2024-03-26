package io.resys.thena.structures.git.tags;

import io.resys.thena.api.actions.GitTagActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagActionsDefault implements GitTagActions {
  private final DbState state;
  private final String repoId;
  
  @Override
  public TagBuilder tagBuilder() {
    return new CreateTagBuilder(state, repoId);
  }

  @Override
  public TagQuery tagQuery() {
    return new AnyTagQuery(state, repoId);
  }
}
