package io.resys.thena.fs.spi.commit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.smallrye.mutiny.tuples.Tuple3;

public class CommitTreeBuilder {
  private static final String FIRST_COMMIT = "FIRST_COMMIT";
  private final Set<String> blobIds = new HashSet<>();
  private final Set<String> propIds = new HashSet<>();

  private final Map<String, Commit> commits = new HashMap<>();
  private final Map<String, Node> nodes = new HashMap<>();
  private final Map<String, Ref> refs = new HashMap<>();
  
  // parent id - (1..n) child commits
  private final Map<String, Map<String, Commit>> commitsByParent = new HashMap<>();
  
  // commit id - (1..n) ref id
  private final Map<String, Collection<String>> commitRefs = new HashMap<>();
  
  // commit id - (1..n) node
  private final Map<String, Map<String, Node>> commitNodes = new HashMap<>();
  
  public CommitTreeBuilder(List<Tuple3<Commit, Ref, Node>> rows) {
    // populate caches
    for(final var row : rows) {
      final var commit = row.getItem1();
      final var ref = row.getItem2();
      final var node = row.getItem3();
      
      // collect payload id-s
      row.getItem3().getBlobId().ifPresent(blobIds::add);     
      row.getItem3().getPropsId().ifPresent(propIds::add);     
      
      // store by id
      commits.put(commit.getId(), commit);
      nodes.put(node.getId(), node);
      refs.put(ref.getId(), ref);
      
      if(!commitNodes.containsKey(commit.getId())) {
        commitNodes.put(commit.getId(), new HashMap<>());
      }
      commitNodes.get(commit.getId()).put(node.getId(), node);
      
      // 
      if(!commitRefs.containsKey(commit.getId())) {
        commitRefs.put(commit.getId(), new HashSet<>());        
      }
      commitRefs.get(commit.getId()).add(row.getItem2().getId());

      
      // commit by parent id
      final var parentId = commit.getParentId().orElse(FIRST_COMMIT);
      if(!commitsByParent.containsKey(parentId)) {
        commitsByParent.put(parentId, new HashMap<String, Commit>());
      } 
      commitsByParent.get(parentId).put(commit.getId(), commit);
    }
  }
  
  
  private void traverse(CommitTree root, Commit parent) {
    final var parentId = parent.getId();
    final var children = Optional.ofNullable(commitsByParent.get(parentId))
      .map(Map::values)
      .orElse(Collections.emptyList());
    
    children.forEach(child -> {
      root.add(child, getCommitRefs(child), getCommitNodes(child));
      traverse(root, child);
    });
  } 
  
  private List<Ref> getCommitRefs(Commit commit) {
    return Optional.ofNullable(commitRefs.get(commit.getId()))
        .orElse(Collections.emptyList())
        .stream().map(refId -> refs.get(refId))
        .toList();
  }
  
  private Collection<Node> getCommitNodes(Commit commit) {
    return Optional.ofNullable(commitNodes.get(commit.getId()))
        .map(Map::values)
        .orElse(Collections.emptyList());
  }
  
  
  public CommitTree build() {
    // start building from parents
    final var root = new CommitTree();
    
    final var tips = commitsByParent.get(FIRST_COMMIT).values().stream()
      .map(item -> root.add(item, getCommitRefs(item), getCommitNodes(item)))
      .toList();

    tips.forEach(tip -> traverse(tip, tip.getCommit()));
    
    return root;
  }
}