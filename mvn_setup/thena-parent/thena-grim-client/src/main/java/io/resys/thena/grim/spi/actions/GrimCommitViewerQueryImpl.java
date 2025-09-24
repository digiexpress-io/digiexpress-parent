package io.resys.thena.grim.spi.actions;

/*-
 * #%L
 * thena-db-client
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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.grim.api.GrimQueryActions.CommitViewersQuery;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimCommitViewerQueryImpl implements CommitViewersQuery {
  
  private final Uni<GrimState> state;
  private String usedBy;
  private String usedFor;
  private List<String> missionId;
  private Duration duration;
  
  @Override
  public CommitViewersQuery usedBy(String usedBy) {
    this.usedBy = RepoAssert.notEmpty(usedBy, () -> "usedBy can't be empty!"); 
    return this;
  }
  @Override
  public CommitViewersQuery usedFor(String usedFor) {
    this.usedFor = RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!"); 
    return this;
  }
  @Override
  public CommitViewersQuery createdIn(Duration duration) {
    this.duration = RepoAssert.notNull(duration, () -> "duration can't be null!"); 
    return this;
  }
  
  @Override
  public CommitViewersQuery missionId(String missionId) {
    this.missionId = Arrays.asList(RepoAssert.notNull(missionId, () -> "missionId can't be null!")); 
    return this;
  }
  @Override
  public CommitViewersQuery missionIds(List<String> missionIds) {
    this.missionId = RepoAssert.notNull(missionIds, () -> "missionIds can't be null!"); 
    return this;
  }
  @Override
  public CommitViewersQuery gteCreateAt(OffsetDateTime gteCreatetAt) {
    this.duration = Duration.between(RepoAssert.notNull(gteCreatetAt, () -> "gteCreatetAt can't be null!"), OffsetDateTime.now().plusDays(1)); 
    return this;
  }

  
  @Override
  public Uni<QueryEnvelopeList<GrimCommitViewer>> findAll() {
    return this.state
      .onItem().transformToUni(state -> {
        return state
            .commitViewer()
            .findAllViewersInDuration(usedBy, usedFor, duration, missionId).collect().asList()
            .onItem().transform(items -> ImmutableQueryEnvelopeList.<GrimCommitViewer>builder()
                .repo(state.getDataSource().getTenant())
                .status(QueryEnvelopeStatus.OK)
                .objects(items)
                .build());
      });
  }


}
