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

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
  Optional<String> getRefDescription();
  
  /**
   * @return optional extension properties for UI configuration and custom behavior
   */
  Optional<JsonObject> getRefProps();
  
  /**
   * @return optional permission settings controlling branch access and operations
   */
  Optional<JsonObject> getRefPermissions();
  
  /**
   * @return optional behavioral flags for branch-specific feature toggles
   */
  Optional<JsonObject> getRefFlags();
  
  /**
   * @return optional author identifier who created this branch
   */
  Optional<String> getRefAuthor();
  

  OffsetDateTime getRefCreatedAt();
  
  Optional<String> getRefCreatedFrom();
  

  @Value.Auxiliary
  @Nullable 
  RefTransitives getTransitives();


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
    Map<String, Blob> getBlobsById();
    
    // might not be loaded
    Map<String, Props> getPropsById();  
    

    default Optional<Node> findOneNode(String objectIdOrFullPath) {
      return getTree().findOneNode(objectIdOrFullPath);
    }
  }
  
  default Ref withTransitives(Commit commit, Tree tree) {
    
    final var trs = ImmutableRefTransitives.builder();
    final var visitedProps = new ArrayList<String>();
    final var visitedBlobs = new ArrayList<String>();
    
    for(final var node : tree.getTreeNodes()) {
      
      final var blobId = node.getBlobId().orElse(null);
      if(blobId != null && !visitedBlobs.contains(blobId)) {
        visitedBlobs.add(blobId);
        trs.putBlobsById(blobId, node.getTransitives().getBlob());
      }
      
      final var propsId = node.getPropsId().orElse(null);
      if(propsId != null && !visitedProps.contains(propsId)) {
        visitedProps.add(propsId);
        trs.putPropsById(node.getPropsId().get(), node.getTransitives().getProps());
      }
    }
    return ImmutableRef.builder().from(this)
        .transitives(trs.commit(commit).tree(tree).build())
        .build();
  }

}
