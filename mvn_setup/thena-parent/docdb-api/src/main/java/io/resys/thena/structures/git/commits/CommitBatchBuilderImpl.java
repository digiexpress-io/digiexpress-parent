package io.resys.thena.structures.git.commits;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.GitCommitActions.JsonObjectMerge;
import io.resys.thena.api.entities.git.ImmutableBlob;
import io.resys.thena.api.entities.git.ImmutableBranch;
import io.resys.thena.api.entities.git.ImmutableCommit;
import io.resys.thena.api.entities.git.ImmutableTree;
import io.resys.thena.api.entities.git.ImmutableTreeValue;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.resys.thena.structures.git.GitInserts.GitBatch;
import io.resys.thena.structures.git.ImmutableBatchRef;
import io.resys.thena.structures.git.ImmutableGitBatch;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.support.Sha2;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class CommitBatchBuilderImpl implements CommitBatchBuilder {

  private final CommitTreeState commitTree; 
  private String commitParent;
  private String commitAuthor;
  private String commitMessage;
  private Map<String, JsonObject> toBeInserted;
  private Map<String, JsonObjectMerge> toBeMerged;
  private Collection<String> toBeRemoved;

  
  @Override
  public GitBatch build() {
    final var mutator = new CommitTreeMutator();
    visitTree(commitTree, mutator);
    visitAppend(toBeInserted, toBeMerged, mutator, commitTree);
    visitRemove(toBeRemoved, mutator);
    
    
    final var nextTree = mutator.getNextTree();
    final var nextBlobs = mutator.getNextBlobs();
    final var blobs = nextBlobs.values();
    final var tree = ImmutableTree.builder().id(Sha2.treeId(nextTree)).values(nextTree).build();

    final var template = ImmutableCommit.builder()
      .id("commit-template")
      .dateTime(LocalDateTime.now())
      .tree(tree.getId())      
      .author(this.commitAuthor)
      .message(this.commitMessage)
      .parent(this.commitTree.getCommit().map(r -> r.getId()))
      .build();
    final var commit = ImmutableCommit.builder()
      .from(template)
      .id(Sha2.commitId(template))
      .build();
    final var ref = ImmutableBranch.builder()
        .commit(commit.getId())
        .name(this.commitTree.getRef().map(e -> e.getName()).orElse(this.commitTree.getRefName()))
        .build();
    final var log = ImmutableMessage.builder().text(mutator.getLogger().toString()).build();
    final var batch = ImmutableGitBatch.builder()
        .log(log)
        .ref(ImmutableBatchRef.builder().ref(ref).created(this.commitTree.getRef().isPresent()).build())
        .repo(commitTree.getRepo())
        .status(visitEmpty(mutator))
        .deleted(toBeRemoved.size())
        .tree(tree)
        .blobs(blobs)
        .commit(commit)
        .build();
    
     return batch;
  }
  
  private static void visitTree(CommitTreeState from, CommitTreeMutator mutator) {
    if(from.getTree().isPresent()) {
      mutator.getNextTree().putAll(from.getTree().get().getValues());
    }
  }
  
  private static BatchStatus visitEmpty(CommitTreeMutator mutator) {
    boolean isEmpty = !(mutator.isDataDeleted() || mutator.isDataAdded());
    return isEmpty ? BatchStatus.EMPTY : BatchStatus.OK;
  }
  
  private static void visitAppend(
      Map<String, JsonObject> newBlobs,
      Map<String, JsonObjectMerge> mergeBlobs,
      CommitTreeMutator mutator,
      CommitTreeState commitTree) {
    
    final var logger = mutator.getLogger();
    final var nextTree = mutator.getNextTree();
    final var nextBlobs = mutator.getNextBlobs();
    final List<RedundentHashedBlob> hashed = newBlobs.entrySet().stream()
      .map(CommitBatchBuilderImpl::visitAppendEntry)
      .collect(Collectors.toList());
    
    
    mergeBlobs.entrySet().stream()
      .map(entry -> CommitBatchBuilderImpl.visitMergeEntry(entry, commitTree))
      .forEach(hashed::add);
    
    
    for(RedundentHashedBlob entry : hashed) {
      logger
      .append(System.lineSeparator())
      .append("  + ").append(entry.getName());
      
      if(nextTree.containsKey(entry.getName())) {
        TreeValue previous = nextTree.get(entry.getName());
        if(previous.getBlob().equals(entry.getHash())) {
          logger.append(" | no changes");
          continue;
        }
        logger.append(" | changed"); 
      } else {
        logger.append(" | added");        
      }
      
      nextBlobs.put(entry.getHash(), ImmutableBlob.builder()
          .id(entry.getHash())
          .value(entry.getBlob())
          .build());
      nextTree.put(entry.getName(), ImmutableTreeValue.builder()
          .name(entry.getName())
          .blob(entry.getHash())
          .build());
      mutator.setDataAdded(true);
    }
    
    if(!hashed.isEmpty()) {
      logger.append(System.lineSeparator());
    }
  }
  
  private static void visitRemove(Collection<String> removeBlobs, CommitTreeMutator mutator) {
    final var logger = mutator.getLogger();
    final var nextTree = mutator.getNextTree();
    if(!removeBlobs.isEmpty()) {
      logger.append("Removing following:").append(System.lineSeparator());
    }
    for(String name : removeBlobs) {
      logger.append(System.lineSeparator()).append("  - ").append(name);
      if(nextTree.containsKey(name)) {
        nextTree.remove(name);
        mutator.setDataDeleted(true);
        logger.append(" | deleted");
      } else {
        logger.append(" | doesn't exist");
      }
    }
    if(!removeBlobs.isEmpty()) {
      logger.append(System.lineSeparator());
    }
  }
  
  private static RedundentHashedBlob visitAppendEntry(Map.Entry<String, JsonObject> entry) {
    return ImmutableRedundentHashedBlob.builder()
      .hash(Sha2.blobId(entry.getValue()))
      .blob(entry.getValue())
      .name(entry.getKey())
      .build();
  }
  private static RedundentHashedBlob visitMergeEntry(Map.Entry<String, JsonObjectMerge> entry, CommitTreeState commitTree) {
    final var treeValue = commitTree.getTree().get().getValues().get(entry.getKey());
    final var previous = commitTree.getBlobs().get(treeValue.getBlob());
    RepoAssert.notNull(previous, () -> "Can't merge object with id: '" + entry.getKey() + "' because it's not found!");
    
    final var next = entry.getValue().apply(previous.getValue());
    return ImmutableRedundentHashedBlob.builder()
      .hash(Sha2.blobId(next))
      .blob(next)
      .name(entry.getKey())
      .build();
  }  

}
