package io.resys.thena.api.entities.grim;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
