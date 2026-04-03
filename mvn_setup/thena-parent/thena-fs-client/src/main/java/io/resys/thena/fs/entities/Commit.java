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
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableCommit.class)
@JsonDeserialize(as = ImmutableCommit.class)
public interface Commit extends Entity {
  
  UUID getId();
  OffsetDateTime getCommitCreatedAt();
  String getCommitAuthor();
  String getCommitMessage();
  UUID getTreeId();
  Optional<UUID> getParentId();
  Optional<UUID> getMergeId();
  Integer getCommitNodesCount();


  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.COMMIT; 
  }

  // H(commit) = μ(H(tree) ⊕ H(parent) ⊕ H(merge) ⊕ author ⊕ timestamp ⊕ message)
  public static ImmutableCommit.Builder newInstance(
      UUID treeId, 
      Optional<UUID> parentId, 
      Optional<UUID> mergeId, 
      String author, 
      OffsetDateTime createdAt, 
      String message) {
    
    final var content = Entity.uuid();
    content.append("tree ").append(treeId);
    if (parentId.isPresent()) {
      content.append("parent ").append(parentId.get());
    }
    if (mergeId.isPresent()) {
      content.append("merge ").append(mergeId.get());
    }
    content.append("author ").append(author).append(" ").append(createdAt.toEpochSecond());
    content.append("committer ").append(author).append(" ").append(createdAt.toEpochSecond());
    content.append(message);
    
    
    return ImmutableCommit.builder()
        .id(content.build())
        .treeId(treeId)
        .parentId(parentId)
        .mergeId(mergeId)
        .commitAuthor(author)
        .commitCreatedAt(createdAt)
        .commitMessage(message);
  }
}
