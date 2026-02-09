package io.resys.thena.fs.spi.snapshot;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFile;
import io.resys.thena.fs.api.commits.CommitBuilder.MergeFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.NewFile;
import io.resys.thena.fs.api.commits.CommitBuilder.NewFolder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.tables.FsDbBuilder.PersistenceUnit;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class Snapshot {
  private final Optional<Ref> lock;
  private final String branchName;
  
  private final ImmutablePersistenceUnit.Builder persistenceUnit = ImmutablePersistenceUnit.builder();  
  private final SnapshotLogger sp_logger = new SnapshotLogger();
  private final List<ChangeCommand> changes = new ArrayList<>();

  
  public interface ChangeCommand {};
  
  public static record RmCommand(String docId) implements ChangeCommand {}
  
  public static record NewFolderCommand(Consumer<NewFolder> consumer) implements ChangeCommand {}
  public static record NewFileCommand(Consumer<NewFile> consumer) implements ChangeCommand {}
  
  public static record MergeFolderCommand(String docId, BiConsumer<Node, MergeFolder> consumer) implements ChangeCommand {}
  public static record MergeFileCommand(String docId, BiConsumer<Node, MergeFile> consumer) implements ChangeCommand {}

  
  private Tuple2<Node, Props> visitNewFolderCommand(NewFolderCommand command) {
    final var merger = new NewFolderImpl(lock);
    command.consumer().accept(merger);
    final var result = merger.close();
    
    final var newNode = result.getNode();
    final var newProps = result.getProps();
    
    return Tuple2.of(newNode, newProps);
  }

  private Tuple3<Node, Props, Blob> visitNewFileCommand(NewFileCommand command) {
    final var merger = new NewFileImpl(lock);
    command.consumer().accept(merger);
    
    final var result = merger.close();
    final var newNode = result.getNode();
    final var newProps = result.getProps();
    final var newBlobs = result.getBlob();

    return Tuple3.of(newNode, newProps, newBlobs);
  }
  
  private Tuple2<Node, Props>  visitMergeFolderCommand(MergeFolderCommand command) {
    RepoAssert.isTrue(lock.isPresent(), () -> "lock is missing, no previous data, merge requires previous change into what to apply changes!");
    
    final var prev = lock.get();
    final var prevNode = prev.getTransitives().getNodesById().get(command.docId);
    RepoAssert.notNull(prevNode, () -> "Can't find target node(props) with id = '"+ command.docId + "'!");
    
    final var merger = new MergeFolderImpl(lock.get(), prevNode); 
    command.consumer().accept(prevNode, merger);
    
    final var result = merger.close();
    final var mergeNode = result.getNode();
    final var mergeProps = result.getProps();
    
    return Tuple2.of(mergeNode, mergeProps);
  }
  
  private Tuple3<Node, Props, Blob> visitMergeFileCommand(MergeFileCommand command) {
    RepoAssert.isTrue(this.lock.isPresent(), () -> "lock is missing, no previous data, merge requires previous change into what to apply changes!");
    
    final var prev = lock.get();
    final var prevNode = prev.getTransitives().getNodesById().get(command.docId);
    RepoAssert.notNull(prevNode, () -> "Can't find target node(props, blob) with id = '"+ command.docId + "'!");
    
    final var merger = new MergeFileImpl(prev, prevNode);
    command.consumer().accept(prevNode, merger);
    final var result = merger.close();
    
    final var mergeNode = result.getNode();
    final var mergeProps = result.getProps();
    final var mergeBlobs = result.getBlob();
    
    return Tuple3.of(mergeNode, mergeProps, mergeBlobs);
  }
    
  private Tree visitTree(List<Node> nodes, List<Props> props, List<Blob> blobs, List<Node> rm) {
    final Tree result = lock.isPresent() ?
      new MergeTree(lock.get(), nodes, props, blobs).close().getTree() : 
      new NewTree(nodes, props, blobs).close().getTree();
    
    if(lock.isPresent()) {
      sp_logger.mergeTree(lock.get().getTransitives().getTree(), result);
    } else {
      sp_logger.newTree(result);  
    }
    
    persistenceUnit.addTreeInserts(result);
    return result;
  }
  
  private Commit visitCommit(
      Tree tree, 
      String commitAuthor,
      String commitMessage,
      OffsetDateTime commitCreatedAt) {
    
    final var result = Commit.newInstance(tree.getId(), 
        lock.map(e -> e.getCommitId()), 
        Optional.empty(), 
        commitAuthor, commitCreatedAt, commitMessage
    ).build();
    
    if(lock.isPresent()) {
      sp_logger.mergeCommit(lock.get().getTransitives().getCommit(), result);
    } else {
      sp_logger.newCommit(result);  
    }
    
    persistenceUnit.addCommitInserts(result);
    return result;
  }
  
  private Ref visitBranch(Commit commit) {
    if(lock.isPresent()) {
      final var result = new MergeBranchImpl(lock.get(), commit).close();
      sp_logger.mergeBranch(lock.get(), result.getBranch());
      persistenceUnit.addRefUpdates(result.getBranch());
      return result.getBranch();
    }
    
    final var result = new NewBranchImpl(branchName, commit).close();
    sp_logger.newBranch(result.getBranch());
    persistenceUnit.addRefInserts(result.getBranch());
    return result.getBranch();
  }
  

  private List<Node> visitRemovals(List<String> removals, List<Node> newNodes) {
    // removal from known tree
    final List<Node> removalNodes = new ArrayList<>(); 
    if(lock.isPresent() && !removals.isEmpty()) {
      
      final var result = lock.get().getTransitives()
        .getTree().getTreeNodes()
        
        .stream().filter(node -> (
            
            removals.contains(node.getId()) || 
            removals.contains(node.getNodeId()) ||
            
            
            removals.contains(node.getNodePath()) || 
            removals.contains(node.getNodePath() + "/" + node.getNodeName())
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
            removals.contains(node.getNodePath()) || 
            removals.contains(node.getNodePath() + "/" + node.getNodeName())
        ))
        .toList();
      
      RepoAssert.isTrue(result.isEmpty(), () -> "Can't add and remove same data in the same tx, outcome is nothing!");
    }
    
    return removalNodes;
  }
  
  public Snapshot addAll(List<ChangeCommand> changes) {
    this.changes.addAll(changes);
    return this;
  }
  
  
  public PersistenceUnit build(
      String commitAuthor,
      String commitMessage,
      OffsetDateTime commitCreatedAt
      ) {
    
    final List<Node> nodes = new ArrayList<>();
    final List<Props> props = new ArrayList<>();
    final List<Blob> blobs = new ArrayList<>();
    final List<String> removals = new ArrayList<>();
    
    for(final var change : changes) {
      if(change instanceof RmCommand) {
      
        removals.add(((RmCommand) change).docId);
        
      } else if(change instanceof NewFolderCommand) {
        final var result = visitNewFolderCommand((NewFolderCommand) change);
        nodes.add(result.getItem1());
        props.add(result.getItem2());
        
      } else if(change instanceof NewFileCommand) {
        final var result = visitNewFileCommand((NewFileCommand) change);
        nodes.add(result.getItem1());
        props.add(result.getItem2());
        blobs.add(result.getItem3());
        
      } else if(change instanceof MergeFolderCommand) {
        final var result = visitMergeFolderCommand((MergeFolderCommand) change);
        nodes.add(result.getItem1());
        props.add(result.getItem2());
        
      } else if(change instanceof MergeFileCommand) {
        final var result = visitMergeFileCommand((MergeFileCommand) change);
        nodes.add(result.getItem1());
        props.add(result.getItem2());
        blobs.add(result.getItem3());
      }
      
      RepoAssert.fail("Unknown command: " + change.getClass().getSimpleName());
    }
    
    final var rm = visitRemovals(removals, nodes);

    // tree 
    final var tree = visitTree(nodes, props, blobs, rm);
    
    // commit
    final var commit = visitCommit(tree, commitAuthor, commitMessage, commitCreatedAt);
    
    // branch
    final var branch = visitBranch(commit);
    log.trace("Commiting to branch_name: '{}', commit_id: '{}', commit_message: '{}'", branch.getRefName(), commit.getId(), commit.getCommitMessage());
    
    // finalize 
    return persistenceUnit.build();
  }
}
