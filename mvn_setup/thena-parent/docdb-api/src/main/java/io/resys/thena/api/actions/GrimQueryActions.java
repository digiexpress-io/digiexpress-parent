package io.resys.thena.api.actions;

import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;



public interface GrimQueryActions {
  MissionQuery missionQuery();
  CommitViewersQuery commitViewersQuery();
  
  interface CommitViewersQuery {
    Uni<QueryEnvelopeList<GrimCommitViewer>> findAll();
  }
  
  interface MissionQuery {
    MissionQuery assignment(String assignementType, String assignmentId);
    MissionQuery viewer(String userBy, String usedFor);
    Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId);
    Uni<QueryEnvelopeList<GrimMissionContainer>> findAll();
  }
}
