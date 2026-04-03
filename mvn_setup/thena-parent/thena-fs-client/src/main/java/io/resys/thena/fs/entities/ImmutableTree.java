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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
public class ImmutableTree implements Tree {
  
  private final UUID id;
  private final Map<String, Node> trees_nodes_by_objectId;
  private final Map<UUID, Node> trees_nodes_by_uuid;
  private final Map<String, Node> tree_nodes_by_path;
  
  @Override
  public UUID getId() {
    return id;
  }
  @Override
  public Collection<Node> getTreeNodes() {
    return trees_nodes_by_objectId.values();
  }
  @Override
  public Optional<Node> findOneNode(String fullPath) {
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
  @Override
  public List<Node> findAllNodes(List<String> filters, Consumer<String> onMissing) {
    final var result = new ArrayList<Node>(); 
    for(final var objectId : filters) {
      
      Node found = trees_nodes_by_uuid.get(Entity.toUuidOrNull(objectId));
      if(found == null) {
        found = trees_nodes_by_objectId.get(objectId);
      }
      if(found == null) {
        found = tree_nodes_by_path.get(objectId);
      }
      if(found == null) {
        onMissing.accept(objectId);
      } else {
        result.add(found);        
      }
      

    }
    return Collections.unmodifiableList(result);
  }
  @Override
  public Node getOneNode(String fullPath) {
    final var found = findOneNode(fullPath);
    RepoAssert.isTrue(found.isPresent(), () -> "Expected exactly 1 node, but found: 0 for path: " + fullPath);
    return found.get();
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Setter
  @Accessors(chain = true, fluent = true)
  @RequiredArgsConstructor
  public static class Builder {
    private UUID id;
    private final Map<String, Node> trees_nodes_by_objectId;
    private final Map<UUID, Node> trees_nodes_by_uuid;
    private final Map<String, Node> tree_nodes_by_path;
    
    public Builder() {
      this.trees_nodes_by_objectId = new HashMap<>();
      this.trees_nodes_by_uuid = new HashMap<>();
      this.tree_nodes_by_path = new HashMap<>();
    }

    public Builder treeNodes(Collection<Node> nodes) {
      nodes.forEach(this::addTreeNodes);
      return this;
    }
    public Builder addTreeNodes(Node node) {
      trees_nodes_by_uuid.put(node.getId(), node);
      tree_nodes_by_path.put(node.getFullPath(), node);
      trees_nodes_by_objectId.put(node.getObjectId(), node);
      return this;
    }
    public Tree build() {
      return new ImmutableTree(
        id, 
        Collections.unmodifiableMap(trees_nodes_by_objectId), 
        Collections.unmodifiableMap(trees_nodes_by_uuid), 
        Collections.unmodifiableMap(tree_nodes_by_path)
      );
    }
  }
}
