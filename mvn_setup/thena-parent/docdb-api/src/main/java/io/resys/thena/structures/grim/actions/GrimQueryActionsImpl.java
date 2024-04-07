package io.resys.thena.structures.grim.actions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.resys.thena.api.actions.GrimQueryActions;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.ImmutableQueryEnvelopeList;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimQueryActionsImpl implements GrimQueryActions {
  private final DbState state;
  private final String repoId;
  
  @Override
  public CommitViewersQuery commitViewersQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public MissionQuery missionQuery() {
    final var assignments = new ArrayList<Tuple2<String, String>>();
    return new MissionQuery() {
      private String usedBy, usedFor, reporterId, likeTitle, likeDescription;
      private List<String> ids;
      private GrimArchiveQueryType includeArchived;
      private LocalDate fromCreatedOrUpdated;
      @Override
      public MissionQuery addMissionId(List<String> ids) {
        if(this.ids == null) {
          this.ids = new ArrayList<>();
        }
        this.ids.addAll(ids);
        return this;
      }
      @Override
      public MissionQuery addAssignment(String assignementType, String assignmentId) {
        assignments.add(Tuple2.of(assignementType, assignmentId));
        return this;
      }
      @Override
      public MissionQuery viewer(String usedBy, String usedFor) {
        this.usedBy = RepoAssert.notEmpty(usedBy, () -> "usedBy can't be empty!"); 
        this.usedFor = RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!"); 
        return this;
      }
      @Override
      public MissionQuery archived(GrimArchiveQueryType includeArchived) {
        this.includeArchived = includeArchived;
        return this;
      }
      @Override
      public MissionQuery reporterId(String reporterId) {
        this.reporterId = reporterId;
        return this;
      }
      @Override
      public MissionQuery likeTitle(String likeTitle) {
        this.likeTitle = likeTitle;
        return this;
      }
      @Override
      public MissionQuery likeDescription(String likeDescription) {
        this.likeDescription = likeDescription;
        return this;
      }
      @Override
      public MissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated) {
        this.fromCreatedOrUpdated = fromCreatedOrUpdated;
        return this;
      }
      @Override
      public Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId) {
        return state.toGrimState(repoId).onItem().transformToUni(state -> {
          final var query = state.query().missions();
          if(usedBy != null) {
            query.viewer(usedBy, usedFor);
          }
          
          assignments.forEach(e -> query.addAssignment(e.getItem1(), e.getItem2()));
          return query
              .reporterId(reporterId)
              .archived(includeArchived)
              .fromCreatedOrUpdated(fromCreatedOrUpdated)
              .likeDescription(likeDescription)
              .likeTitle(likeTitle)
              .getById(missionIdOrExtId).onItem().transform(items -> 
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
          if(usedBy != null) {
            query.viewer(usedBy, usedFor);
          }
          if(this.ids != null) {
            query.missionId(this.ids.toArray(new String[] {}));
          }

          assignments.forEach(e -> query.addAssignment(e.getItem1(), e.getItem2()));
          return query
              .reporterId(reporterId)
              .archived(includeArchived)
              .fromCreatedOrUpdated(fromCreatedOrUpdated)
              .likeDescription(likeDescription)
              .likeTitle(likeTitle)
              .findAll().collect().asList().onItem().transform(items -> 
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
