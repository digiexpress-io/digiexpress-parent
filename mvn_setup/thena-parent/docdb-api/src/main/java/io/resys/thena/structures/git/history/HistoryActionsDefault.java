package io.resys.thena.structures.git.history;

import io.resys.thena.api.actions.GitHistoryActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HistoryActionsDefault implements GitHistoryActions {

  private final DbState state;
  private final String repoId;

  @Override
  public BlobHistoryQuery blobQuery() {
    return new BlobHistoryQueryImpl(state, repoId);
  }

}
