package io.resys.thena.api.entities.grim;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.entities.TenantEntity;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;


/**
 * Audit trail for deleted mission, represents last known object state.
 * Commit-s and commit views are not backed up.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGrimDeletedMission.class)
@JsonDeserialize(as = ImmutableGrimDeletedMission.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface GrimDeletedMission extends IsGrimObject, TenantEntity {
  GrimMission getMission();    
  
  List<GrimMissionLabel> getMissionLabels();
  List<GrimMissionLink> getLinks();
  List<GrimRemark> getRemarks();
  List<GrimObjective> getObjectives();
  List<GrimObjectiveGoal> getGoals();
  List<GrimMissionData> getData();
  List<GrimAssignment> getAssignments(); 
  List<GrimCommands> getCommands();
  List<GrimCommit> getCommits();
  
  @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_MISSION; };
}
