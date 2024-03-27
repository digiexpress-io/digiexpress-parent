package io.resys.thena.structures.git;

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

import io.resys.thena.api.ImmutableGitRepoObjects;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.ThenaClient.GitRepoQuery;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
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
public class GitRepoQueryImpl implements GitRepoQuery {
  private final DbState state;
  private final String projectName; //repo name

  @Override
  public Uni<QueryEnvelope<ThenaClient.GitRepoObjects>> get() {
    RepoAssert.notEmpty(projectName, () -> "projectName not defined!");
    
    return state.tenant().getByNameOrId(projectName).onItem().transformToUni((Tenant existing) -> {
          
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(projectName, log));
      }
      return getState(state.toGitState(existing));
    });
  }
  
  private Uni<QueryEnvelope<ThenaClient.GitRepoObjects>> getState(GitState ctx) {
    final Uni<ThenaClient.GitRepoObjects> objects = Uni.combine().all().unis(
        getRefs(ctx),
        getTags(ctx),
        getBlobs(ctx),
        getTrees(ctx),
        getCommits(ctx)
    ).combinedWith(raw -> {
      final var builder = ImmutableGitRepoObjects.builder();
      raw.stream().map(r -> (ThenaClient.GitRepoObjects) r).forEach(r -> builder
          .putAllBranches(r.getBranches())
          .putAllTags(r.getTags())
          .putAllValues(r.getValues())
      );
      return builder.build();
    });
    
    return objects.onItem().transform(state -> ImmutableQueryEnvelope
      .<ThenaClient.GitRepoObjects>builder()
      .objects(state)
      .repo(ctx.getDataSource().getTenant())
      .status(QueryEnvelopeStatus.OK)
      .build());
  }
  
  private Uni<ThenaClient.GitRepoObjects> getRefs(GitState ctx) {
    return ctx.query().refs().findAll().collect().asList().onItem()
        .transform(refs -> ImmutableGitRepoObjects.builder()
            .putAllBranches(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<ThenaClient.GitRepoObjects> getTags(GitState ctx) {
    return ctx.query().tags().find().collect().asList().onItem()
        .transform(refs -> ImmutableGitRepoObjects.builder()
            .putAllTags(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<ThenaClient.GitRepoObjects> getBlobs(GitState ctx) {
    return ctx.query().blobs().findAll().collect().asList().onItem()
        .transform(blobs -> {
          
          final var objects = ImmutableGitRepoObjects.builder();
          blobs.forEach(blob -> objects.putValues(blob.getId(), blob));
          return objects.build();
        });
  }
  private Uni<ThenaClient.GitRepoObjects> getTrees(GitState ctx) {
    return ctx.query().trees().findAll().collect().asList().onItem()
        .transform(trees -> ImmutableGitRepoObjects.builder()
            .putAllValues(trees.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
  private Uni<ThenaClient.GitRepoObjects> getCommits(GitState ctx) {
    return ctx.query().commits().findAll().collect().asList().onItem()
        .transform(commits -> ImmutableGitRepoObjects.builder()
            .putAllValues(commits.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
}
