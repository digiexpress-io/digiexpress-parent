package io.resys.thena.fs.spi.commit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.resys.thena.fs.api.commits.CommitQuery.CommitsByObject;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.Value;



public class CommitTree {
  
  // null in case the main container 
  private final Commit commit;
  private final Optional<CommitTree> parent;
  
  // master ref
  private final Map<String, CommitTree> all_commitTrees;
  private final Set<String> all_objectIds;
  
  private final Map<String, CommitTree> children = new HashMap<>();
  private final Map<String, Ref> refs = new HashMap<>();
  private final Map<String, Node> nodes = new HashMap<>();

  public CommitTree() {
    this.commit = null;
    this.parent = Optional.empty();
    this.all_commitTrees = new HashMap<>();
    this.all_objectIds = new HashSet<>();
  }
  public CommitTree(Commit commit, Map<String, CommitTree> all_commitTrees, Set<String> all_objectIds) {
    this.commit = commit;
    this.parent = Optional.empty();
    this.all_commitTrees = all_commitTrees;
    this.all_objectIds = all_objectIds;
    
    this.all_commitTrees.put(commit.getId(), this);
    
  }  
  public CommitTree(Commit commit, Optional<CommitTree> parent, Map<String, CommitTree> all_commitTrees, Set<String> all_objectIds) {
    this.commit = commit;
    this.parent = parent;
    this.all_commitTrees = all_commitTrees;
    this.all_objectIds = all_objectIds;
    
    this.all_commitTrees.put(commit.getId(), this);
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
    return all_commitTrees.get(parentId).add(commit, refs, nodes);
  }
  
  private void accept(CommitTreeVisitor visitor) {
    final var prev = parent.orElse(null);
    final var next = this;
    visitor.visit(prev, next);
    children.values().forEach(child -> child.accept(visitor));
  }
  
  
  public Tuple2<UsedBlobsAndProps, Collection<CommitsByObject>> groupByObject() {
    final var visitor = new GroupByObject(all_objectIds);
    accept(visitor);
    return visitor.close();
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
    final var root = new CommitTree(commit, all_commitTrees, all_objectIds).addTransient(refs, nodes);
    this.children.put(commit.getId(), root);
    return root;
  }
  private CommitTree createChild(Commit commit, List<Ref> refs, Collection<Node> nodes) {
    RepoAssert.isTrue(commit == null, () -> "root can be only added to container");
    final var child = new CommitTree(commit, Optional.of(this), all_commitTrees, all_objectIds).addTransient(refs, nodes);
    this.children.put(commit.getId(), child);
    return child;
  }
  private CommitTree addTransient(List<Ref> refs, Collection<Node> nodes) {
    refs.forEach(ref -> this.refs.put(ref.getId(), ref));
    nodes.forEach(node -> {
      this.nodes.put(node.getId(), node);
      this.all_objectIds.add(node.getObjectId());
    });
    return this;
  }
  
  interface CommitTreeVisitor {
    void visit(CommitTree previous, CommitTree next);
  }
  
  
  private static class GroupByObject implements CommitTreeVisitor {
    private final Map<String, CommitsByObject> objects = new HashMap<>();
    private final Set<String> blobs = new HashSet<>();
    private final Set<String> props = new HashSet<>();
    
    public GroupByObject(Set<String> all_objectIds) {
      all_objectIds.forEach(id -> objects.put(id, new CommitsByObjectImpl(id)));
    }
    
    @Override
    public void visit(CommitTree previous, CommitTree next) {
      if(previous == null) {
        init(next);
      } else {
        diff(previous, next);
      }
    }

    public void init(CommitTree next) {
      for(final var entry : next.nodes.entrySet()) {
        final var objectId = entry.getValue().getObjectId();
        ((CommitsByObjectImpl) objects.get(objectId))
          .add(next.getCommit())
          .add(entry.getValue());
      }
    }
        
    public void diff(CommitTree previous, CommitTree next) {
      for(final var entry : next.nodes.entrySet()) {

        final var objectId = entry.getValue().getObjectId();
        final var prev_node = previous.nodes.get(objectId);
        final var next_node = next.nodes.get(objectId);
        
        final var isBlobChanged = prev_node.getBlobId().orElse("").equals(next_node.getBlobId().orElse(""));
        final var isPropsChanged = prev_node.getPropsId().orElse("").equals(next_node.getPropsId().orElse(""));
        
        if(isBlobChanged || isPropsChanged) {
          ((CommitsByObjectImpl) objects.get(objectId))
            .add(next.getCommit())
            .add(next_node);
        }
      }
    }

    public Tuple2<UsedBlobsAndProps, Collection<CommitsByObject>> close() {
      return Tuple2.of(new UsedBlobsAndProps(blobs, props), objects.values());
    }
  }
  
  @Value
  public static class UsedBlobsAndProps {
    Collection<String> blobs;
    Collection<String> props;
  }
}