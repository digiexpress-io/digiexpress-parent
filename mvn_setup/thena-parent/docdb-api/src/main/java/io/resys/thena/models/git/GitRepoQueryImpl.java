package io.resys.thena.models.git;

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

import io.resys.thena.api.ThenaClient.GitStructuredTenant.GitRepoQuery;
import io.resys.thena.api.models.ImmutableGitRepoObjects;
import io.resys.thena.api.models.ImmutableQueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.Repo;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.ThenaGitObjects.GitRepoObjects;
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
public class GitRepoQueryImpl implements GitRepoQuery {
  private final DbState state;
  private final String projectName; //repo name

  @Override
  public Uni<QueryEnvelope<GitRepoObjects>> get() {
    RepoAssert.notEmpty(projectName, () -> "projectName not defined!");
    
    return state.project().getByNameOrId(projectName).onItem().transformToUni((Repo existing) -> {
          
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(projectName, log));
      }
      return getState(existing, state.toGitState().withRepo(existing));
    });
  }
  
  private Uni<QueryEnvelope<GitRepoObjects>> getState(Repo repo, GitRepo ctx) {
    final Uni<GitRepoObjects> objects = Uni.combine().all().unis(
        getRefs(repo, ctx),
        getTags(repo, ctx),
        getBlobs(repo, ctx),
        getTrees(repo, ctx),
        getCommits(repo, ctx)
    ).combinedWith(raw -> {
      final var builder = ImmutableGitRepoObjects.builder();
      raw.stream().map(r -> (GitRepoObjects) r).forEach(r -> builder
          .putAllBranches(r.getBranches())
          .putAllTags(r.getTags())
          .putAllValues(r.getValues())
      );
      return builder.build();
    });
    
    return objects.onItem().transform(state -> ImmutableQueryEnvelope
      .<GitRepoObjects>builder()
      .objects(state)
      .repo(repo)
      .status(QueryEnvelopeStatus.OK)
      .build());
  }
  
  private Uni<GitRepoObjects> getRefs(Repo repo, GitRepo ctx) {
    return ctx.query().refs().findAll().collect().asList().onItem()
        .transform(refs -> ImmutableGitRepoObjects.builder()
            .putAllBranches(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<GitRepoObjects> getTags(Repo repo, GitRepo ctx) {
    return ctx.query().tags().find().collect().asList().onItem()
        .transform(refs -> ImmutableGitRepoObjects.builder()
            .putAllTags(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<GitRepoObjects> getBlobs(Repo repo, GitRepo ctx) {
    return ctx.query().blobs().findAll().collect().asList().onItem()
        .transform(blobs -> {
          
          final var objects = ImmutableGitRepoObjects.builder();
          blobs.forEach(blob -> objects.putValues(blob.getId(), blob));
          return objects.build();
        });
  }
  private Uni<GitRepoObjects> getTrees(Repo repo, GitRepo ctx) {
    return ctx.query().trees().findAll().collect().asList().onItem()
        .transform(trees -> ImmutableGitRepoObjects.builder()
            .putAllValues(trees.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
  private Uni<GitRepoObjects> getCommits(Repo repo, GitRepo ctx) {
    return ctx.query().commits().findAll().collect().asList().onItem()
        .transform(commits -> ImmutableGitRepoObjects.builder()
            .putAllValues(commits.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
}
