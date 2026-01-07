package io.digiexpress.thena.cockpit.client.api;

/*-
 * #%L
 * thena-CockpitConfig-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.immutables.value.Value;

import io.digiexpress.thena.cockpit.client.api.CockpitMergeObject.MergeCockpitConfig;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfig;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;



public interface CockpitCommitActions {
  
  CreateOneCockpitConfig createOneCockpitConfig();
  ModifyOneCockpitConfig modifyOneCockpitConfig();
  

  interface CreateOneCockpitConfig {
    CreateOneCockpitConfig commitAuthor(String author);
    CreateOneCockpitConfig commitMessage(String message);
    CreateOneCockpitConfig cockpitConfig(Consumer<NewCockpitConfig> addCockpitConfig);
    CreateOneCockpitConfig onNewCockpitConfig(Consumer<CockpitContainer> handleNewState);
    Uni<OneCockpitConfigEnvelope> build();
  }
  
  interface ModifyOneCockpitConfig {
    ModifyOneCockpitConfig commitAuthor(String author);
    ModifyOneCockpitConfig commitMessage(String message);
    ModifyOneCockpitConfig cockpitConfigId(String cockpitConfigId);
    ModifyOneCockpitConfig modifyCockpitConfig(Consumer<MergeCockpitConfig> modifyCockpitConfig);
    Uni<OneCockpitConfigEnvelope> build();
  }
  
  
  interface ModifyManyCockpitConfigs {
    ModifyManyCockpitConfigs commitAuthor(String author);
    ModifyManyCockpitConfigs commitMessage(String message);
    ModifyManyCockpitConfigs modifyMission(String missionId, Consumer<MergeCockpitConfig> mergeMission);
    
    Uni<ManyCockpitConfigsEnvelope> build();
  }
  
  interface CreateManyCockpitConfig {
    CreateManyCockpitConfig commitAuthor(String author);
    CreateManyCockpitConfig commitMessage(String message);
    CreateManyCockpitConfig addMission(Consumer<NewCockpitConfig> addMission);
    Uni<ManyCockpitConfigsEnvelope> build();
  }
  
  @Value.Immutable
  interface ManyCockpitConfigsEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<CockpitContainer> getCockpitConfigs();
  }
  
  
  @Value.Immutable
  interface OneCockpitConfigEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable CockpitContainer getCockpitConfig();

  }
}
