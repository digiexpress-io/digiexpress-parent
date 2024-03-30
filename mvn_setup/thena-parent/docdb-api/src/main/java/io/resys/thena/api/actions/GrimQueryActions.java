package io.resys.thena.api.actions;

import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.smallrye.mutiny.Uni;

public interface GrimQueryActions {
  MissionQuery missionQuery();
  
  interface MissionQuery {
    MissionQuery orAssignment(String assignmentId, String assignementType); 
    Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId);
    Uni<QueryEnvelopeList<GrimMissionContainer>> findAll();
  }

  
}
