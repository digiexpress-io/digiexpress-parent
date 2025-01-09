package io.resys.thena.structures.grim;

import java.util.List;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommands;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.structures.BatchStatus;
import io.smallrye.mutiny.Uni;

public interface GrimInserts {
  
  Uni<GrimBatchMissions> batchMany(GrimBatchMissions output);
  Uni<GrimBatchForViewers> batchMany(GrimBatchForViewers output);

  @Value.Immutable
  interface GrimBatchForViewers {
    List<GrimCommitViewer> getViewers();
    List<GrimCommitViewer> getUpdateViewers();
    String getLog();
    List<Message> getMessages();
    BatchStatus getStatus();
    String getTenantId();
  }
  
  
  @Value.Immutable
  interface GrimBatchMissions {
    List<GrimMission> getMissions();
    List<GrimMissionLabel> getMissionLabels();
    List<GrimMissionLink> getLinks();
    List<GrimRemark> getRemarks();
    List<GrimObjective> getObjectives();
    List<GrimObjectiveGoal> getGoals();
    List<GrimMissionData> getData();
    List<GrimAssignment> getAssignments();
    List<GrimCommands> getCommands();
    
    // Commit related
    List<GrimCommit> getCommits();
    List<GrimCommitTree> getCommitTrees();
    List<GrimCommitViewer> getCommitViewers();
    
    // Objects to update
    List<GrimMissionData> getUpdateData();
    List<GrimRemark> getUpdateRemarks();
    List<GrimObjectiveGoal> getUpdateGoals();
    List<GrimObjective> getUpdateObjectives();
    List<GrimMission> getUpdateMissions();
    List<GrimMissionLink> getUpdateLinks();

    // Objects to delete
    List<GrimAssignment> getDeleteAssignments();
    List<GrimMissionLink> getDeleteLinks();
    List<GrimMissionLabel> getDeleteMissionLabels();
    List<GrimRemark> getDeleteRemarks();
    List<GrimObjective> getDeleteObjectives();
    List<GrimMissionData> getDeleteData();
    List<GrimObjectiveGoal> getDeleteGoals();
    
    BatchStatus getStatus();
    String getTenantId();

    String getLog();
    List<Message> getMessages();
    
    @JsonIgnore
    default boolean isEmpty() {
      return 
        this.getMissions().isEmpty() &&
        this.getMissionLabels().isEmpty() &&
        this.getLinks().isEmpty() &&
        this.getRemarks().isEmpty() &&
        this.getObjectives().isEmpty() &&
        this.getGoals().isEmpty() &&
        this.getData().isEmpty() &&
        this.getAssignments().isEmpty() &&
        this.getCommands().isEmpty() &&
        
        // Objects to update
        this.getUpdateData().isEmpty() &&
        this.getUpdateRemarks().isEmpty() &&
        this.getUpdateGoals().isEmpty() &&
        this.getUpdateObjectives().isEmpty() &&
        this.getUpdateMissions().isEmpty() &&
  
        // Objects to delete
        this.getDeleteAssignments().isEmpty() &&
        this.getDeleteLinks().isEmpty() &&
        this.getDeleteMissionLabels().isEmpty() &&
        this.getDeleteRemarks().isEmpty() &&
        this.getDeleteObjectives().isEmpty() &&
        this.getDeleteGoals().isEmpty();
    }
    
  }
}
