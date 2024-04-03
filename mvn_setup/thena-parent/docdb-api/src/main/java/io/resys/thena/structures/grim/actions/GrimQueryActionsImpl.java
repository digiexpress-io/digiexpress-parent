package io.resys.thena.structures.grim.actions;

import java.util.ArrayList;

import io.resys.thena.api.actions.GrimQueryActions;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.spi.DbState;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimQueryActionsImpl implements GrimQueryActions {
  private final DbState state;
  private final String repoId;
  
  @Override
  public MissionQuery missionQuery() {
    final var assignments = new ArrayList<Tuple2<String, String>>();
    return new MissionQuery() {
      
      @Override
      public MissionQuery orAssignment(String assignementType, String assignmentId) {
        assignments.add(Tuple2.of(assignementType, assignmentId));
        return this;
      }
      
      @Override
      public Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId) {
        return state.toGrimState(repoId).onItem().transformToUni(state -> {
          final var query = state.query().missions();
          
          assignments.forEach(e -> query.addAssignmentFilter(e.getItem1(), e.getItem2()));
          return query.getById(missionIdOrExtId).onItem().transform(items -> 
            ImmutableQueryEnvelope.<GrimMissionContainer>builder()
              .repo(state.getDataSource().getTenant())
              .status(QueryEnvelopeStatus.OK)
              .objects(items)
              .build()
          );
        });
      }
      
      @Override
      public Uni<QueryEnvelopeList<GrimMissionContainer>> findAll() {
        return state.toGrimState(repoId).onItem().transformToUni(state -> {
          final var query = state.query().missions();
          
          assignments.forEach(e -> query.addAssignmentFilter(e.getItem1(), e.getItem2()));
          return query.findAll().collect().asList().onItem().transform(items -> 
            ImmutableQueryEnvelopeList.<GrimMissionContainer>builder()
              .repo(state.getDataSource().getTenant())
              .status(QueryEnvelopeStatus.OK)
              .objects(items)
              .build()
          );
        });
      }
    };
  }
}
