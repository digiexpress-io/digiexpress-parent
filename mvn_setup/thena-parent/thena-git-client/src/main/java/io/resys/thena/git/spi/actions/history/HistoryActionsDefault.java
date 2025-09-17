package io.resys.thena.git.spi.actions.history;

/*-
 * #%L
 * thena-git-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.git.api.GitDataSource;
import io.resys.thena.git.api.GitHistoryActions;
import io.resys.thena.git.api.ImmutableBlobCommitObjects;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class HistoryActionsDefault implements GitHistoryActions {

  private final GitDataSource state;
  private final String repoId;

  @Override
  public BlobHistoryQuery blobQuery() {
    return new BlobHistoryQueryImpl(state, repoId);
  }

  @Override
  public BlobCommitQuery blobCommitQuery() {
    
    return new BlobCommitQuery() {
      private boolean includBlob;
      private String branchName;
      @Override
      public BlobCommitQuery includBlob(boolean includBlob) {
        this.includBlob = includBlob;
        return this;
      }
      @Override
      public BlobCommitQuery branchName(String branchName) {
        this.branchName = branchName;
        return this;
      }
      @Override
      public Uni<QueryEnvelope<BlobCommitObjects>> findAll() {
        RepoAssert.notEmpty(repoId, () -> "repoId is not defined!");
        RepoAssert.notEmpty(branchName, () -> "branchName is not defined!");
        
        
        return state.tenant().getByNameOrId(repoId).onItem()
        .transformToUni((Tenant existing) -> {
          if(existing == null) {
            return Uni.createFrom().item(QueryEnvelope.<BlobCommitObjects>repoNotFound(repoId, log));
          }
          final var ctx = state.toGitState(existing);
          return ctx.query().blobCommits().includBlob(includBlob).branchName(branchName).findAll()
              .onItem().transform(found -> ImmutableQueryEnvelope
                  .<GitHistoryActions.BlobCommitObjects>builder().status(QueryEnvelopeStatus.OK).objects(ImmutableBlobCommitObjects.builder()
                      .values(found)
                      .build())
                  .build());
        });
      }

    };
  }
}
