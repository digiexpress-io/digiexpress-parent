package io.digiexpress.eveli.mig.v6.assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent.ObjectType;
import io.digiexpress.eveli.mig.v6.assets.CommitNode.AddNodeOperation;
import io.digiexpress.eveli.mig.v6.assets.CommitNode.MergeNodeOperation;
import io.digiexpress.eveli.mig.v6.assets.CommitNode.NodeOperation;
import io.digiexpress.eveli.mig.v6.assets.CommitNode.RmNodeOperation;
import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.Commit;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.TreeValue;
import io.resys.thena.support.RepoAssert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CommitNodeBuilder {
  private final ObjectType type;
  private final OldGit.OldGitObjects git;
  private final Map<String, String> reverseTree = new HashMap<>();
  private CommitNode_Impl root;

  private void visitCommit(OldGit.Commit commit) {
    // add only
    final var tree_now = git.getTrees().get(commit.getTree());
    if(commit.getParent().isEmpty()) {

      final var nodeOperations = new ArrayList<NodeOperation>();
      for(final var value :  tree_now.getValues().values()) {
        nodeOperations.add(visitAdd(commit, value));  
      }
      
      this.root = new CommitNode_Impl(git, type, commit, nodeOperations, Optional.empty());
    } else {
      final var parentId = commit.getParent().get();
      final var parentCommit = git.getCommits().get(parentId);
      
      final var tree_before = git.getTrees().get(parentCommit.getTree());
      final var objects = ImmutableSet.<String>builder()
          .addAll(tree_now.getValues().keySet())
          .addAll(tree_before.getValues().keySet())
          .build();
      
      final var nodeOperations = new ArrayList<NodeOperation>();
      for(final var objectId : objects) {
        final var object_now = tree_now.getValues().get(objectId);
        final var object_before = tree_before.getValues().get(objectId);
        
        if(object_before != null && object_now != null) {
          nodeOperations.add(visitMerge(commit, object_now, object_before));
        } else if(object_now != null) {
          nodeOperations.add(visitAdd(commit, object_now));
        } else {
          nodeOperations.add(visitRm(commit, object_before));
        }
      }
      this.root.add(commit, nodeOperations);
    }
    
    
    final var nextCommitId = reverseTree.get(commit.getId());
    if(nextCommitId == null) {
      return;
    }
    visitCommit(git.getCommits().get(nextCommitId));
  }
  
  
  private AddNodeOperation visitAdd(OldGit.Commit commit, TreeValue now) {
    return new AddNodeOperation(now);
  }
  private MergeNodeOperation visitMerge(OldGit.Commit commit, TreeValue now, TreeValue before) {
    return new MergeNodeOperation(before, now);
  }  
  private RmNodeOperation visitRm(OldGit.Commit commitThatDoesNotContainRemoved, TreeValue removed) {
    return new RmNodeOperation(removed);
  }

  public CommitNode_Impl build() {
    OldGit.Commit firstCommit = null;
    for(final var commit : git.getCommits().values()) {
      if(commit.getParent().isEmpty()) {
        firstCommit = commit;
      } else {
        final var parentId = commit.getParent().get();
        RepoAssert.isTrue(!reverseTree.containsKey(parentId), () -> "Cant find first commit, " + git.getTenant() + "!");    
        reverseTree.put(commit.getParent().get(), commit.getId());
      }
    }
    RepoAssert.notNull(firstCommit, () -> "Cant find first commit, " + git.getTenant() + "!");    
    visitCommit(firstCommit);
    
    return new CommitNode_Impl(git, type, null, Collections.emptyList(), Optional.empty()).add(root);
    
  }
  
  @RequiredArgsConstructor
  @Getter
  public class CommitNode_Impl implements CommitNode {
    private final OldGit.OldGitObjects src;
    private final ObjectType type;
    private final Commit commit;
    private final List<NodeOperation> nodeOperations;
    private final Optional<CommitNode> previous;
    
    private Optional<CommitNode> next = Optional.empty();
    
    
    public CommitNode_Impl add(Commit commit, List<NodeOperation> op) {
      if(this.commit.getId().equals(commit.getParent().get())) {
        this.next = Optional.of(new CommitNode_Impl(src, type, commit, op, Optional.ofNullable(this)));
      } else {
        ((CommitNode_Impl)this.next.get()).add(commit, op);
      }
      
      return this;
    }
    public CommitNode_Impl add(CommitNode_Impl next) {
      this.next = Optional.of(next);
      return this;
    }
  }
}
