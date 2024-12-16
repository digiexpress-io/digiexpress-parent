package io.resys.thena.structures.git.commits;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.List;

import io.resys.thena.api.actions.GitCommitActions;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.spi.DbState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CommitActionsImpl implements GitCommitActions {

  private final DbState state;
  private final String repoId;
  
  @Override
  public CommitBuilder commitBuilder() {
    return new CommitBuilderImpl(state, repoId);
  }

  @Override
  public Uni<List<Commit>> findAllCommits() {
    return state.tenant().getByNameOrId(repoId).onItem()
        .transformToUni((Tenant existing) -> {
          if(existing == null) {
            final var ex = RepoException.builder().notRepoWithName(repoId);
            log.error(ex.getText());
            throw new RepoException(ex.getText());
          }
          final var repoCtx = state.toGitState(existing);      
          return repoCtx.query().commits().findAll().collect().asList();
        });
  }
  @Override
  public Uni<List<Tree>> findAllCommitTrees() {
    return state.tenant().getByNameOrId(repoId).onItem()
        .transformToUni((Tenant existing) -> {
          if(existing == null) {
            final var ex = RepoException.builder().notRepoWithName(repoId);
            log.error(ex.getText());
            throw new RepoException(ex.getText());
          }
          final var repoCtx = state.toGitState(existing);      
          return repoCtx.query().trees().findAll().collect().asList();
        });
  }
  @Override
  public CommitQuery commitQuery() {
    return new CommitQueryImpl(state);
  }
}
