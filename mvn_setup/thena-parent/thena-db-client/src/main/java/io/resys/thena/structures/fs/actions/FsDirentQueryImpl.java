package io.resys.thena.structures.fs.actions;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.thena.api.actions.FsQueryActions.DirentQuery;
import io.resys.thena.api.actions.FsQueryActions.FsArchiveQueryType;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsObject.FsDocType;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.structures.fs.FsQueries.InternalDirentQuery;
import io.resys.thena.structures.fs.FsState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FsDirentQueryImpl implements DirentQuery {
  
  private final Uni<FsState> state;
  
  private List<String> ids;
  private List<FsDocType> docs;
  private FsArchiveQueryType includeArchived;
  private boolean lockForUpdate;
  
  
  @Override
  public DirentQuery excludeDocs(FsDocType ...docs) {
    this.docs = Arrays.asList(docs);
    return this;
  }
  @Override
  public DirentQuery addDirentId(List<String> ids) {
    if(this.ids == null) {
      this.ids = new ArrayList<>();
    }
    this.ids.addAll(ids);
    return this;
  }
  @Override
  public DirentQuery archived(FsArchiveQueryType includeArchived) {
    this.includeArchived = includeArchived;
    return this;
  }
  @Override
  public DirentQuery lockForUpdate() {
    lockForUpdate = true;
    return this;
  } 
  private InternalDirentQuery startQuery(FsState state) {
    final var query = state.query().dirents();
    if(this.ids != null) {
      query.direntId(this.ids.toArray(new String[] {}));
    }
    if(docs != null && !docs.isEmpty()) {
      query.excludeDocs(docs.toArray(new FsDocType[] {}));
    }
    
    if(lockForUpdate) {
      query.lockForUpdate();
    }
    return query.archived(includeArchived);
  }
  
  @Override
  public Uni<QueryEnvelope<FsDirentContainer>> get(String missionIdOrExtId) {
    return this.state
        .onItem().transformToUni(state -> {  
          return startQuery(state).getById(missionIdOrExtId)
            .onItem().transform((items) -> ImmutableQueryEnvelope.<FsDirentContainer>builder()
                .repo(state.getDataSource().getTenant())
                .status(QueryEnvelopeStatus.OK)
                .objects(items)
                .build());
        });
  }
  
  @Override
  public Uni<QueryEnvelopeList<FsDirentContainer>> findAll() {
    return this.state
      .onItem().transformToUni(state -> {
        return startQuery(state).findAll().collect().asList()
          .onItem().transform(items -> ImmutableQueryEnvelopeList.<FsDirentContainer>builder()
              .repo(state.getDataSource().getTenant())
              .status(QueryEnvelopeStatus.OK)
              .objects(items)
              .build());
      });      
  }
}
