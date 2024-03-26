package io.resys.thena.structures.git.diff;

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

import java.util.Arrays;
import java.util.function.Supplier;

import io.resys.thena.api.ThenaClient.GitStructuredTenant.GitRepoQuery;
import io.resys.thena.api.actions.CommitActions;
import io.resys.thena.api.actions.ImmutableDiffResult;
import io.resys.thena.api.actions.PullActions;
import io.resys.thena.api.actions.DiffActions.DiffQuery;
import io.resys.thena.api.actions.DiffActions.DiffResult;
import io.resys.thena.api.actions.DiffActions.DiffResultStatus;
import io.resys.thena.api.models.Diff;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.models.ThenaGitObjects.CommitObjects;
import io.resys.thena.api.models.ThenaGitObjects.GitRepoObjects;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class DiffQueryImpl implements DiffQuery {
  private final DbState state;
  private final PullActions objects;
  private final CommitActions commits;
  private final Supplier<GitRepoQuery> repos;
  
  private String left;  //HeadOrCommitOrTag;
  private String right; //HeadOrCommitOrTag;

  @Override
  public Uni<DiffResult<Diff>> get() {
    RepoAssert.notEmpty(left, () -> "left is not defined!");
    RepoAssert.notEmpty(right, () -> "right is not defined!");
    
    return Uni.combine().all().unis(
        repos.get().get(),
        commits.commitQuery().branchNameOrCommitOrTag(left).get(), 
        commits.commitQuery().branchNameOrCommitOrTag(right).get())

      .asTuple().onItem().transform(tuple -> {
        final var objects = tuple.getItem1();
        final var left = tuple.getItem2();
        final var right = tuple.getItem3();
        
        if(left.getStatus() != QueryEnvelopeStatus.OK || right.getStatus() != QueryEnvelopeStatus.OK) {
          return ImmutableDiffResult.<Diff>builder()
              .addAllMessages(left.getMessages())
              .addAllMessages(right.getMessages())
              .status(DiffResultStatus.ERROR)
              .build();
        }
        return createDiff(objects.getObjects(), left.getObjects(), right.getObjects());
      });
  }
  private DiffResult<Diff> createDiff(GitRepoObjects objects, CommitObjects left, CommitObjects right) {
    final var diff = new DiffVisitor().visit(objects, left, Arrays.asList(right));
    return ImmutableDiffResult.<Diff>builder()
        .repo(left.getRepo())
        .objects(diff)
        .status(DiffResultStatus.OK)
        .build();
  }
}
