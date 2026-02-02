package io.resys.thena.grim.api;

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

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.immutables.value.Value;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeProcess;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewProcess;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;



public interface GrimCommitActions {
  
  CreateOneMission createOneMission();
  CreateManyMissions createManyMissions();
  
  ModifyOneMission modifyOneMission();
  ModifyManyMissions modifyManyMissions();  
  
  ModifyManyCommitViewers modifyManyCommitViewer();
  
  ModifyOneProc modifyOneProc();
  
  CreateOneProc createOneProc();
  
  interface CreateOneProc {
    CreateOneProc commitAuthor(String author);
    CreateOneProc commitMessage(String message);
    CreateOneProc proc(Consumer<NewProcess> modifyProc);
    
    Uni<OneProcEnvelope> build();
  }
  
  
  interface ModifyOneProc {
    ModifyOneProc commitAuthor(String author);
    ModifyOneProc commitMessage(String message);
    ModifyOneProc procId(String procId);

    ModifyOneProc onAnyUni(Function<MergeProcess, Uni<?>> callback); // stage 1: do some async tasks after locking proc table and before on mission
    ModifyOneProc onMission(BiConsumer<Optional<GrimMissionContainer>, MergeProcess> onMission); // stage 2: resolves mission by questionnaire id
    ModifyOneProc modifyProc(BiConsumer<GrimProcess, MergeProcess> modifyProc);
    
    Uni<OneProcEnvelope> build();
  }
  
  
  interface ModifyManyCommitViewers {
    ModifyManyCommitViewers usedFor(String usedFor);
    ModifyManyCommitViewers commitAuthor(String author);
    ModifyManyCommitViewers commitMessage(String message);
    ModifyManyCommitViewers object(String commitId, GrimDocType docs, String objectId);
    Uni<ManyCommitViewersEnvelope> build();
  }

  interface ModifyOneMission {
    ModifyOneMission commitAuthor(String author);
    ModifyOneMission commitMessage(String message);
    ModifyOneMission missionId(String missionId);
    
    ModifyOneMission modifyMission(Consumer<MergeMission> addMission);
    ModifyOneMission addProcess(Consumer<NewProcess> process);
    
    Uni<OneMissionEnvelope> build();
  }
  
  interface ModifyManyMissions {
    ModifyManyMissions commitAuthor(String author);
    ModifyManyMissions commitMessage(String message);
    ModifyManyMissions modifyMission(String missionId, Consumer<MergeMission> mergeMission);
    
    Uni<ManyMissionsEnvelope> build();
  }
  
  interface CreateManyMissions {
    CreateManyMissions commitAuthor(String author);
    CreateManyMissions commitMessage(String message);
    CreateManyMissions addMission(Consumer<NewMission> addMission);
    Uni<ManyMissionsEnvelope> build();
  }
  
  
  interface CreateOneMission {
    CreateOneMission commitAuthor(String author);
    CreateOneMission commitMessage(String message);
    CreateOneMission mission(Consumer<NewMission> addMission);
    Uni<OneMissionEnvelope> build();
  }

  @Value.Immutable
  interface ManyMissionsEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<GrimMission> getMissions();
  }
  @Value.Immutable
  interface OneMissionEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable GrimMission getMission();
    List<GrimAssignment> getAssignments(); // assignments that are linked to mission 
    List<GrimRemark> getRemarks(); // remarks that are linked to mission
    List<GrimMissionLink> getLinks(); // remarks that are linked to mission
    List<GrimMissionLabel> getLabels(); // labels that are linked to mission
    List<GrimObjective> getObjectives(); // labels that are linked to mission
  }
  
  @Value.Immutable
  interface OneProcEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable GrimProcess getProc();
  }
  
  
  @Value.Immutable
  interface ManyCommitViewersEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<GrimCommitViewer> getViewers();
  }
}
