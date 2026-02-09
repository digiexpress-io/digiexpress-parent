package io.resys.thena.fs.entities;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

/**
 * Branch reference entity representing a named pointer to a specific commit.
 * Extended with metadata to support branch-specific configuration and documentation.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRef.class)
@JsonDeserialize(as = ImmutableRef.class)
public interface Ref extends FileSystemEntity {
  
  /**
   * @return the unique branch name identifier
   */
  String getRefName();
  
  /**
   * @return the commit hash that this branch currently points to
   */
  String getCommitId();
  
  /**
   * @return optional description explaining the purpose of this branch
   */
  Optional<String> getBranchDescription();
  
  /**
   * @return optional extension properties for UI configuration and custom behavior
   */
  Optional<JsonObject> getBranchProps();
  
  /**
   * @return optional permission settings controlling branch access and operations
   */
  Optional<JsonObject> getBranchPermissions();
  
  /**
   * @return optional behavioral flags for branch-specific feature toggles
   */
  Optional<JsonObject> getBranchFlags();
  
  /**
   * @return optional author identifier who created this branch
   */
  Optional<String> getBranchAuthor();

  @Value.Auxiliary
  @Nullable 
  RefTransitives getTransitives();

  @Override
  default String getId() {
    return getRefName();
  }

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.REF; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableRefTransitives.class)
  @JsonDeserialize(as = ImmutableRefTransitives.class)
  interface RefTransitives {
    // might not be loaded
    @Nullable Commit getCommit();
    
    // might not be loaded
    @Nullable Tree getTree();

    // might not be loaded
    Map<String, Node> getNodesById();
    
    // might not be loaded
    Map<String, Blob> getBlobsById();
    
    // might not be loaded
    Map<String, Props> getPropsById();  
  }

}
