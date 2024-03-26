package io.resys.thena.models.git.objects;

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
import java.util.stream.Collectors;

import io.resys.thena.api.actions.BranchActions.BranchObjectsQuery;
import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.api.models.ImmutableBranchObjects;
import io.resys.thena.api.models.ImmutableQueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.Repo;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.ThenaGitObject.Branch;
import io.resys.thena.api.models.ThenaGitObjects.BranchObjects;
import io.resys.thena.models.git.GitState.GitRepo;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class BranchObjectsQueryImpl implements BranchObjectsQuery {
  private final DbState state;
  private final String repoId;
  private final List<MatchCriteria> blobCriteria = new ArrayList<>();
  private String branchName;
  private boolean docsIncluded;
  @Override public BranchObjectsQueryImpl matchBy(List<MatchCriteria> blobCriteria) { this.blobCriteria.addAll(blobCriteria); return this; }

  @Override
  public BranchObjectsQuery docsIncluded() {
    docsIncluded = true;
    return this;
  }
  @Override
  public Uni<QueryEnvelope<BranchObjects>> get() {
    RepoAssert.notEmpty(branchName, () -> "branchName is not defined!");
    
    return state.project().getByNameOrId(repoId).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      return getRef(existing, branchName, state.toGitState().withRepo(existing));
    });
  }
  
  private Uni<QueryEnvelope<BranchObjects>> getRef(Repo repo, String refName, GitRepo ctx) {

    return ctx.query().refs().name(refName).onItem()
        .transformToUni(ref -> {
          if(ref == null) {
            return ctx.query().refs().findAll().collect().asList().onItem().transform(allRefs -> 
              (QueryEnvelope<BranchObjects>) ImmutableQueryEnvelope
              .<BranchObjects>builder()
              .repo(repo)
              .status(QueryEnvelopeStatus.OK)
              .addMessages(RepoException.builder().noRepoRef(
                  repo.getName(), refName, 
                  allRefs.stream().map(e -> e.getName()).collect(Collectors.toList())))
              .build()
            );
          }
          return getState(repo, ref, ctx);
        });
  }
  
  private Uni<QueryEnvelope<BranchObjects>> getState(Repo repo, Branch ref, GitRepo ctx) {
    return ObjectsUtils.getCommit(ref.getCommit(), ctx).onItem()
        .transformToUni(commit -> ObjectsUtils.getTree(commit, ctx).onItem()
        .transformToUni(tree -> {
          if(this.docsIncluded) {
            return ObjectsUtils.getBlobs(tree, blobCriteria, ctx)
              .onItem().transform(blobs -> ImmutableQueryEnvelope.<BranchObjects>builder()
                .repo(repo)
                .objects(ImmutableBranchObjects.builder()
                    .repo(repo)
                    .ref(ref)
                    .tree(tree)
                    .blobs(blobs)
                    .commit(commit)
                    .build())
                .repo(repo)
                .status(QueryEnvelopeStatus.OK)
                .build());
          }
          
          return Uni.createFrom().item(ImmutableQueryEnvelope.<BranchObjects>builder()
            .repo(repo)
            .objects(ImmutableBranchObjects.builder()
                .repo(repo)
                .ref(ref)
                .tree(tree)
                .commit(commit)
                .build())
            .status(QueryEnvelopeStatus.OK)
            .build());
        }));
  }
}