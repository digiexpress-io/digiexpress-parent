package io.resys.thena.structures.fs.actions;

import java.util.List;

/*-
 * #%L
 * thena-db-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.thena.api.actions.FsQueryActions;
import io.resys.thena.api.entities.fs.FsUniqueDirentLabel;
import io.resys.thena.spi.DbState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FsQueryActionsImpl implements FsQueryActions {
  private final DbState startingState;
  private final String repoId;
  
  @Override
  public DirentQuery direntQuery() {
    final var state = startingState.toFsState(repoId);
    return new FsDirentQueryImpl(state);
  }
  
  @Override
  public DirentLabelQuery direntLabelQuery() {
    return new DirentLabelQuery() {
      @Override
      public Uni<List<FsUniqueDirentLabel>> findAllUnique() {
        return startingState.toFsState(repoId).onItem().transformToUni(state -> state.query().direntLabels().findAllUnique());
      }
    };
  }
  


}
