package io.resys.thena.fs.spi.committree;

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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.resys.thena.fs.api.commits.CommitQuery.CommitsByObject;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.RepoAssert;



public class CommitTree {
  private final CommitTreeCache cache;
  
  
  // null in case the main container 
  private final Commit commit;
  private final Optional<CommitTree> parent;

  
  private final Map<UUID, CommitTree> children = new HashMap<>();
  private final Map<UUID, Ref> refs = new HashMap<>();
  private final Map<UUID, Node> nodes = new HashMap<>();
  private final Map<String, Node> nodes_by_objectId = new HashMap<>();

  public CommitTree() {
    this.commit = null;
    this.parent = Optional.empty();
    this.cache = new CommitTreeCache();
  }
  private CommitTree(Commit commit, CommitTreeCache cache) {
    this.commit = commit;
    this.parent = Optional.empty();
    this.cache = cache;
    this.cache.add(this);
    
  }  
  private CommitTree(Commit commit, Optional<CommitTree> parent, CommitTreeCache cache) {
    this.commit = commit;
    this.parent = parent;
    this.cache = cache;
    this.cache.add(this);
  }  

  public CommitTree add(Commit commit, List<Ref> refs, Collection<Node> nodes) {
    // root commit
    if(commit.getParentId().isEmpty()) {
      return createRoot(commit, refs, nodes); 
    }
    
    // already added
    if(this.commit.getId().equals(commit.getId())) {
      return updateNode(commit, refs, nodes);
    }
    
    // new child
    if(this.commit.getId().equals(commit.getParentId().get())) {
      return createChild(commit, refs, nodes);
    }
    
    // find parent and delegate
    final var parentId = commit.getParentId().get();
    return cache.getTreeByCommitId(parentId).add(commit, refs, nodes);
  }
  
  public void accept(CommitTreeVisitor visitor) {
    final var prev = parent.orElse(null);
    final var next = this;
    visitor.visit(prev, next);
    children.values().forEach(child -> child.accept(visitor));
  }
  
  public CommitTree addAllBlobs(List<Blob> blobs) {
    cache.addAllBlobs(blobs);
    return this;
  }
  
  public CommitTree addAllProps(List<Props> props) {
    cache.addAllProps(props);
    return this;
  }
  
  public CommitTreeBlobsAndProps getDeps() {
    return cache.getDeps();
  }
  
  public Collection<CommitsByObject> groupByObject() {
    final var visitor = new GroupByObject(cache);
    accept(visitor);
    return visitor.close();
  }
  public Set<Entry<UUID, Node>> getNodes() {
    return nodes.entrySet();
  }
  public Node getNode(UUID nodeId) {
    return nodes.get(nodeId);
  }
  public Node getNode(String objectId) {
    return nodes_by_objectId.get(objectId);
  }
  public Commit getCommit() {
    return commit;
  }
  
  private CommitTree updateNode(Commit commit, List<Ref> refs, Collection<Node> nodes) {
    addTransient(refs, nodes);
    return this;
  }
  private CommitTree createRoot(Commit commit, List<Ref> refs, Collection<Node> nodes) {
    RepoAssert.isTrue(this.commit == null, () -> "root can be only added to container");
    final var root = new CommitTree(commit, this.cache).addTransient(refs, nodes);
    this.children.put(commit.getId(), root);
    return root;
  }
  private CommitTree createChild(Commit commit, List<Ref> refs, Collection<Node> nodes) {
    RepoAssert.isTrue(commit == null, () -> "root can be only added to container");
    final var child = new CommitTree(commit, Optional.of(this), this.cache).addTransient(refs, nodes);
    this.children.put(commit.getId(), child);
    return child;
  }
  private CommitTree addTransient(List<Ref> refs, Collection<Node> nodes) {
    refs.forEach(ref -> this.refs.put(ref.getId(), ref));
    nodes.forEach(node -> {
      this.nodes.put(node.getId(), node);
      this.nodes_by_objectId.put(node.getObjectId(), node);
      this.cache.add(node);
    });
    return this;
  }
}
