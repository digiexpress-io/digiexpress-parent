package io.resys.thena.docdb.spi.diff;

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

import io.resys.thena.docdb.api.actions.DiffActions.DiffResult;
import io.resys.thena.docdb.api.actions.DiffActions.DiffStatus;
import io.resys.thena.docdb.api.actions.DiffActions.HeadDiffBuilder;
import io.resys.thena.docdb.api.actions.ImmutableDiffResult;
import io.resys.thena.docdb.api.actions.ObjectsActions;
import io.resys.thena.docdb.api.actions.ObjectsActions.CommitObjects;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsStatus;
import io.resys.thena.docdb.api.models.Diff;
import io.resys.thena.docdb.api.models.Objects;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class HeadDiffBuilderDefault implements HeadDiffBuilder {
  private final ClientState state;
  private final ObjectsActions objects;
  
  private String repo;  //RepoIdOrName;
  private String left;  //HeadOrCommitOrTag;
  private String right; //HeadOrCommitOrTag;

  @Override
  public Uni<DiffResult<Diff>> build() {
    RepoAssert.notEmpty(repo, () -> "repo is not defined!");
    RepoAssert.notEmpty(left, () -> "left is not defined!");
    RepoAssert.notEmpty(right, () -> "right is not defined!");
    
    return Uni.combine().all().unis(
        objects.repoState().repo(repo).build(),
        objects.commitState().anyId(left).repo(repo).build(), 
        objects.commitState().anyId(right).repo(repo).build())

      .asTuple().onItem().transform(tuple -> {
        final var objects = tuple.getItem1();
        final var left = tuple.getItem2();
        final var right = tuple.getItem3();
        
        if(left.getStatus() != ObjectsStatus.OK || right.getStatus() != ObjectsStatus.OK) {
          return ImmutableDiffResult.<Diff>builder()
              .addAllMessages(left.getMessages())
              .addAllMessages(right.getMessages())
              .status(DiffStatus.ERROR)
              .build();
        }
        return createDiff(objects.getObjects(), left.getObjects(), right.getObjects());
      });
  }
  private DiffResult<Diff> createDiff(Objects objects, CommitObjects left, CommitObjects right) {
    final var diff = new DiffVisitor().visit(objects, left, Arrays.asList(right));
    return ImmutableDiffResult.<Diff>builder()
        .repo(left.getRepo())
        .objects(diff)
        .status(DiffStatus.OK)
        .build();
  }
}
