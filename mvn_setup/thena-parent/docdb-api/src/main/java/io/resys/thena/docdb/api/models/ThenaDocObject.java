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
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;

public interface ThenaDocObject {
  interface IsDocObject { String getId(); }

  
  @Value.Immutable
  interface Doc extends ThenaDocObject, IsDocObject {
    String getId();
    String getVersion();
    String getExternalId();
    
    @Nullable JsonObject getValue();  // null when json loading is disabled
    
    Optional<DocBranch> getBranch();  // in-case this is branch object
    List<DocBranch> getBranches();    // in case this is main and has branches 
    
  }
  
  @Value.Immutable
  interface DocBranch extends ThenaDocObject, IsDocObject {
    String getId();
    String getVersion();
    String getBranchId();
    String getVersionOrigin();
  }
  
  @Value.Immutable
  interface DocCommit extends ThenaDocObject, IsDocObject {
    String getId();
    String getAuthor();
    LocalDateTime getDateTime();
    String getMessage();
    String getParent();
    
    String getDocId();
    String getDocVersion();
  }
   
  @Value.Immutable
  interface DocLog extends ThenaDocObject, IsDocObject {
    String getId();
    String getDocId();
    Map<String, DocLogValue> getValues();
  }
  
  @Value.Immutable
  interface DocLogValue extends ThenaDocObject, IsDocObject {
    String getId();
    String getDocId();
    String getDocCommitId();
    @Nullable JsonObject getValue();  // null when json loading is disabled
  }
  
  @Value.Immutable  
  interface DocCommitLock extends ThenaDocObject {
    DocCommitLockStatus getStatus();
    Optional<DocBranch> getBranch();
    Optional<DocCommit> getCommit();
    Map<String, DocLog> getLogs();
    Optional<String> getMessage();
  }
  
  enum DocCommitLockStatus {
    LOCK_TAKEN, NOT_FOUND
  }
}
