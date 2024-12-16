package io.resys.thena.api.actions;

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
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;



public interface GrimCommitActions {
  
  CreateOneMission createOneMission();
  CreateManyMissions createManyMissions();
  
  ModifyOneMission modifyOneMission();
  ModifyManyMissions modifyManyMissions();  
  
  ModifyManyCommitViewers modifyManyCommitViewer();
  
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
