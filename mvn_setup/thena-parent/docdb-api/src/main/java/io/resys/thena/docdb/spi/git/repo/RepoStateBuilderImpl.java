package io.resys.thena.docdb.spi.git.repo;

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

import io.resys.thena.docdb.api.DocDB.GitModel.GitProjectQuery;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableProjectObjects;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaGitObjects.ProjectObjects;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.GitDbState.GitRepo;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class RepoStateBuilderImpl implements GitProjectQuery {
  private final DbState state;
  private String projectName; //repo name

  @Override
  public Uni<QueryEnvelope<ProjectObjects>> get() {
    RepoAssert.notEmpty(projectName, () -> "projectName not defined!");
    
    return state.project().getByNameOrId(projectName).onItem().transformToUni((Repo existing) -> {
          
      if(existing == null) {
        final var ex = RepoException.builder().notRepoWithName(projectName);
        log.warn(ex.getText());
        return Uni.createFrom().item(ImmutableQueryEnvelope
            .<ProjectObjects>builder()
            .status(QueryEnvelopeStatus.ERROR)
            .addMessages(ex)
            .build());
      }
      return getState(existing, state.toGitState().withRepo(existing));
    });
  }
  
  private Uni<QueryEnvelope<ProjectObjects>> getState(Repo repo, GitRepo ctx) {
    final Uni<ProjectObjects> objects = Uni.combine().all().unis(
        getRefs(repo, ctx),
        getTags(repo, ctx),
        getBlobs(repo, ctx),
        getTrees(repo, ctx),
        getCommits(repo, ctx)
    ).combinedWith(raw -> {
      final var builder = ImmutableProjectObjects.builder();
      raw.stream().map(r -> (ProjectObjects) r).forEach(r -> builder
          .putAllBranches(r.getBranches())
          .putAllTags(r.getTags())
          .putAllValues(r.getValues())
      );
      return builder.build();
    });
    
    return objects.onItem().transform(state -> ImmutableQueryEnvelope
      .<ProjectObjects>builder()
      .objects(state)
      .repo(repo)
      .status(QueryEnvelopeStatus.OK)
      .build());
  }
  
  private Uni<ProjectObjects> getRefs(Repo repo, GitRepo ctx) {
    return ctx.query().refs().findAll().collect().asList().onItem()
        .transform(refs -> ImmutableProjectObjects.builder()
            .putAllBranches(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<ProjectObjects> getTags(Repo repo, GitRepo ctx) {
    return ctx.query().tags().find().collect().asList().onItem()
        .transform(refs -> ImmutableProjectObjects.builder()
            .putAllTags(refs.stream().collect(Collectors.toMap(r -> r.getName(), r -> r)))
            .build());
  }
  private Uni<ProjectObjects> getBlobs(Repo repo, GitRepo ctx) {
    return ctx.query().blobs().findAll().collect().asList().onItem()
        .transform(blobs -> {
          
          final var objects = ImmutableProjectObjects.builder();
          blobs.forEach(blob -> objects.putValues(blob.getId(), blob));
          return objects.build();
        });
  }
  private Uni<ProjectObjects> getTrees(Repo repo, GitRepo ctx) {
    return ctx.query().trees().findAll().collect().asList().onItem()
        .transform(trees -> ImmutableProjectObjects.builder()
            .putAllValues(trees.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
  private Uni<ProjectObjects> getCommits(Repo repo, GitRepo ctx) {
    return ctx.query().commits().findAll().collect().asList().onItem()
        .transform(commits -> ImmutableProjectObjects.builder()
            .putAllValues(commits.stream().collect(Collectors.toMap(r -> r.getId(), r -> r)))
            .build());
  }
}
