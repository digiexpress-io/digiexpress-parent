package io.resys.thena.api.models;

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
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;

public interface ThenaGitObject {

  interface IsGitObject { String getId(); }
  interface IsGitName { String getName(); }
  
  
  // branch with a name
  @Value.Immutable
  interface Branch extends IsGitName, ThenaGitObject {
    // last commit in the branch
    String getCommit();
  }

  @Value.Immutable
  interface Tag extends IsGitName, ThenaGitObject {
    // id of a commit
    String getCommit();
    LocalDateTime getDateTime();
    String getAuthor();
    String getMessage();
  }
  
  // World state 
  @Value.Immutable
  interface Tree extends IsGitObject, ThenaGitObject {
    // resource name - blob id
    Map<String, TreeValue> getValues();
  }
  
  // Resource name - blob id(content in blob)
  @Value.Immutable
  interface TreeValue extends ThenaGitObject {
    // Name of the resource
    String getName();
    // Id of the blob that holds content
    String getBlob();
  }
  
  @Value.Immutable
  interface Commit extends IsGitObject, ThenaGitObject {
    String getAuthor();
    LocalDateTime getDateTime();
    String getMessage();
    
    // Parent commit id
    Optional<String> getParent();
    
    // This commit is merge commit, that points to a commit in different branch
    Optional<String> getMerge();
    
    // Tree id that describes list of (resource name - content) entries
    String getTree();
  }
  
  @Value.Immutable
  interface Blob extends IsGitObject, ThenaGitObject {
    JsonObject getValue();
  }
  
  
  @Value.Immutable  
  interface CommitTree extends ThenaGitObject {
    String getCommitId();
    
    String getCommitAuthor();
    LocalDateTime getCommitDateTime();
    String getCommitMessage();
    @Nullable String getCommitParent();
    @Nullable String getCommitMerge();
    
    String getBranchName();
    String getTreeId();
    Optional<TreeValue> getTreeValue();
    
    // Only if loaded
    Optional<Blob> getBlob();
  }
  
  @Value.Immutable  
  interface CommitLock extends ThenaGitObject {
    CommitLockStatus getStatus();
    Optional<Branch> getBranch();
    Optional<Commit> getCommit();
    Optional<Tree> getTree();
    Map<String, Blob> getBlobs();
    Optional<String> getMessage();
  }
  
  enum CommitLockStatus { 
    LOCK_TAKEN, NOT_FOUND
  }
  
  
  @Value.Immutable
  interface BlobHistory extends ThenaGitObject {
    String getTreeId();
    String getTreeValueName();
    String getCommit();
    Blob getBlob();
  }

}