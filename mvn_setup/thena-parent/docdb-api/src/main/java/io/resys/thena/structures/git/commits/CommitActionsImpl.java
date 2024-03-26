package io.resys.thena.structures.git.commits;

import java.util.List;

import io.resys.thena.api.actions.CommitActions;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.ThenaGitObject.Commit;
import io.resys.thena.api.entities.git.ThenaGitObject.Tree;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.spi.DbState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CommitActionsImpl implements CommitActions {

  private final DbState state;
  private final String repoId;
  
  @Override
  public CommitBuilder commitBuilder() {
    return new CommitBuilderImpl(state, repoId);
  }

  @Override
  public Uni<List<Commit>> findAllCommits() {
    return state.project().getByNameOrId(repoId).onItem()
        .transformToUni((Tenant existing) -> {
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
  public Uni<List<Tree>> findAllCommitTrees() {
    return state.project().getByNameOrId(repoId).onItem()
        .transformToUni((Tenant existing) -> {
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
