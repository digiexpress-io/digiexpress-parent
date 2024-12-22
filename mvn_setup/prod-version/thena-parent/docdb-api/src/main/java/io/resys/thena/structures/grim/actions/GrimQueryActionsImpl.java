package io.resys.thena.structures.grim.actions;

import java.util.List;

import io.resys.thena.api.actions.GrimQueryActions;
import io.resys.thena.api.entities.grim.GrimUniqueMissionLabel;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DbState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimQueryActionsImpl implements GrimQueryActions {
  private final DbState startingState;
  private final String repoId;
  
  @Override
  public CommitViewersQuery commitViewersQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public MissionQuery missionQuery() {
    final var state = startingState.toGrimState(repoId);
    return new GrimMissionQueryImpl(state);
  }

  // plain delegate
  @Override
  public MissionLabelQuery missionLabelQuery() {
    return new MissionLabelQuery() {
      @Override
      public Uni<List<GrimUniqueMissionLabel>> findAllUnique() {
        return startingState.toGrimState(repoId).onItem().transformToUni(state -> state.query().missionLabels().findAllUnique());
      }
    };
  }
  
  // plain delegate
  @Override
  public MissionRemarkQuery missionRemarkQuery() {
    return new MissionRemarkQuery() {
      @Override
      public Uni<QueryEnvelope<GrimMissionContainer>> getOneByRemarkId(String remarkId) {
        return startingState.toGrimState(repoId)
            .onItem().transformToUni(state -> state.query().missionRemarks().getOneByRemarkId(remarkId))
            .onItem().transform(items -> 
              ImmutableQueryEnvelope.<GrimMissionContainer>builder()
                .repo(startingState.getDataSource().getTenant())
                .status(QueryEnvelopeStatus.OK)
                .objects(items)
                .build());
      }
      
      @Override
      public Uni<QueryEnvelope<GrimMissionContainer>> findAllByMissionId(String missionId) {
        return startingState.toGrimState(repoId)
            .onItem().transformToUni(state -> state.query().missionRemarks().findAllByMissionId(missionId))
            .onItem().transform(items -> 
              ImmutableQueryEnvelope.<GrimMissionContainer>builder()
                .repo(startingState.getDataSource().getTenant())
                .status(QueryEnvelopeStatus.OK)
                .objects(items)
                .build());
      }

      @Override
      public Uni<QueryEnvelope<GrimMissionContainer>> findAllByReporterId(String reporterId) {
        return startingState.toGrimState(repoId)
            .onItem().transformToUni(state -> state.query().missionRemarks().findAllByReporterId(reporterId))
            .onItem().transform(items -> 
              ImmutableQueryEnvelope.<GrimMissionContainer>builder()
                .repo(startingState.getDataSource().getTenant())
                .status(QueryEnvelopeStatus.OK)
                .objects(items)
                .build());
      }
    };
  }
}
