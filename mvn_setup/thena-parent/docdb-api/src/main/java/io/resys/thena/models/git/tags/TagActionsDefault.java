package io.resys.thena.models.git.tags;

import io.resys.thena.api.actions.TagActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagActionsDefault implements TagActions {
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