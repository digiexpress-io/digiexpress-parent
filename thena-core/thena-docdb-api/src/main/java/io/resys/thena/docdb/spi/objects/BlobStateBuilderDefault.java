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

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.ImmutableBlobObjects;
import io.resys.thena.docdb.api.actions.ImmutableObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobStateBuilder;
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

public class BlobStateBuilderDefault implements BlobStateBuilder {
  private final ClientState state;
  private String repoName;
  private String refOrCommitOrTag;
  private String blobName;
  
  @Value.Immutable
  public static interface BlobAndTree {
    @Nullable
    Blob getBlob();
    Tree getTree();
  }
  
  public BlobStateBuilderDefault(ClientState state) {
    super();
    this.state = state;
  }
  @Override
  public BlobStateBuilderDefault repo(String repoName) {
    this.repoName = repoName;
    return this;
  }
  @Override
  public BlobStateBuilderDefault anyId(String refOrCommitOrTag) {
    this.refOrCommitOrTag = refOrCommitOrTag;
    return this;
  }
  @Override
  public BlobStateBuilderDefault blobName(String blobName) {
    this.blobName = blobName;
    return this;
  }
  @Override
  public Uni<ObjectsResult<BlobObjects>> build() {
    RepoAssert.notEmpty(repoName, () -> "repoName is not defined!");
    RepoAssert.notEmpty(refOrCommitOrTag, () -> "refOrCommitOrTag is not defined!");
    RepoAssert.notEmpty(blobName, () -> "blobName is not defined!");
    
    return state.repos().getByNameOrId(repoName).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(ImmutableObjectsResult
            .<BlobObjects>builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(RepoException.builder().notRepoWithName(repoName))
            .build());
      }
      final var ctx = state.withRepo(existing);
      
      return getTagCommit(refOrCommitOrTag, ctx)
        .onItem().transformToUni(tag -> {
          if(tag == null) {
            return getRefCommit(refOrCommitOrTag, ctx);
          }
          return Uni.createFrom().item(tag);
        })
        .onItem().transformToUni(commitId -> {
          if(commitId == null) {
            return getCommit(refOrCommitOrTag, ctx);
          }
          return getCommit(commitId, ctx);
        }).onItem().transformToUni(commit -> {
          if(commit == null) {
            return Uni.createFrom().item(ImmutableObjectsResult
                .<BlobObjects>builder()
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
      .append(" '").append(refOrCommitOrTag).append("'")
      .toString())
      .build();
  }
  
  private Message noBlob(Repo repo, Tree tree) {
    return ImmutableMessage.builder()
      .text(new StringBuilder()
      .append("Repo with name: '").append(repo.getName()).append("'")
      .append(", tag, ref or commit with id: ").append(" '").append(refOrCommitOrTag).append("'")
      .append(" and tree: ").append(tree.toString())
      .append(" does not contain a blob with name: ").append("'").append(blobName).append("'").append("!")
      .toString())
      .build();
  }
  
  private Uni<ObjectsResult<BlobObjects>> getState(Repo repo, Commit commit, ClientRepoState ctx) {
    return getTree(commit, ctx).onItem()
        .transformToUni(tree -> getBlob(tree, ctx)).onItem()
        .transformToUni(blobAndTree -> {
          
          if(blobAndTree.getBlob() == null) {
            return Uni.createFrom().item(ImmutableObjectsResult
                .<BlobObjects>builder()
                .status(ObjectsStatus.ERROR)
                .addMessages(noBlob(repo, blobAndTree.getTree()))
                .build()); 
          }
          
          return Uni.createFrom().item(ImmutableObjectsResult.<BlobObjects>builder()
            .repo(repo)
            .objects(ImmutableBlobObjects.builder()
                .repo(repo)
                .tree(blobAndTree.getTree())
                .commit(commit)
                .blob(blobAndTree.getBlob())
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
  private Uni<BlobAndTree> getBlob(Tree tree, ClientRepoState ctx) {
    final var entry = tree.getValues().entrySet().stream().filter(e -> e.getValue().getName().equals(blobName)).findFirst();
    if(entry.isPresent()) {
      return ctx.query().blobs().id(entry.get().getValue().getBlob())
          .onItem().transform(blob -> ImmutableBlobAndTree.builder().blob(blob).tree(tree).build());
    }
    
    return Uni.createFrom().item(ImmutableBlobAndTree.builder().tree(tree).build());
  }
}
