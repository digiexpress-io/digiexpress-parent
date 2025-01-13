package io.resys.thena.structures.grim.actions;

import java.util.List;

import io.resys.thena.api.actions.GrimQueryActions.MissionCommitQuery;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.grim.GrimState;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor @Setter @Accessors(fluent = true)
public class GrimMissionCommitQueryImpl implements MissionCommitQuery {
  private final DbState startingState;
  private final String repoId;


  @Override
  public Uni<QueryEnvelope<GrimMissionContainer>> findPreviousCommit(String missionId, String currentCommitId) {
    return startingState.toGrimState(repoId).onItem().transformToUni(tx -> {

      return visitCommit(tx, missionId, currentCommitId)
          .onItem()
          .transform(commit -> visitResponse(tx, commit));
    });
  }

  private Uni<Tuple2<GrimCommit, List<GrimCommitTree>>> visitCommit(GrimState tx, String missionId, String currentCommitId) {
    return Uni.combine().all().unis(
      tx.query().commit().getOneByMissionIdAndCommitId(missionId, currentCommitId),
      tx.query().commitTree().findAllByMissionIdAndCommitId(missionId, currentCommitId)
    ).asTuple();
  }
  
  private QueryEnvelope<GrimMissionContainer> visitResponse(GrimState tx, Tuple2<GrimCommit, List<GrimCommitTree>> commit) {
    return ImmutableQueryEnvelope.<GrimMissionContainer>builder()
      .repo(tx.getDataSource().getTenant())
      .status(QueryEnvelopeStatus.OK)
      .objects(null)
      .build();
  }
}