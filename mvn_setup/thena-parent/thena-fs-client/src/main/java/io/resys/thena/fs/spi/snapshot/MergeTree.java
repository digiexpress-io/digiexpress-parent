package io.resys.thena.fs.spi.snapshot;

import java.util.ArrayList;

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
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import lombok.Value;



public class MergeTree {
  private final Optional<Ref> ref;
  private final Map<String, Node> nodes = new HashedMap<>();
  
  private final Map<String, Optional<Props>> props = new HashedMap<>();
  private final Map<String, Optional<Blob>> blobs = new HashedMap<>();
  
  private final List<Blob> newBlobs = new ArrayList<>();
  private final List<Props> newProps = new ArrayList<>();
  
  
  public MergeTree(Optional<Ref> ref) {
    super();
    this.ref = ref;
    
    if(ref.isPresent()) {
      final var prevTree = ref.get().getTransitives().getTree();
      for(final var node : prevTree.getTreeNodes()) {
        
        nodes.put(node.getId(), node);
        
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
  }

  public void rm(String idOrPath) {
    
  }
  public MergeTree add(Node node) {
    // hash based check
    if(nodes.containsKey(node.getId())) {
      return this;  
    } 
    
    // brand new node
    nodes.put(node.getId(), node);
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
    final var tree = Tree.newInstance(this.nodes.values()).build();
    return new MergeTreeResult(tree, newProps, newBlobs);
  }
  
  @Value
  public static class MergeTreeResult {
    Tree tree;
    List<Props> props;
    List<Blob> blobs;
  }
  
  /*
  private List<Node> visitRemovals(List<String> removals, List<Node> newNodes) {
    // removal from known tree
    final List<Node> removalNodes = new ArrayList<>(); 
    if(lock.isPresent() && !removals.isEmpty()) {
      
      final var result = lock.get().getTransitives()
        .getTree().getTreeNodes()
        
        .stream().filter(node -> (
            
            removals.contains(node.getId()) || 
            removals.contains(node.getNodeId()) ||
            removals.contains(node.getFullPath()) ||
            
            (node.getNodePath().isPresent() && removals.contains(node.getNodePath().get()))
        ))
        .toList();
      
      removalNodes.addAll(result);
      sp_logger.rmNodes(result);
    }
    
  if(!removals.isEmpty()) {
      final var result = newNodes
        .stream().filter(node -> (
            removals.contains(node.getId()) || 
            removals.contains(node.getNodeId()) ||
            
            removals.contains(node.getFullPath()) ||
            (node.getNodePath().isPresent() && removals.contains(node.getNodePath().get()))
        ))
        .toList();
      
      RepoAssert.isTrue(result.isEmpty(), () -> "Can't add and remove same data in the same tx, outcome is nothing!");
    }
    
    return removalNodes;
  }
  */

}
