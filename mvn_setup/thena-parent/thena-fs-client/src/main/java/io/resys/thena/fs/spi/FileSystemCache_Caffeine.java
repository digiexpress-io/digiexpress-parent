package io.resys.thena.fs.spi;

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

import java.time.Duration;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.spi.FileSystemConfig.FileSystemCache;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FileSystemCache_Caffeine implements FileSystemCache {
  private final Cache<String, CachedNode> nodesById;
  

  public FileSystemCache_Caffeine() {
    nodesById = Caffeine.newBuilder()
        .maximumSize(200)
        .expireAfterWrite(Duration.ofMinutes(30))
        .build();
    log.info("FileSystem cache created with caffeine");
  }

  @Override
  public Optional<Tree> findOneTreeById(String treeId) {
    final var result = ImmutableTree.builder().id(treeId);
    nodesById.asMap().values().stream().filter(t -> t.getTreeId().equals(treeId))
        .map(e -> e.getNode())
        .forEach(result::addTreeNodes);
    
    final var tree = result.build();
    if(tree.getTreeNodes().isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(tree);
  }

  @Override
  public Tree cacheOneTree(Tree tree) {
    for(final var node : tree.getTreeNodes()) {
      final var cachedNode = nodesById.get(node.getId(), (nodeId) -> new CachedNode(tree.getId(), node));
      if(cachedNode.node.getTransitives().getBlob() == null && node.getTransitives().getBlob() != null) {
        nodesById.put(node.getId(), new CachedNode(tree.getId(), node));
      }
    }

    final var result = ImmutableTree.builder().id(tree.getId());
    nodesById.asMap().values().stream().filter(t -> t.getTreeId().equals(tree.getId()))
        .map(e -> e.getNode())
        .forEach(result::addTreeNodes);
    return result.build();
  }

  @Override
  public void flushAll() {
    nodesById.invalidateAll();
  }
  
  @Value
  private static final class CachedNode {
    String treeId;
    Node node;
  }
}
