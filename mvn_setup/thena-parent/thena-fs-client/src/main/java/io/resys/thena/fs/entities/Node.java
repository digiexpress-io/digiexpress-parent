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

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableNode.class)
@JsonDeserialize(as = ImmutableNode.class)
public interface Node extends FileSystemEntity {
  
  // the actual hash calculated based on the contents
  String getId();
  
  // convenience junk data, the json content object id thats connected to json in the blob
  String getNodeId();
  
  // path to whatever we have part of hash calc
  Optional<String> getNodePath();
  
  // last path fragment to whatever we have part of hash calc, never empty for files
  String getNodeName();
  
  // for directorys this is not present, for actual files its always present, part of has calculation
  Optional<String> getBlobId();
  
  // extra comments, permissions, docs for everything in file or directory... meta content, part of has calculation
  Optional<String> getPropsId();

  @Value.Auxiliary
  @Nullable 
  NodeTransitives getTransitives();
  
  
  default boolean isDirectory() {
    return getBlobId().isEmpty();
  }
  
  default String getFullPath() {
    return getNodePath().map(e -> e + "/").orElse("") + getNodeName();
  }
  
  @Value.Check
  default void check() {
    
    if(isDirectory()) {
      
      if(getNodePath().isPresent()) {
        final var directoryPath = getNodePath().get();
        RepoAssert.isTrue(!directoryPath.trim().isEmpty(), () -> "directoryPath cannot be empty");
        RepoAssert.isTrue(directoryPath.matches("^[a-zA-Z0-9/_-]+$"), () -> "directoryPath contains invalid characters, only a-z, A-Z, 0-9, /, _, - allowed");
        RepoAssert.isTrue(!directoryPath.contains("//"), () -> "directoryPath cannot contain double slashes");
        RepoAssert.isTrue(!directoryPath.endsWith("/"), () -> "directoryPath cannot end with slash");      
      }

      final var directoryName = getNodeName();
      RepoAssert.notNull(directoryName, () -> "directoryName is required");
      RepoAssert.isTrue(!directoryName.trim().isEmpty(), () -> "directoryName cannot be empty");
      RepoAssert.isTrue(directoryName.matches("^[a-zA-Z0-9_-]+$"), () -> "directoryName contains invalid characters, only a-z, A-Z, 0-9, _, - allowed");
      RepoAssert.isTrue(!directoryName.contains("/"), () -> "directoryName cannot contain slashes");   
      return;
    }
 
    if(getNodePath().isPresent()) {
      final var filePath = getNodePath().get();
      RepoAssert.notNull(filePath, () -> "filePath is required");
      RepoAssert.isTrue(!filePath.trim().isEmpty(), () -> "filePath cannot be empty");
      RepoAssert.isTrue(filePath.matches("^[a-zA-Z0-9/_-]+$"), () -> "filePath contains invalid characters, only a-z, A-Z, 0-9, /, _, - allowed");
      RepoAssert.isTrue(!filePath.contains("//"), () -> "filePath cannot contain double slashes");
      RepoAssert.isTrue(!filePath.endsWith("/"), () -> "filePath cannot end with slash");
    }
    
    final var fileName = getNodeName();
    RepoAssert.notNull(fileName, () -> "fileName is required");
    RepoAssert.isTrue(!fileName.trim().isEmpty(), () -> "fileName cannot be empty");
    RepoAssert.isTrue(fileName.matches("^[a-zA-Z0-9_.-]+$"), () -> "fileName contains invalid characters, only a-z, A-Z, 0-9, _, ., - allowed");
    RepoAssert.isTrue(!fileName.contains("/"), () -> "fileName cannot contain slashes");
  }
  

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.NODE; 
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableNodeTransitives.class)
  @JsonDeserialize(as = ImmutableNodeTransitives.class)
  interface NodeTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
    
    @Nullable Blob getBlob();
    @Nullable Props getProps();
  }
    
  // H(node) = μ(node_path ⊕ node_name ⊕ H(blob) ⊕ H(props))
  public static ImmutableNode.Builder newInstance(
      Optional<String> path, 
      String nodeId, String name,
      Optional<String> blobId, Optional<String> propsId) {
    
    final var content = new StringBuilder();
    content.append(path);
    content.append(name);
    content.append(nodeId);
    content.append(blobId.orElse(""));
    content.append(propsId.orElse(""));
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableNode.builder()
        .id(hash)
        .nodeId(nodeId)
        .nodePath(path)
        .nodeName(name)
        .blobId(blobId)
        .propsId(propsId);
  }
}
