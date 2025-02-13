package io.resys.thena.api.entities.grim;

import java.time.OffsetDateTime;

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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.envelope.ThenaContainer;
import jakarta.annotation.Nullable;

public interface ThenaGrimContainers extends ThenaContainer {

  @Value.Immutable
  interface GrimMissionContainer extends ThenaGrimContainers { 
    
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

    
    @JsonIgnore
    default Optional<OffsetDateTime> getCreatedAt(String commitId) {
      return Optional.ofNullable(this.getCommits().get(commitId)).map(e -> e.getCreatedAt());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @JsonIgnore
    default GrimMissionContainer sort() {
      final var result = ImmutableGrimMissionContainer.builder();
      
      final Function<GrimMission, Comparable> getMissionSortingKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };
      
      final Function<GrimMissionLabel, Comparable> getLabelSortingKey = (mission) -> {
        return getCreatedAt(mission.getCommitId())
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getLabelType() + "/" + mission.getLabelValue());
      };
      
      final Function<GrimMissionLink, Comparable> getLinksKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getLinkType() + "/" + mission.getExternalId());
      };
      
      final Function<GrimRemark, Comparable> getRemarksKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };
      
      final Function<GrimObjective, Comparable> getObjectiveKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };

      final Function<GrimObjectiveGoal, Comparable> getGoalKey = (mission) -> {
        return Optional.ofNullable(mission.getTransitives())
            .map(e -> e.getUpdatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };

      final Function<GrimMissionData, Comparable> getDataKey = (mission) -> {
        return Optional.ofNullable(mission.getCreatedAt())
            .or(() -> getCreatedAt(mission.getCommitId()))
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };
      
      final Function<GrimAssignment, Comparable> getAssignmentKey = (mission) -> {
        return getCreatedAt(mission.getCommitId())
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getAssignmentType() + "/" + mission.getAssignee());
      };
      
      final Function<GrimCommit, OffsetDateTime> getCommitKey = (mission) -> {
        return mission.getCreatedAt();
      };
      
      final Function<GrimCommitViewer, OffsetDateTime> getViewKey = (mission) -> {
        return mission.getUpdatedAt();
      };
      
      final Function<GrimCommands, Comparable> getCommandsKey = (mission) -> {
        return getCreatedAt(mission.getCommitId())
            .map(e -> (Comparable) e)
            .orElse((Comparable) mission.getCommitId());
      };
      
      this.getMissions().values().stream()
        .sorted((a, b) -> getMissionSortingKey.apply(a).compareTo(getMissionSortingKey.apply(b)))
        .forEach(m -> result.putMissions(m.getId(), m));

      this.getMissionLabels().values().stream()
        .sorted((a, b) -> getLabelSortingKey.apply(a).compareTo(getLabelSortingKey.apply(b)))
        .forEach(m -> result.putMissionLabels(m.getId(), m));
      
      this.getLinks().values().stream()
        .sorted((a, b) -> getLinksKey.apply(a).compareTo(getLinksKey.apply(b)))
        .forEach(m -> result.putLinks(m.getId(), m));     

      this.getRemarks().values().stream()
        .sorted((a, b) -> getRemarksKey.apply(a).compareTo(getRemarksKey.apply(b)))
        .forEach(m -> result.putRemarks(m.getId(), m));    

      this.getObjectives().values().stream()
        .sorted((a, b) -> getObjectiveKey.apply(a).compareTo(getObjectiveKey.apply(b)))
        .forEach(m -> result.putObjectives(m.getId(), m));
      
      this.getGoals().values().stream()
        .sorted((a, b) -> getGoalKey.apply(a).compareTo(getGoalKey.apply(b)))
        .forEach(m -> result.putGoals(m.getId(), m));  
      
      this.getData().values().stream()
        .sorted((a, b) -> getDataKey.apply(a).compareTo(getDataKey.apply(b)))
        .forEach(m -> result.putData(m.getId(), m));  
      
      this.getAssignments().values().stream()
        .sorted((a, b) -> getAssignmentKey.apply(a).compareTo(getAssignmentKey.apply(b)))
        .forEach(m -> result.putAssignments(m.getId(), m));  

      this.getCommits().values().stream()
        .sorted((a, b) -> getCommitKey.apply(a).compareTo(getCommitKey.apply(b)))
        .forEach(m -> result.putCommits(m.getCommitId(), m));
      
      this.getCommands().values().stream()
        .sorted((a, b) -> getCommandsKey.apply(a).compareTo(getCommandsKey.apply(b)))
        .forEach(m -> result.putCommands(m.getId(), m));

      this.getViews().values().stream()
        .sorted((a, b) -> getViewKey.apply(a).compareTo(getViewKey.apply(b)))
        .forEach(m -> result.putViews(m.getId(), m));
      return result.build();
    }
  }
 
  
  // world state
  @Value.Immutable
  interface GrimProjectObjects extends ThenaGrimContainers { 
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
  
  @Value.Immutable
  interface GrimContainerVersion extends ThenaGrimContainers {
    String getMissionId(); 
    String getCurrentCommitId();
    @Nullable GrimMissionContainer getParentVersion();
    @Nullable GrimMissionContainer getCurrentVersion();
  }
}
