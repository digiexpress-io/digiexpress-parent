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
import java.util.Collection;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableTree.class)
@JsonDeserialize(as = ImmutableTree.class)
public interface Tree extends FileSystemEntity {
  
  String getId();
  List<Node> getTreeNodes();

  @Value.Auxiliary
  @Nullable 
  TreeTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.TREE; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTreeTransitives.class)
  @JsonDeserialize(as = ImmutableTreeTransitives.class)
  interface TreeTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }

  // H(tree) = μ(∑ᵢ₌₁ⁿ H(nodeᵢ))
  public static ImmutableTree.Builder newInstance(Collection<Node> nodes) {
    final var content = new StringBuilder();
    
    final var sorted = nodes.stream()
        .sorted(
            (a, b) -> 
              (a.getNodePath().map(n -> n + "/").orElse("") + a.getNodeName())
            .compareTo
              (b.getNodePath().map(n -> n + "/").orElse("") + b.getNodeName())
        )
        .map(node -> {
          content.append(node.getId());
          return node;
        })
        .toList();
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableTree.builder()
        .id(hash)
        .treeNodes(sorted);
  }
}
