package io.resys.thena.grim.spi.actions;

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
