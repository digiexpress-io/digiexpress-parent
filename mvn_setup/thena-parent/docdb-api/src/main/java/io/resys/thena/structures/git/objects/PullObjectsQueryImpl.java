package io.resys.thena.structures.git.objects;

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

import java.util.ArrayList;
import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.actions.PullActions.MatchCriteria;
import io.resys.thena.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.ImmutablePullObject;
import io.resys.thena.api.entities.git.ImmutablePullObjects;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.ThenaGitObjects.PullObject;
import io.resys.thena.api.entities.git.ThenaGitObjects.PullObjects;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.git.GitState.GitRepo;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data @Accessors(fluent = true)
@RequiredArgsConstructor
public class PullObjectsQueryImpl implements PullObjectsQuery {
  private final DbState state;
  private final List<MatchCriteria> blobCriteria = new ArrayList<>();
  private final String projectName;
  private String branchNameOrCommitOrTag; //anyId;
  private List<String> docIds = new ArrayList<>();

  @Value.Immutable
  public static interface BlobAndTree {
    List<Blob> getBlob();
    String getTreeId();
  }
  
  @Override
  public PullObjectsQuery branchNameOrCommitOrTag(String branchNameOrCommitOrTag) {
    this.branchNameOrCommitOrTag = branchNameOrCommitOrTag;
    return this;
  }

  @Override
  public PullObjectsQueryImpl docId(String blobName) {
    this.docIds.add(blobName);
    return this;
  }
  @Override
  public PullObjectsQueryImpl docId(List<String> blobName) {
    this.docIds.addAll(blobName);
    return this;
  }
  @Override
  public PullObjectsQuery matchBy(List<MatchCriteria> blobCriteria) {
    this.blobCriteria.addAll(blobCriteria);
    return this;
  }
  
  @Override
  public PullObjectsQuery matchBy(MatchCriteria blobCriteria) {
    this.blobCriteria.add(blobCriteria);
    return this;
  }
  
  @Override
  public Uni<QueryEnvelope<PullObjects>> findAll() {
    RepoAssert.notEmpty(projectName, () -> "projectName is not defined!");
    RepoAssert.notEmpty(branchNameOrCommitOrTag, () -> "branchNameOrCommitOrTag is not defined!");
   // RepoAssert.isTrue(!blobName.isEmpty(), () -> "docId is not defined!");
    
    return state.project().getByNameOrId(projectName).onItem()
    .transformToUni((Tenant existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(projectName, log));
      }
      final var ctx = state.toGitState().withRepo(existing);
      return ObjectsUtils.findCommit(ctx, branchNameOrCommitOrTag).onItem().transformToUni(commit -> {
        if(commit == null) {
          return Uni.createFrom().item(QueryEnvelope.repoCommitNotFound(existing, branchNameOrCommitOrTag, log)); 
        }
        return getListState(existing, commit, ctx);
      });
    });
  }
  
  @Override
  public Uni<QueryEnvelope<PullObject>> get() {
    RepoAssert.notEmpty(projectName, () -> "projectName is not defined!");
    RepoAssert.notEmpty(branchNameOrCommitOrTag, () -> "branchNameOrCommitOrTag is not defined!");
    RepoAssert.isTrue(!docIds.isEmpty(), () -> "blobName is not defined!");
    
    return state.project().getByNameOrId(projectName).onItem()
    .transformToUni((Tenant existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(projectName, log));
      }
      final var ctx = state.toGitState().withRepo(existing);
      return ObjectsUtils.findCommit(ctx, branchNameOrCommitOrTag).onItem().transformToUni(commit -> {
          if(commit == null) {
            return Uni.createFrom().item(QueryEnvelope.repoCommitNotFound(existing, branchNameOrCommitOrTag, log));
          }
          return getState(existing, commit, ctx);
        });
    });
  }
  
  private Uni<QueryEnvelope<PullObject>> getState(Tenant repo, Commit commit, GitRepo ctx) {
    return getBlob(commit.getTree(), ctx, blobCriteria, docIds).onItem()
        .transformToUni(blobTree -> {
          
          if(blobTree.getBlob().size() != 1) {
            return Uni.createFrom().item(QueryEnvelope.repoBlobNotFound(repo, blobTree, commit, docIds, log));
          }
          
          return Uni.createFrom().item(ImmutableQueryEnvelope.<PullObject>builder()
            .repo(repo)
            .objects(ImmutablePullObject.builder()
                .repo(repo)
                .commit(commit)
                .blob(blobTree.getBlob().isEmpty() ? null : blobTree.getBlob().get(0))
                .build())
            .status(QueryEnvelopeStatus.OK)
            .build());
        });
  
  }
  
  private Uni<QueryEnvelope<PullObjects>> getListState(Tenant repo, Commit commit, GitRepo ctx) {
    return getBlob(commit.getTree(), ctx, blobCriteria, docIds).onItem()
        .transformToUni(blobAndTree -> {
          
          if(blobAndTree.getBlob().isEmpty()) {
            return Uni.createFrom().item(QueryEnvelope.repoBlobNotFound(repo, blobAndTree, commit, docIds, log));
          }
          return Uni.createFrom().item(ImmutableQueryEnvelope.<PullObjects>builder()
            .repo(repo)
            .objects(ImmutablePullObjects.builder()
                .repo(repo)
                .commit(commit)
                .blob(blobAndTree.getBlob())
                .build())
            .status(QueryEnvelopeStatus.OK)
            .build());
        });
  
  }

  private static Uni<BlobAndTree> getBlob(String treeId, GitRepo ctx, List<MatchCriteria> blobCriteria, List<String> docIds) {
    if(docIds.isEmpty()) {
      return ctx.query().blobs().findAll(treeId, blobCriteria).collect().asList()
          .onItem().transform(blobs -> ImmutableBlobAndTree.builder().blob(blobs).treeId(treeId).build());
      
    }

    return ctx.query().blobs().findAll(treeId, docIds, blobCriteria).collect().asList()
        .onItem().transform(blobs -> ImmutableBlobAndTree.builder().blob(blobs).treeId(treeId).build());    
  }
}
