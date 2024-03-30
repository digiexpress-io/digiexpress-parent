package io.resys.thena.api.entities.grim;

import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.api.entities.org.ThenaOrgObjects;
import io.resys.thena.api.envelope.ThenaContainer;

public interface ThenaGrimContainers extends ThenaContainer {

  @Value.Immutable
  interface GrimMissionContainer extends ThenaOrgObjects { 
    GrimMission getMission();
    
    Map<String, GrimLabel> getLabels();
    Map<String, GrimMissionLink> getLinks();
    Map<String, GrimRemark> getRemarks();
    Map<String, GrimObjective> getObjectives();
    Map<String, GrimObjectiveGoal> getGoals();
    Map<String, GrimMissionData> getData();
    Map<String, GrimAssignment> getAssignments();
    Map<String, GrimCommit> getCommits(); 
  }
 
  
  // world state
  @Value.Immutable
  interface GrimProjectObjects extends ThenaOrgObjects { 
    Map<String, GrimMission>  getMissions();
    Map<String, GrimLabel> getLabels();
    Map<String, GrimMissionLink> getLinks();
    Map<String, GrimRemark> getRemarks();
    Map<String, GrimObjective> getObjectives();
    Map<String, GrimObjectiveGoal> getGoals();
    Map<String, GrimMissionData> getData();
    Map<String, GrimAssignment> getAssignments();
    Map<String, GrimCommit> getCommits();
    Map<String, GrimCommitTree> getCommitTrees();
    Map<String, GrimCommitViewer> getCommitViewers();
  }
}
