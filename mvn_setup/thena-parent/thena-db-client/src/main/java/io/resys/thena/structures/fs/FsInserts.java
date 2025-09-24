package io.resys.thena.structures.fs;

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

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.fs.FsCommit;
import io.resys.thena.api.entities.fs.FsCommitTree;
import io.resys.thena.api.entities.fs.FsDirent;
import io.resys.thena.api.entities.fs.FsDirentAssignment;
import io.resys.thena.api.entities.fs.FsDirentData;
import io.resys.thena.api.entities.fs.FsDirentLabel;
import io.resys.thena.api.entities.fs.FsDirentLink;
import io.resys.thena.api.entities.fs.FsDirentRemark;
import io.resys.thena.api.envelope.Message;
import io.smallrye.mutiny.Uni;

public interface FsInserts {
  
  Uni<FsBatchDirents> batchMany(FsBatchDirents output);
  
  @Value.Immutable
  interface FsBatchDirents {
    List<FsDirent> getDirents();
    List<FsDirentLabel> getLabels();
    List<FsDirentLink> getLinks();
    List<FsDirentRemark> getRemarks();
    List<FsDirentData> getData();
    List<FsDirentAssignment> getAssignments();

    
    // Commit related
    List<FsCommit> getCommits();
    List<FsCommitTree> getCommitTrees();
    
    // Objects to update
    List<FsDirentData> getUpdateData();
    List<FsDirentRemark> getUpdateRemarks();
    List<FsDirent> getUpdateDirents();
    List<FsDirentLink> getUpdateLinks();

    // Objects to delete
    List<FsDirentAssignment> getDeleteAssignments();
    List<FsDirentLink> getDeleteLinks();
    List<FsDirentLabel> getDeleteDirentLabels();
    List<FsDirentRemark> getDeleteRemarks();
    List<FsDirentData> getDeleteData();
    
    
    BatchStatus getStatus();
    String getTenantId();

    String getLog();
    List<Message> getMessages();
    
    @JsonIgnore
    default boolean isEmpty() {
      return 
        this.getDirents().isEmpty() &&
        this.getLabels().isEmpty() &&
        this.getLinks().isEmpty() &&
        this.getRemarks().isEmpty() &&
        this.getData().isEmpty() &&
        this.getAssignments().isEmpty() &&
        
        // Objects to update
        this.getUpdateData().isEmpty() &&
        this.getUpdateRemarks().isEmpty() &&
        this.getUpdateDirents().isEmpty() &&
  
        // Objects to delete
        this.getDeleteAssignments().isEmpty() &&
        this.getDeleteLinks().isEmpty() &&
        this.getDeleteDirentLabels().isEmpty() &&
        this.getDeleteRemarks().isEmpty();
    }
    
  }
}
