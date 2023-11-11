package io.resys.thena.docdb.models.git.commits;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.actions.CommitActions.CommitQuery;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableCommitObjects;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObjects.CommitObjects;
import io.resys.thena.docdb.models.git.GitDbState.GitRepo;
import io.resys.thena.docdb.models.git.objects.ObjectsUtils;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class CommitQueryImpl implements CommitQuery {
  private final DbState state;
  private final List<MatchCriteria> blobCriteria = new ArrayList<>();
  private String projectName;
  private String branchNameOrCommitOrTag; //refOrCommitOrTag
  private boolean docsIncluded;
  
  @Override public CommitQueryImpl matchBy(List<MatchCriteria> blobCriteria) { this.blobCriteria.addAll(blobCriteria); return this; }
  @Override public CommitQuery docsIncluded() { this.docsIncluded = true; return this; }
  
  @Override
  public Uni<QueryEnvelope<CommitObjects>> get() {
    RepoAssert.notEmpty(projectName, () -> "projectName is not defined!");
    RepoAssert.notEmpty(branchNameOrCommitOrTag, () -> "branchNameOrCommitOrTag is not defined!");
    
    return state.project().getByNameOrId(projectName).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var error = RepoException.builder().notRepoWithName(projectName);
        log.error(error.getText());
        return Uni.createFrom().item(ImmutableQueryEnvelope
            .<CommitObjects>builder()
            .status(QueryEnvelopeStatus.ERROR)
            .addMessages(error)
            .build());
      }
      final var ctx = state.toGitState().withRepo(existing);
      
      return ObjectsUtils.findCommit(ctx, branchNameOrCommitOrTag)
        .onItem().transformToUni(commit -> {
          if(commit == null) {
            final var error = RepoException.builder().noCommit(existing, branchNameOrCommitOrTag);
            log.error(error.getText());
            return Uni.createFrom().item(ImmutableQueryEnvelope
                .<CommitObjects>builder()
                .status(QueryEnvelopeStatus.ERROR)
                .addMessages(error)
                .build()); 
          }
          return getState(existing, commit, ctx);
        });
    });
  }
  
  
  private Uni<QueryEnvelope<CommitObjects>> getState(Repo repo, Commit commit, GitRepo ctx) {
    return ObjectsUtils.getTree(commit, ctx).onItem()
    .transformToUni(tree -> {
      if(this.docsIncluded) {
        return getBlobs(tree, ctx, blobCriteria)
          .onItem().transform(blobs -> ImmutableQueryEnvelope.<CommitObjects>builder()
            .repo(repo)
            .objects(blobs
                .repo(repo)
                .tree(tree)
                .commit(commit)
                .build())
            .repo(repo)
            .status(QueryEnvelopeStatus.OK)
            .build());
      }
      
      return Uni.createFrom().item(ImmutableQueryEnvelope.<CommitObjects>builder()
        .repo(repo)
        .objects(ImmutableCommitObjects.builder()
            .repo(repo)
            .tree(tree)
            .commit(commit)
            .build())
        .status(QueryEnvelopeStatus.OK)
        .build());
    });
  }
  
  private static Uni<ImmutableCommitObjects.Builder> getBlobs(Tree tree, GitRepo ctx, List<MatchCriteria> blobCriteria) {
    return ctx.query().blobs().findAll(tree.getId(), blobCriteria)
        .collect().asList().onItem()
        .transform(blobs -> {
          final var objects = ImmutableCommitObjects.builder();
          blobs.forEach(blob -> objects.putBlobs(blob.getId(), blob));
          return objects;
        });
  }
}
