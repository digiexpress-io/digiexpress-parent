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

import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.ImmutableObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.actions.ObjectsActions.RepoStateBuilder;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableObjects;
import io.resys.thena.docdb.api.models.Objects;
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
public class RepoStateBuilderDefault implements RepoStateBuilder {
  private final ClientState state;
  private String repo; //repo name

  @Override
  public Uni<ObjectsResult<Objects>> build() {
    RepoAssert.notEmpty(repo, () -> "repo not defined!");
    
    return state.repos().getByNameOrId(repo).onItem().transformToUni((Repo existing) -> {
          
      if(existing == null) {
        return Uni.createFrom().item(ImmutableObjectsResult
            .<Objects>builder()
            .status(ObjectsStatus.ERROR)
            .addMessages(RepoException.builder().notRepoWithName(repo))
            .build());
      }
      return getState(existing, state.withRepo(existing));
    });
  }
  
  private Uni<ObjectsResult<Objects>> getState(Repo repo, ClientRepoState ctx) {
    final Uni<Objects> objects = Uni.combine().all().unis(
        getRefs(repo, ctx),
        getTags(repo, ctx),
        getBlobs(repo, ctx),
        getTrees(repo, ctx),
        getCommits(repo, ctx)
    ).combinedWith(raw -> {
      final var builder = ImmutableObjects.builder();
      raw.stream().map(r -> (Objects) r).forEach(r -> builder
          .putAllRefs(r.getRefs())
          .putAllTags(r.getTags())
          .putAllValues(r.getValues())
      );
      return builder.build();
    });
    
    return objects.onItem().transform(state -> ImmutableObjectsResult
      .<Objects>builder()
      .objects(state)
      .status(ObjectsStatus.OK)
      .build());
  }
  
  private Uni<Objects> getRefs(Repo repo, ClientRepoState ctx) {
    return ctx.query().refs().find().collect().asList().onItem()
        .transform(refs -> ImmutableObjects.builder()
            .putAllRefs(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<Objects> getTags(Repo repo, ClientRepoState ctx) {
    return ctx.query().tags().find().collect().asList().onItem()
        .transform(refs -> ImmutableObjects.builder()
            .putAllTags(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<Objects> getBlobs(Repo repo, ClientRepoState ctx) {
    return ctx.query().blobs().find().collect().asList().onItem()
        .transform(blobs -> ImmutableObjects.builder()
            .putAllValues(blobs.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
  private Uni<Objects> getTrees(Repo repo, ClientRepoState ctx) {
    return ctx.query().trees().find().collect().asList().onItem()
        .transform(trees -> ImmutableObjects.builder()
            .putAllValues(trees.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
  private Uni<Objects> getCommits(Repo repo, ClientRepoState ctx) {
    return ctx.query().commits().find().collect().asList().onItem()
        .transform(commits -> ImmutableObjects.builder()
            .putAllValues(commits.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
}
