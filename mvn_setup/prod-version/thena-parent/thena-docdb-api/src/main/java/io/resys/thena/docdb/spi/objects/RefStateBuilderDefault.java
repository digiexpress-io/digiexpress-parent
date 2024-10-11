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

import io.resys.thena.docdb.api.actions.ImmutableObjectsResult;
import io.resys.thena.docdb.api.actions.ImmutableRefObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.RefStateBuilder;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
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
public class RefStateBuilderDefault implements RefStateBuilder {
  private final ClientState state;
  private String repo; //repo name
  private String ref;
  private boolean blobs;

  @Override
  public RefStateBuilder blobs() {
    this.blobs = true;
    return this;
  }
  @Override
  public Uni<ObjectsResult<RefObjects>> build() {
    RepoAssert.notEmpty(repo, () -> "repoName is not defined!");
    RepoAssert.notEmpty(ref, () -> "ref is not defined!");
    
    return state.repos().getByNameOrId(repo).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(ImmutableObjectsResult
            .<RefObjects>builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(RepoException.builder().notRepoWithName(repo))
            .build());
      }
      return getRef(existing, ref, state.withRepo(existing));
    });
  }
  
  private Uni<ObjectsResult<RefObjects>> getRef(Repo repo, String refName, ClientRepoState ctx) {

    return ctx.query().refs().name(refName).onItem()
        .transformToUni(ref -> {
          if(ref == null) {
            return ctx.query().refs().find().collect().asList().onItem().transform(allRefs -> 
              (ObjectsResult<RefObjects>) ImmutableObjectsResult
              .<RefObjects>builder()
              .repo(repo)
              .status(ObjectsStatus.OK)
              .addMessages(RepoException.builder().noRepoRef(
                  repo.getName(), refName, 
                  allRefs.stream().map(e -> e.getName()).collect(Collectors.toList())))
              .build()
            );
          }
          return getState(repo, ref, ctx);
        });
  }
  
  private Uni<ObjectsResult<RefObjects>> getState(Repo repo, Ref ref, ClientRepoState ctx) {
    return getCommit(ref, ctx).onItem()
        .transformToUni(commit -> getTree(commit, ctx).onItem()
        .transformToUni(tree -> {
          if(this.blobs) {
            return getBlobs(tree, ctx)
              .onItem().transform(blobs -> ImmutableObjectsResult.<RefObjects>builder()
                .repo(repo)
                .objects(ImmutableRefObjects.builder()
                    .repo(repo)
                    .ref(ref)
                    .tree(tree)
                    .blobs(blobs)
                    .commit(commit)
                    .build())
                .repo(repo)
                .status(ObjectsStatus.OK)
                .build());
          }
          
          return Uni.createFrom().item(ImmutableObjectsResult.<RefObjects>builder()
            .repo(repo)
            .objects(ImmutableRefObjects.builder()
                .repo(repo)
                .ref(ref)
                .tree(tree)
                .commit(commit)
                .build())
            .status(ObjectsStatus.OK)
            .build());
        }));
  
  }
  private Uni<Tree> getTree(Commit commit, ClientRepoState ctx) {
    return ctx.query().trees().id(commit.getTree());
  }
  private Uni<Commit> getCommit(Ref ref, ClientRepoState ctx) {
    return ctx.query().commits().id(ref.getCommit());
  }
  private Uni<Map<String, Blob>> getBlobs(Tree tree, ClientRepoState ctx) {
    return ctx.query().blobs().find(tree).collect().asList().onItem()
        .transform(blobs -> blobs.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)));
  }
}
