package io.resys.hdes.resource.editor.api;

/*-
 * #%L
 * thena-re-backend
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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface ReResource extends Serializable {

  interface BatchMutator extends Serializable {
    String getCommit();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableBlobUpdate.class)
  @JsonDeserialize(as = ImmutableBlobUpdate.class)
  interface BlobUpdate extends BatchMutator {
    String getBlob();
  }


  @Value.Immutable
  @JsonSerialize(as = ImmutableSnapshotResource.class)
  @JsonDeserialize(as = ImmutableSnapshotResource.class)
  public interface SnapshotResource extends ReResource {
    Head getHead();
    Project getProject();
    Map<String, Blob> getBlobs();     // name       - asset
    Map<String, Error> getErrors();   // name name  - error
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableError.class)
  @JsonDeserialize(as = ImmutableError.class)
  interface Error extends Serializable {
    String getId();
    List<String> getMessages();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableBlob.class)
  @JsonDeserialize(as = ImmutableBlob.class)
  interface Blob extends Serializable {
    String getId();
    String getName();
    String getSrc(); 
    Map<String, Serializable> getAst();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableProjectResource.class)
  @JsonDeserialize(as = ImmutableProjectResource.class)
  public interface ProjectResource extends ReResource {
    Project getProject();
    Map<String, ReResource.Head> getHeads();
    Map<String, ReResource.HeadState> getStates();
  }

  @JsonSerialize(as = ImmutableProject.class)
  @JsonDeserialize(as = ImmutableProject.class)
  @Value.Immutable
  interface Project extends Serializable {
    String getId();
    String getRev();
    String getName();
    LocalDateTime getCreated();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableHead.class)
  @JsonDeserialize(as = ImmutableHead.class)
  interface Head {
    String getId();
    String getName();
    Commit getCommit();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableHeadState.class)
  @JsonDeserialize(as = ImmutableHeadState.class)
  interface HeadState {
    String getId();
    String getHead();
    Long getCommits();
    HeadStateType getType();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCommit.class)
  @JsonDeserialize(as = ImmutableCommit.class)
  interface Commit {
    String getId();
    String getAuthor();
    LocalDateTime getDateTime();
  }

  enum HeadStateType {
    ahead, behind, same
  }
}
