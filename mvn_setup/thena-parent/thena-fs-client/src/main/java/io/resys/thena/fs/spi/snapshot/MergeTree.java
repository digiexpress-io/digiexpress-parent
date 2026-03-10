package io.resys.thena.fs.spi.snapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.HashedMap;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.ImmutableNode;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.support.RepoAssert;
import lombok.Value;



public class MergeTree {
  @SuppressWarnings("unused")
  private final Optional<Ref> ref;
  private final Map<String, Node> nodes = new HashedMap<>();
  private final List<String> sessionNodeIds = new ArrayList<>();
  private final List<String> sessionNodePaths = new ArrayList<>();
  
  private final Map<String, Optional<Props>> props = new HashedMap<>();
  private final Map<String, Optional<Blob>> blobs = new HashedMap<>();
  
  private final List<Blob> newBlobs = new ArrayList<>();
  private final List<Props> newProps = new ArrayList<>();
  private final List<RenameOp> renames = new ArrayList<>();
  

  public MergeTree(Optional<Ref> ref) {
    super();
    this.ref = ref;
    if(ref.isEmpty()) {
      return;
    }
    final var prevTree = ref.get().getTransitives().getTree();
    for(final var node : prevTree.getTreeNodes()) {
      // all old nodes
      nodes.put(node.getObjectId(), node);
      
      
      if(node.getBlobId().isPresent()) {
        final var blobId = node.getBlobId().get();
        final var props = ref.get().getTransitives().getBlobsById().get(blobId);
        this.blobs.put(blobId, Optional.ofNullable(props));   
      }
      if(node.getPropsId().isPresent()) {
        final var propsId = node.getPropsId().get();
        final var props = ref.get().getTransitives().getPropsById().get(propsId);
        this.props.put(propsId, Optional.ofNullable(props));
      }
    }
  }

  public List<Node> rm(String idOrPath) {
    // 1. Find the target node (the one being explicitly deleted)
    final Node target = nodes.values().stream()
      .filter(n -> n.getId().equals(idOrPath) || n.getFullPath().equals(idOrPath) ||  n.getObjectId().equals(idOrPath) )
      .findFirst()
      .orElse(null);

    if (target == null) {
      return Collections.emptyList();
    }

    final var targetPathPrefix = target.getFullPath() + "/";

    // 2. Identify all victims (the target + all children)
    final List<Node> toRemove = nodes.values().stream()
        .filter(n -> n.getId().equals(target.getId()) || n.getObjectId().equals(target.getObjectId()) || n.getFullPath().startsWith(targetPathPrefix))
        .toList();

    // 3. Perform the actual removal from the internal state
    toRemove.forEach(n -> nodes.remove(n.getObjectId()));

    // 4. Return the list of removed nodes
    return toRemove;
  }
  
  public MergeTree add(Node node) {
    RepoAssert.isTrue(!sessionNodeIds.contains(node.getObjectId()), () -> "Can't add the same node multiple times: '" + node.getFullPath() + "'");
    RepoAssert.isTrue(!sessionNodePaths.contains(node.getFullPath()), () -> "Can't add the same node multiple times: '" + node.getFullPath() + "'");
    
    
    final var rename = isFolderRename(node);
    if(rename.isRename) {
      this.renames.add(rename);
    }
    
    // brand new node
    sessionNodePaths.add(node.getFullPath());
    sessionNodeIds.add(node.getObjectId());
    
    nodes.put(node.getObjectId(), node);
    
    if(nodes.values().stream().filter(t -> t.getFullPath().equals(node.getFullPath())).count() != 1) {
      RepoAssert.fail("Can't add the same node multiple times: '" + node.getFullPath() + "'");  
    }
    
    return this;
  }
  
  

  public MergeTree add(Blob blob) {
    // hash based check
    if(blobs.containsKey(blob.getId())) {
      return this;  
    } 
    
    // brand new node
    newBlobs.add(blob);
    blobs.put(blob.getId(), Optional.of(blob));
    return this;
  }
  
  public MergeTree add(Optional<Props> props) {
    if(props.isEmpty()) {
      return this;      
    }
    
    // hash based check
    if(this.props.containsKey(props.get().getId())) {
      return this;  
    } 
    
    // brand new node
    this.newProps.add(props.get());
    this.props.put(props.get().getId(), props);
    return this;
  }
  
  
  public MergeTreeResult close() {
    final Collection<Node> values = renames.isEmpty() ? this.nodes.values() : renameNodes();
    final Tree tree = Tree.newInstance(values).build();
    return new MergeTreeResult(tree, newProps, newBlobs);
  }
  
  private Collection<Node> renameNodes() {
    Collection<Node> nextState = this.nodes.values();
    for(final var rename : renames) {
      nextState = nextState.stream()
        .map(node -> renameNode(node, rename))
        .toList();
    }
    return nextState;
  }

  private RenameOp isFolderRename(Node next) {
    final var prev = nodes.get(next.getObjectId());
     
    if(prev == null) {
      return new RenameOp(false, prev, next);
    }
    
    boolean isRename = !prev.getFullPath().toLowerCase().equals(next.getFullPath().toLowerCase());
    return new RenameOp(isRename, prev, next);
  }
  
  
  public Node renameNode(Node anyNode, RenameOp rename) {
    // already renamed the src node
    if(rename.getNext().getId().equals(anyNode.getId())) {
      return anyNode;
    }
    // sanity check
    if (!rename.isRename()) {
      return anyNode;
    }
    

    final var oldPath = rename.getPrev().getFullPath().toLowerCase();
    final var currentPath = anyNode.getFullPath().toLowerCase();

    // 3. Check if anyNode is a descendant (is the node inside the renamed folder?)
    // We check if it starts with oldPath + "/" to avoid partial word matches
    if (currentPath.startsWith(oldPath + "/")) {
      
      // Calculate the new path by replacing the prefix
      final String newFullPath = rename.getNext().getFullPath() + 
                           anyNode.getFullPath().substring(rename.getPrev().getFullPath().length());
      
      // Split the newFullPath to separate the new node_path from node_name
      final int lastSlash = newFullPath.lastIndexOf("/");
      final String newNodePath = (lastSlash == -1) ? "" : newFullPath.substring(0, lastSlash);
      
      // Return a new Node instance with the updated path
      return ImmutableNode.builder().from(anyNode).nodePath(newNodePath).build(); 
    }

    return anyNode;
  }
  
  @Value
  public static class MergeTreeResult {
    Tree tree;
    List<Props> props;
    List<Blob> blobs;
  }
  
  @Value
  private static class RenameOp {
    boolean isRename;
    Node prev;
    Node next;
  }
}
