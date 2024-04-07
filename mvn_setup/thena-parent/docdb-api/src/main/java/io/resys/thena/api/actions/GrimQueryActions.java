package io.resys.thena.api.actions;

import java.time.LocalDate;
import java.util.List;

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
    MissionQuery addAssignment(String assignementType, String assignmentId);
    MissionQuery viewer(String userBy, String usedFor);
    MissionQuery addMissionId(List<String> ids);
    MissionQuery archived(ArchiveQueryType includeArchived);
    
    MissionQuery reporterId(String reporterId);
    MissionQuery likeTitle(String likeTitle);
    MissionQuery likeDescription(String likeDescription);
    MissionQuery fromCreatedOrUpdated(LocalDate fromCreatedOrUpdated);
    
    Uni<QueryEnvelope<GrimMissionContainer>> get(String missionIdOrExtId);
    Uni<QueryEnvelopeList<GrimMissionContainer>> findAll();
  }
  
  enum ArchiveQueryType {
    ALL, ONLY_ARCHIVED, ONLY_IN_FORCE
  }
}
