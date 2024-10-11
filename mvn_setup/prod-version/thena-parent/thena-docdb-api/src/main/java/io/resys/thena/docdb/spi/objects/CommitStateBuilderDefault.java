package io.resys.thena.docdb.spi.objects;

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

import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ImmutableCommitObjects;
import io.resys.thena.docdb.api.actions.ImmutableObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.CommitObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.CommitStateBuilder;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.ClientState.ClientRepoState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class CommitStateBuilderDefault implements CommitStateBuilder {
  private final ClientState state;
  private String repo;
  private String anyId; //refOrCommitOrTag
  private boolean blobs;
  
  @Override
  public CommitStateBuilder blobs() {
    this.blobs = true;
    return this;
  }
  @Override
  public Uni<ObjectsResult<CommitObjects>> build() {
    RepoAssert.notEmpty(repo, () -> "repo is not defined!");
    RepoAssert.notEmpty(anyId, () -> "refOrCommitOrTag is not defined!");
    
    return state.repos().getByNameOrId(repo).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(ImmutableObjectsResult
            .<CommitObjects>builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(RepoException.builder().notRepoWithName(repo))
            .build());
      }
      final var ctx = state.withRepo(existing);
      
      return getTagCommit(anyId, ctx)
        .onItem().transformToUni(tag -> {
          if(tag == null) {
            return getRefCommit(anyId, ctx);
          }
          return Uni.createFrom().item(tag);
        })
        .onItem().transformToUni(commitId -> {
          if(commitId == null) {
            return getCommit(anyId, ctx);
          }
          return getCommit(commitId, ctx);
        }).onItem().transformToUni(commit -> {
          if(commit == null) {
            return Uni.createFrom().item(ImmutableObjectsResult
                .<CommitObjects>builder()
                .status(ObjectsStatus.ERROR)
                .addMessages(noCommit(existing))
                .build()); 
          }
          return getState(existing, commit, ctx);
        });
    });
  }
  
  private Message noCommit(Repo repo) {
    return ImmutableMessage.builder()
      .text(new StringBuilder()
      .append("Repo with name: '").append(repo.getName()).append("'")
      .append(" does not contain: tag, ref or commit with id:")
      .append(" '").append(anyId).append("'")
      .toString())
      .build();
  }
  
  private Uni<ObjectsResult<CommitObjects>> getState(Repo repo, Commit commit, ClientRepoState ctx) {
    return getTree(commit, ctx).onItem()
        .transformToUni(tree -> {
          if(this.blobs) {
            return getBlobs(tree, ctx)
              .onItem().transform(blobs -> ImmutableObjectsResult.<CommitObjects>builder()
                .repo(repo)
                .objects(ImmutableCommitObjects.builder()
                    .repo(repo)
                    .tree(tree)
                    .blobs(blobs)
                    .commit(commit)
                    .build())
                .repo(repo)
                .status(ObjectsStatus.OK)
                .build());
          }
          
          return Uni.createFrom().item(ImmutableObjectsResult.<CommitObjects>builder()
            .repo(repo)
            .objects(ImmutableCommitObjects.builder()
                .repo(repo)
                .tree(tree)
                .commit(commit)
                .build())
            .status(ObjectsStatus.OK)
            .build());
        });
  
  }
  private Uni<String> getTagCommit(String tagName, ClientRepoState ctx) {
    return ctx.query().tags().name(tagName).get()
        .onItem().transform(tag -> tag == null ? null : tag.getCommit());
  }
  private Uni<String> getRefCommit(String refName, ClientRepoState ctx) {
    return ctx.query().refs().name(refName)
        .onItem().transform(ref -> ref == null ? null : ref.getCommit());
  }
  private Uni<Tree> getTree(Commit commit, ClientRepoState ctx) {
    return ctx.query().trees().id(commit.getTree());
  }
  private Uni<Commit> getCommit(String commit, ClientRepoState ctx) {
    return ctx.query().commits().id(commit);
  }
  private Uni<Map<String, Blob>> getBlobs(Tree tree, ClientRepoState ctx) {
    return ctx.query().blobs().find(tree)
        .collect().asList().onItem()
        .transform(blobs -> blobs.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)));
  }
}
