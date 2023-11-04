package io.resys.thena.docdb.spi.git.commits;

import java.util.List;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.actions.CommitActions;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.spi.DbState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CommitActionsImpl implements CommitActions {

  private final DbState state;
  @Override
  public CommitBuilder commitBuilder() {
    return new CommitBuilderImpl(state);
  }

  @Override
  public Uni<List<Commit>> findAllCommits(String repoId) {
    return state.project().getByNameOrId(repoId).onItem()
        .transformToUni((Repo existing) -> {
          if(existing == null) {
            final var ex = RepoException.builder().notRepoWithName(repoId);
            log.error(ex.getText());
            throw new RepoException(ex.getText());
          }
          final var repoCtx = state.toGitState().withRepo(existing);      
          return repoCtx.query().commits().findAll().collect().asList();
        });
  }
  @Override
  public Uni<List<Tree>> findAllCommitTrees(String repoId) {
    return state.project().getByNameOrId(repoId).onItem()
        .transformToUni((Repo existing) -> {
          if(existing == null) {
            final var ex = RepoException.builder().notRepoWithName(repoId);
            log.error(ex.getText());
            throw new RepoException(ex.getText());
          }
          final var repoCtx = state.toGitState().withRepo(existing);      
          return repoCtx.query().trees().findAll().collect().asList();
        });
  }
  @Override
  public CommitQuery commitQuery() {
    return new CommitQueryImpl(state);
  }
}
