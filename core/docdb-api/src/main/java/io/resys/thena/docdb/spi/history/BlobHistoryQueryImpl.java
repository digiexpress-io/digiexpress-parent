package io.resys.thena.docdb.spi.history;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.thena.docdb.api.actions.HistoryActions.BlobHistoryQuery;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.exceptions.RepoException;
import io.resys.thena.docdb.api.models.ImmutableHistoryObjects;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaObjects.HistoryObjects;
import io.resys.thena.docdb.spi.ClientState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data @Accessors(fluent = true)
@RequiredArgsConstructor
public class BlobHistoryQueryImpl implements BlobHistoryQuery {
  private final ClientState state;
  private final List<MatchCriteria> criteria = new ArrayList<>();
  private String projectName;
  private String branchName;
  private boolean latestOnly;
  private String docId;

  @Override public BlobHistoryQuery matchBy(MatchCriteria ... criteria) { this.criteria.addAll(Arrays.asList(criteria)); return this; }
  @Override public BlobHistoryQuery matchBy(List<MatchCriteria> criteria) { this.criteria.addAll(criteria); return this; }
  @Override public BlobHistoryQuery head(String projectName, String branchName) { this.projectName = projectName; this.branchName = branchName; return this; }
  @Override public BlobHistoryQuery latestOnly() { this.latestOnly = true; return this; }
  
  @Override
  public Uni<QueryEnvelope<HistoryObjects>> get() {
    RepoAssert.notEmpty(projectName, () -> "projectName is not defined!");
    RepoAssert.notEmpty(branchName, () -> "branchName is not defined!");
    
    return state.project().getByNameOrId(projectName).onItem()
    .transformToUni((Repo existing) -> {
      if(existing == null) {
        final var ex = RepoException.builder().notRepoWithName(projectName);
        log.error(ex.getText());
        return Uni.createFrom().item(ImmutableQueryEnvelope
            .<HistoryObjects>builder()
            .status(QueryEnvelopeStatus.ERROR)
            .addMessages(ex)
            .build());
      }
      final var ctx = state.withRepo(existing);
      return ctx.query().blobHistory()
        .latestOnly(latestOnly)
        .blobName(docId)
        .criteria(criteria)
        .find().collect()
        .asList().onItem().transform(found -> ImmutableQueryEnvelope
            .<HistoryObjects>builder().status(QueryEnvelopeStatus.OK).objects(ImmutableHistoryObjects.builder()
                .values(found)
                .build())
            .build());
    });
  }
}
