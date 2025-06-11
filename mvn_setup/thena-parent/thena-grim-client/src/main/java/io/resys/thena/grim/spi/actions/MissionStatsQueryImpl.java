package io.resys.thena.grim.spi.actions;

/*-
 * #%L
 * thena-grim-client
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

import io.resys.thena.api.entities.grim.GrimMissionStats.GrimMissionAttributeEvent;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.grim.api.GrimQueryActions.MissionStatsQuery;
import io.resys.thena.grim.spi.GrimDataSource;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor @Setter @Accessors(fluent = true)
public class MissionStatsQueryImpl implements MissionStatsQuery {
  private final GrimDataSource startingState;
  private final String repoId;


  @Override
  public Uni<QueryEnvelopeList<GrimMissionAttributeEvent>> findAllByMissionAttributes() {
    return startingState
        .toGrimState(repoId).onItem()
        .transformToUni(tenant -> {
          return tenant.missionStats().findAllByMissionAttributes()
          .onItem().transform(events -> ImmutableQueryEnvelopeList.<GrimMissionAttributeEvent>builder()
            .repo(tenant.getDataSource().getTenant())
            .status(QueryEnvelopeStatus.OK)
            .objects(events)
            .build());
        });
  }
}
