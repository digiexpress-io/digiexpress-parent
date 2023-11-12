package io.resys.thena.docdb.api.models;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.ThenaGitObject.CommitLockStatus;
import io.vertx.core.json.JsonObject;

public interface ThenaDocObject {
  interface IsDocObject { String getId(); }
  
  @Value.Immutable
  interface DocFlatted extends ThenaDocObject {
    String getExternalId();
    Optional<String> getExternalIdDeleted();
    
    String getDocId();
    String getDocType();
    Optional<String> getDocParentId();
    DocStatus getDocStatus();
    Optional<JsonObject> getDocMeta();


    String getBranchId();
    String getBranchName();
    DocStatus getBranchStatus();
    JsonObject getBranchValue();
    
    String getCommitAuthor();
    LocalDateTime getCommitDateTime();
    String getCommitMessage();
    Optional<String> getCommitParent();
    String getCommitId();
    
    Optional<String> getDocLogId();
    Optional<JsonObject> getDocLogValue();
  }
  
  
  @Value.Immutable
  interface Doc extends ThenaDocObject, IsDocObject {
    String getId();
    String getType();
    DocStatus getStatus();
    String getExternalId();
    @Nullable String getExternalIdDeleted();
    @Nullable String getParentId();
    @Nullable JsonObject getMeta();
  }
  
  @Value.Immutable
  interface DocBranch extends ThenaDocObject, IsDocObject {
    String getId();
    String getCommitId();
    String getBranchName();
    String getDocId();
    DocStatus getStatus();
    @Nullable JsonObject getValue();  // null when json loading is disabled
  }
  
  @Value.Immutable
  interface DocCommit extends ThenaDocObject, IsDocObject {
    String getId();
    String getBranchId();
    String getDocId();
    String getAuthor();
    LocalDateTime getDateTime();
    String getMessage();
    Optional<String> getParent();    


  }
   
  @Value.Immutable
  interface DocLog extends ThenaDocObject, IsDocObject {
    String getId();
    String getBranchId();
    String getDocId();
    String getDocCommitId();
    JsonObject getValue();
  }
  
  
  @Value.Immutable  
  interface DocBranchLock extends ThenaDocObject {
    CommitLockStatus getStatus();
    Optional<Doc> getDoc();
    Optional<DocBranch> getBranch();
    Optional<DocCommit> getCommit();
    Optional<String> getMessage();
  }
  
  @Value.Immutable  
  interface DocLock extends ThenaDocObject {
    CommitLockStatus getStatus();
    Optional<Doc> getDoc();
    List<DocBranchLock> getBranches();
    Optional<String> getMessage();
  }
  
  
  enum DocStatus {
    IN_FORCE, ARCHIVED
  }
}
