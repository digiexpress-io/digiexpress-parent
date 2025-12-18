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

import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.ThenaContainer;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;


public interface ThenaGrimContainers extends ThenaContainer {

  @Slf4j
  public static class GrimContainerLogger {
    
  }
  
  
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
    Map<String, GrimProcess> getProcs();
    
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
      
      getMissionLabels().values().forEach(label -> 
        Optional.ofNullable(builders.get(label.getMissionId()))
          .map(builder -> builder.putMissionLabels(label.getId(), label))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", label.getDocType(), label.getMissionId());
            return null;
          })
      );
      
      getLinks().values().forEach(link -> 
        Optional.ofNullable(builders.get(link.getMissionId()))
          .map(builder -> builder.putLinks(link.getId(), link))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", link.getDocType(), link.getMissionId());
            return null;
          })
      );
      
      getRemarks().values().forEach(remark -> 
        Optional.ofNullable(builders.get(remark.getMissionId()))
          .map(builder -> builder.putRemarks(remark.getId(), remark))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", remark.getDocType(), remark.getMissionId());
            return null;
          })
      );
      
      getObjectives().values().forEach(objective -> 
        Optional.ofNullable(builders.get(objective.getMissionId()))
          .map(builder -> builder.putObjectives(objective.getId(), objective))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", objective.getDocType(), objective.getMissionId());
            return null;
          })
      );
      
      getGoals().values().forEach(goals -> 
        Optional.ofNullable(builders.get(goals.getTransitives().getMissionId()))
          .map(builder -> builder.putGoals(goals.getId(), goals))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", goals.getDocType(), goals.getTransitives().getMissionId());
            return null;
          })
      );
      
      getData().values().forEach(data -> 
        Optional.ofNullable(builders.get(data.getMissionId()))
          .map(builder -> builder.putData(data.getId(), data))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", data.getDocType(), data.getMissionId());
            return null;
          })
      );
      
      getAssignments().values().forEach(assignment -> 
        Optional.ofNullable(builders.get(assignment.getMissionId()))
          .map(builder -> builder.putAssignments(assignment.getId(), assignment))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", assignment.getDocType(), assignment.getMissionId());
            return null;
          })
      );
      
      getCommands().values().forEach(commands -> 
        Optional.ofNullable(builders.get(commands.getMissionId()))
          .map(builder -> builder.putCommands(commands.getId(), commands))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", commands.getDocType(), commands.getMissionId());
            return null;
          })
      );
      
      getViews().values().forEach(commands -> 
        Optional.ofNullable(builders.get(commands.getMissionId()))
          .map(builder -> builder.putViews(commands.getId(), commands))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", commands.getDocType(), commands.getMissionId());
            return null;
          })
      );
      
      getCommits().values().stream().filter(e -> builders.containsKey(e.getMissionId())).forEach(commit -> 
        Optional.ofNullable(builders.get(commit.getMissionId()))
          .map(builder -> builder.putCommits(commit.getCommitId(), commit))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", GrimDocType.GRIM_COMMIT, commit.getMissionId());
            return null;
          })
      );
      
      getProcs().values().forEach(proc -> 
        Optional.ofNullable(builders.get(proc.getMissionId()))
          .map(builder -> builder.putProcs(proc.getId(), proc))
          .orElseGet(() -> {
            GrimContainerLogger.log.error("GRIM001: Mission not found for entity type: {}, missionId: {}", proc.getDocType(), proc.getMissionId());
            return null;
          })
      );
      
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
            .orElse((Comparable) mission.getLinkType() + "/" + mission.getLinkValue());
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
      
      final Function<GrimProcess, Comparable> getProcKey = (mission) -> {
        return mission.getCreated();
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
      this.getProcs().values().stream()
        .sorted((a, b) -> getProcKey.apply(a).compareTo(getProcKey.apply(b)))
        .forEach(m -> result.putProcs(m.getId(), m));
      
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
    Map<String, GrimProcess> getProcs(); 
    
  }
  
  @Value.Immutable
  interface GrimContainerVersion extends ThenaGrimContainers {
    String getMissionId(); 
    String getCurrentCommitId();
    @Nullable GrimMissionContainer getParentVersion();
    @Nullable GrimMissionContainer getCurrentVersion();
  }
}
