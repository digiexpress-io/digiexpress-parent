package io.resys.thena.models.git.history;

import io.resys.thena.api.actions.HistoryActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HistoryActionsDefault implements HistoryActions {

  private final DbState state;
  private final String repoId;

  @Override
  public BlobHistoryQuery blobQuery() {
    return new BlobHistoryQueryImpl(state, repoId);
  }

}
