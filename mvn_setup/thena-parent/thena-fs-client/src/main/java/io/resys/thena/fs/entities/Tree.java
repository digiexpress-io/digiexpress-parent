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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonArray;

@Value.Immutable
@JsonSerialize(as = ImmutableTree.class)
@JsonDeserialize(as = ImmutableTree.class)
public interface Tree extends Entity {
  
  String getId();
  List<Node> getTreeNodes();
  
  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.TREE; 
  }
  
  default Tree withTransitives(Stream<Node> merge) {
    final Map<String, Node> all = new HashMap<>();
    
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
  
  default List<Node> findAllNodes(List<String> filters, Consumer<String> onMissing) {
    final var result = new ArrayList<Node>(); 
    for(final var objectId : filters) {
      final var found = getTreeNodes().stream()
          .filter(node -> (
              node.getFullPath().equals(objectId) || 
              node.getId().equals(objectId) || 
              node.getObjectId().equals(objectId)
          ))
          .toList();
      onMissing.accept(objectId);
      result.addAll(found);
    }
    return Collections.unmodifiableList(result);
  }
  
  default Optional<Node> findOneNode(String fullPath) {
    final var found = findAllNodes(Arrays.asList(fullPath), (ignore) -> {});
    if(found.size() > 1) {
      RepoAssert.isTrue(found.size() <= 1, 
          () -> "Expected exactly 0..1 nodes, but found: " + found.size() + 
                " for path: " + fullPath + " nodes: " + System.lineSeparator() + 
                new JsonArray(found).encodePrettily().replace("\"", "'")
                );      
    }

    return found.stream().findFirst();
  }
  
  default Node getOneNode(String fullPath) {
    final var found = findOneNode(fullPath);
    RepoAssert.isTrue(found.isPresent(), () -> "Expected exactly 1 node, but found: 0 for path: " + fullPath);
    return found.get();
  }


  // H(tree) = μ(∑ᵢ₌₁ⁿ H(nodeᵢ))
  public static ImmutableTree.Builder newInstance(Collection<Node> nodes) {
    final var content = new StringBuilder();
    
    final var sorted = nodes.stream()
        .sorted((a, b) -> (a.getFullPath()).compareTo(b.getFullPath()))
        .map(node -> {
          content.append(node.getId());
          return node;
        })
        .toList();
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableTree.builder().id(hash).treeNodes(sorted);
  }
}
