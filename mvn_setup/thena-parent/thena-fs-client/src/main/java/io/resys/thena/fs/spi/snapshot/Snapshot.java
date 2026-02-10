package io.resys.thena.fs.spi.snapshot;

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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.MergeFileCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.MergeFolderCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.NewFileCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.NewFolderCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.RmCommand;
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
  private final String tenantId;
  private final Optional<Ref> lock;
  private final String branchName;
  private final OffsetDateTime now;
  
  private final ImmutablePersistenceUnit.Builder persistenceUnit = ImmutablePersistenceUnit.builder();  
  private final SnapshotLogger sp_logger = new SnapshotLogger();
  
  private final List<ChangeCommand> updates = new ArrayList<>();
  private final List<ChangeCommand> creates = new ArrayList<>();
  private final List<RmCommand> removals = new ArrayList<>();
  
  

  private Tuple2<Node, Optional<Props>> visitNewFolderCommand(MergeTree mergeTree, NewFolderCommand command) {
    final var merger = new NewFolderImpl();
    command.consumer().accept(merger);
    final var result = merger.close();
    
    
    final var newNode = result.getNode();
    final var newProps = result.getProps();
    
    mergeTree.add(newNode).add(newProps);
    return Tuple2.of(newNode, newProps);
  }

  private Tuple3<Node, Optional<Props>, Blob> visitNewFileCommand(MergeTree mergeTree, NewFileCommand command) {
    final var merger = new NewFileImpl();
    command.consumer().accept(merger);
    
    final var result = merger.close();
    final var newNode = result.getNode();
    final var newProps = result.getProps();
    final var newBlobs = result.getBlob();

    mergeTree.add(newNode).add(newProps).add(newBlobs);
    return Tuple3.of(newNode, newProps, newBlobs);
  }
  
  private Tuple2<Node, Optional<Props>> visitMergeFolderCommand(MergeTree mergeTree, MergeFolderCommand command) {
    RepoAssert.isTrue(lock.isPresent(), () -> "lock is missing, no previous data, merge requires previous change into what to apply changes!");
    
    final var prev = lock.get();
    final var prevNode = prev.getTransitives().getNodesById().get(command.docId());
    RepoAssert.notNull(prevNode, () -> "Can't find target node(props) with id = '"+ command.docId() + "'!");
    
    final var merger = new MergeFolderImpl(prevNode); 
    command.consumer().accept(prevNode, merger);
    
    final var result = merger.close();
    final var mergeNode = result.getNode();
    final var mergeProps = result.getProps();
    mergeTree.add(mergeNode).add(mergeProps);
    return Tuple2.of(mergeNode, mergeProps);
  }
  
  private Tuple3<Node, Optional<Props>, Blob> visitMergeFileCommand(MergeTree mergeTree, MergeFileCommand command) {
    RepoAssert.isTrue(this.lock.isPresent(), () -> "lock is missing, no previous data, merge requires previous change into what to apply changes!");
    
    final var prev = lock.get();
    final var prevNode = prev.getTransitives().getNodesById().get(command.docId());
    RepoAssert.notNull(prevNode, () -> "Can't find target node(props, blob) with id = '"+ command.docId() + "'!");
    
    final var merger = new MergeFileImpl(prevNode);
    command.consumer().accept(prevNode, merger);
    final var result = merger.close();
    
    final var mergeNode = result.getNode();
    final var mergeProps = result.getProps();
    final var mergeBlobs = result.getBlob();
    
    mergeTree
      .add(mergeNode)
      .add(mergeProps)
      .add(mergeBlobs);
    return Tuple3.of(mergeNode, mergeProps, mergeBlobs);
  }
    
  private Tree visitTree(MergeTree merger) {
    final var result = merger.close();
    persistenceUnit.addAllBlobInserts(result.getBlobs());
    persistenceUnit.addAllPropsInserts(result.getProps());
    
    final Tree tree = result.getTree(); 
    
    if(lock.isPresent()) {
      sp_logger.mergeTree(lock.get().getTransitives().getTree(), tree);
    } else {
      sp_logger.newTree(tree);
    }
    
    persistenceUnit.addTreeInserts(tree);
    return tree;
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
  
  
  private void visitCommand(MergeTree mergeTree, MergeIndex mergeIndex, ChangeCommand change) {
    if(change instanceof RmCommand) {
      
      // do nothing
      final var result = mergeTree.rm(((RmCommand) change).docId());
      mergeIndex.rm(result);
          
    } else if(change instanceof NewFolderCommand) {
      final var result = visitNewFolderCommand(mergeTree, (NewFolderCommand) change);
      mergeIndex.create(result.getItem1());
    } else if(change instanceof NewFileCommand) {
      final var result = visitNewFileCommand(mergeTree, (NewFileCommand) change);
      mergeIndex.create(result.getItem1());
      
    } else if(change instanceof MergeFolderCommand) {
      final var result = visitMergeFolderCommand(mergeTree, (MergeFolderCommand) change);
      mergeIndex.create(result.getItem1());
    } else if(change instanceof MergeFileCommand) {
      final var result = visitMergeFileCommand(mergeTree, (MergeFileCommand) change);
      mergeIndex.create(result.getItem1());
    } else {
      RepoAssert.fail("Unknown command: " + change.getClass().getSimpleName());        
    }
  }
  
  
  public Snapshot addAll(List<ChangeCommand> changes) {
    for(final var anyCommand : changes) {
      if(anyCommand instanceof RmCommand) {
        removals.add((RmCommand) anyCommand);
        
      } else if(anyCommand instanceof NewFolderCommand) {
        creates.add(anyCommand);
      } else if(anyCommand instanceof NewFileCommand) {
        creates.add(anyCommand);
        
      } else if(anyCommand instanceof MergeFolderCommand) {
        updates.add(anyCommand);
      } else if(anyCommand instanceof MergeFileCommand) {
        updates.add(anyCommand);

      } else {
        RepoAssert.fail("Unknown command: " + anyCommand.getClass().getSimpleName());        
      }
    }
    return this;
  }
  
  public PersistenceUnit build(
      String commitAuthor,
      String commitMessage,
      OffsetDateTime commitCreatedAt
      ) {
    
    // check if lock is loaded correctly
    if(this.lock.isPresent()) {
      final var lock = this.lock.get();
      RepoAssert.isTrue(lock.getTransitives() != null, () -> "lock transitives must be loaded!");
      RepoAssert.isTrue(lock.getTransitives().getCommit() != null, () -> "lock transitives.commit must be loaded!");
      RepoAssert.isTrue(lock.getTransitives().getTree() != null, () -> "lock transitives.tree must be loaded!");
    }
    
    final var mergeIndex = new MergeIndex(this.lock, this.now);
    final var mergeTree = new MergeTree(this.lock);
    
    this.creates.forEach(command -> visitCommand(mergeTree, mergeIndex, command));
    this.updates.forEach(command -> visitCommand(mergeTree, mergeIndex, command));
    this.removals.forEach(command -> visitCommand(mergeTree, mergeIndex, command));

    // tree
    final var tree = visitTree(mergeTree);

    // commit
    final var commit = visitCommit(tree, commitAuthor, commitMessage, commitCreatedAt);

    // index
    final var index = mergeIndex.close(commit);
    
    // branch
    final var branch = visitBranch(commit);
    log.trace("Commiting to branch_name: '{}', commit_id: '{}', commit_message: '{}'", branch.getRefName(), commit.getId(), commit.getCommitMessage());
    
    // finalize 
    return persistenceUnit
        .status(BatchStatus.OK)
        .tenantId(tenantId)
        .addAllObjectIndexInserts(index.getInserts())
        .addAllObjectIndexUpdates(index.getUpdates())
        .log("")
        .build();
  }
}
