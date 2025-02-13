package io.resys.thena.api.entities.grim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObjects;
import io.resys.thena.api.envelope.ThenaContainer;

public interface ThenaGrimContainers extends ThenaContainer {

  @Value.Immutable
  interface GrimMissionContainer extends ThenaOrgObjects { 
    
    Map<String, GrimMission> getMissions();    
    Map<String, GrimMissionLabel> getMissionLabels();
    Map<String, GrimMissionLink> getLinks();
    Map<String, GrimRemark> getRemarks();
    Map<String, GrimObjective> getObjectives();
    Map<String, GrimObjectiveGoal> getGoals();
    Map<String, GrimMissionData> getData();
    Map<String, GrimAssignment> getAssignments();
    Map<String, GrimCommit> getCommits(); 
    Map<String, GrimCommands> getCommands();
    Map<String, GrimCommitViewer> getViews();
    
    @JsonIgnore
    default GrimMission getMission() {
      return this.getMissions().values().iterator().next();
    }

    @JsonIgnore
    default List<GrimMissionContainer> groupByMission() {
      final var builders = new HashMap<String, ImmutableGrimMissionContainer.Builder>(); 
      for(final var mission : getMissions().values()) {
        builders.put(mission.getId(), ImmutableGrimMissionContainer.builder().putMissions(mission.getId(), mission));
      }
      getMissionLabels().values().forEach(label -> builders.get(label.getMissionId()).putMissionLabels(label.getId(), label));
      getLinks().values().forEach(link -> builders.get(link.getMissionId()).putLinks(link.getId(), link));
      getRemarks().values().forEach(remark -> builders.get(remark.getMissionId()).putRemarks(remark.getId(), remark));
      getObjectives().values().forEach(objective -> builders.get(objective.getMissionId()).putObjectives(objective.getId(), objective));
      getGoals().values().forEach(goals -> builders.get(goals.getTransitives().getMissionId()).putGoals(goals.getId(), goals));
      getData().values().forEach(data -> builders.get(data.getMissionId()).putData(data.getId(), data));
      getAssignments().values().forEach(assignment -> builders.get(assignment.getMissionId()).putAssignments(assignment.getId(), assignment));
      getCommands().values().forEach(commands -> builders.get(commands.getMissionId()).putCommands(commands.getId(), commands));
      getViews().values().forEach(commands -> builders.get(commands.getMissionId()).putViews(commands.getId(), commands));
      getCommits().values().stream().filter(e -> builders.containsKey(e.getMissionId())).forEach(commit -> builders.get(commit.getMissionId()).putCommits(commit.getCommitId(), commit));
      return builders.values().stream().map(builder -> builder.build()).collect(Collectors.toList());
    }
  }
 
  
  // world state
  @Value.Immutable
  interface GrimProjectObjects extends ThenaOrgObjects { 
    Map<String, GrimMission>  getMissions();
    Map<String, GrimMissionLink> getLinks();
    Map<String, GrimRemark> getRemarks();
    Map<String, GrimObjective> getObjectives();
    Map<String, GrimObjectiveGoal> getGoals();
    Map<String, GrimMissionData> getData();
    Map<String, GrimAssignment> getAssignments();
    Map<String, GrimCommit> getCommits();
    Map<String, GrimCommitTree> getCommitTrees();
    Map<String, GrimCommitViewer> getCommitViewers();
    Map<String, GrimCommands> getCommands(); 
    
  }
}
