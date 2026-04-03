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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;


public interface Tree extends Entity {
  
  UUID getId();
  Collection<Node> getTreeNodes();
  Collection<Node> findAllNodes(List<String> filters, Consumer<String> onMissing);
  Optional<Node> findOneNode(String fullPath);
  Node getOneNode(String fullPath);
  
  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.TREE; 
  }
  

  
  default Tree withTransitives(Stream<Node> merge) {
    final Map<UUID, Node> all = new HashMap<>();
    
    merge.forEach(node -> {
      if(all.containsKey(node.getId())) {
        if(node.getTransitives() == null || node.getTransitives().getBlob() == null) {
          return;
        }
        all.put(node.getId(), node);
      } else {
        all.put(node.getId(), node);
      }
    });
    return ImmutableTree.builder()
        .id(getId())
        .treeNodes(all.values())
        .build();
  }


  // H(tree) = μ(∑ᵢ₌₁ⁿ H(nodeᵢ))
  public static ImmutableTree.Builder newInstance(Collection<Node> nodes) {
    final var content = Entity.uuid();
    final var sorted = nodes.stream()
      .sorted((a, b) -> (a.getFullPath()).compareTo(b.getFullPath()))
      .map(node -> {
        content.append(node.getId());
        return node;
      })
      .toList();
    return ImmutableTree.builder().id(content.build()).treeNodes(sorted);
  }
}
