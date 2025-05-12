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
import io.resys.thena.api.entities.fs.FsDirent;
import io.resys.thena.api.entities.fs.FsDirentAssignment;
import io.resys.thena.api.entities.fs.FsDirentLabel;
import io.resys.thena.api.entities.fs.FsDirentLink;
import io.resys.thena.api.entities.fs.FsDirentRemark;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject.MergeDirent;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirent;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Uni;



public interface FsCommitActions {
  
  CreateOneDirent createOneDirent();
  CreateManyDirents createManyDirents();
  
  ModifyOneDirent modifyOneDirent();
  ModifyManyDirents modifyManyDirents();  
  
  

  interface ModifyOneDirent {
    ModifyOneDirent commitAuthor(String author);
    ModifyOneDirent commitMessage(String message);
    ModifyOneDirent direntId(String direntId);
    ModifyOneDirent modifyDirent(Consumer<MergeDirent> addDirent);
    
    Uni<OneDirentEnvelope> build();
  }
  
  interface ModifyManyDirents {
    ModifyManyDirents commitAuthor(String author);
    ModifyManyDirents commitMessage(String message);
    ModifyManyDirents modifyDirent(String missionId, Consumer<MergeDirent> mergeDirent);
    
    Uni<ManyDirentsEnvelope> build();
  }
  
  interface CreateManyDirents {
    CreateManyDirents commitAuthor(String author);
    CreateManyDirents commitMessage(String message);
    CreateManyDirents addDirent(Consumer<NewDirent> addDirent);
    Uni<ManyDirentsEnvelope> build();
  }
  
  
  interface CreateOneDirent {
    CreateOneDirent commitAuthor(String author);
    CreateOneDirent commitMessage(String message);
    CreateOneDirent dirent(Consumer<NewDirent> addDirent);
    Uni<OneDirentEnvelope> build();
  }

  @Value.Immutable
  interface ManyDirentsEnvelope extends ThenaEnvelope {
    String getTenantId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<FsDirent> getDirents();
  }
  @Value.Immutable
  interface OneDirentEnvelope extends ThenaEnvelope {
    String getTenantId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable FsDirent getDirent();
    List<FsDirentAssignment> getAssignments(); // assignments that are linked to mission 
    List<FsDirentRemark> getRemarks(); // remarks that are linked to mission
    List<FsDirentLink> getLinks(); // remarks that are linked to mission
    List<FsDirentLabel> getLabels(); // labels that are linked to mission
  }
}
