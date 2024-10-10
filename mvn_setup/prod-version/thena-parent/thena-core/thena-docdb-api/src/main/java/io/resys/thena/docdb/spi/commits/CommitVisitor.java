package io.resys.thena.docdb.spi.commits;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.ObjectsActions.RefObjects;
import io.resys.thena.docdb.api.models.ImmutableBlob;
import io.resys.thena.docdb.api.models.ImmutableCommit;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ImmutableRef;
import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.ImmutableTreeValue;
import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.api.models.Repo;

public class CommitVisitor {
  
  
  @Value.Immutable
  interface RedundentCommitTree {
    boolean isEmpty();
    Map<String, TreeValue> getTreeValues();
    Map<String, Blob> getBlobs();
    String getLog();
  }

  @Value.Immutable
  public interface RedundentHashedBlob {
    String getName();
    String getHash();
    String getBlob();
  }
  
  @Value.Immutable
  public interface CommitInput {
    Optional<RefObjects> getParent();
    Repo getRepo();
    String getRef();
    String getCommitAuthor();
    String getCommitMessage();
    Map<String, String> getAppend();
    Collection<String> getRemove();
  }
  
  public enum CommitOutputStatus {
    OK, EMPTY, ERROR, CONFLICT
  }
  
  @Value.Immutable
  public interface CommitOutput {
    CommitOutputStatus getStatus();
    Repo getRepo();
    Message getLog();
    Ref getRef();
    Commit getCommit();
    Tree getTree();
    Collection<Blob> getBlobs();
    List<Message> getMessages();
  }
  
  private final Map<String, Blob> nextBlobs = new HashMap<>();
  private final Map<String, TreeValue> nextTree = new HashMap<>();
  private final StringBuilder logger = new StringBuilder();
  private boolean dataDeleted = false;
  private boolean dataAdded = false;
  
  
  public CommitOutput visit(CommitInput input) {
    if(input.getParent().isPresent()) {
      visitParent(input.getParent().get());
    }
    visitAppend(input.getAppend());
    visitRemove(input.getRemove());
    
    Tree tree = visitTree();
    Collection<Blob> blobs = visitBlobs();
    Commit commit = visitCommit(tree, input);
    
    return ImmutableCommitOutput.builder()
        .log(visitLog())
        .repo(input.getRepo())
        .ref(visitRef(commit, input))
        .status(visitEmpty())
        .tree(tree)
        .blobs(blobs)
        .commit(commit)
        .build();
  }
  private Ref visitRef(Commit commit, CommitInput input) {
    return ImmutableRef.builder()
        .commit(commit.getId())
        .name(input.getRef())
        .build();
  }
  
  private Commit visitCommit(Tree tree, CommitInput input) {
    final Optional<String> parent = input.getParent().map(r -> r.getCommit().getId());
    final Commit commitTemplate = ImmutableCommit.builder()
      .id("commit-template")
      .author(input.getCommitAuthor())
      .message(input.getCommitMessage())
      .dateTime(LocalDateTime.now())
      .parent(parent)
      .tree(tree.getId())
      .build();
    final Commit commit = ImmutableCommit.builder()
        .from(commitTemplate)
        .id(Sha2.commitId(commitTemplate))
        .build();
    
    return commit;
  }
  
  private Collection<Blob> visitBlobs() {
    final Collection<Blob> blobs = nextBlobs.values();
    return blobs;
  }
  
  private Tree visitTree() {
    final Tree tree = ImmutableTree.builder()
        .id(Sha2.treeId(nextTree))
        .values(nextTree)
        .build();
    return tree;
  }
  
  private CommitOutputStatus visitEmpty() {
    boolean isEmpty = !(dataDeleted || dataAdded);
    return isEmpty ? CommitOutputStatus.EMPTY : CommitOutputStatus.OK;
  }
  private Message visitLog() {
    return ImmutableMessage.builder().text(logger.toString()).build();
  }
  
  private void visitParent(RefObjects parent) {
    this.nextTree.putAll(parent.getTree().getValues());
  }
  
  private void visitAppend(Map<String, String> newBlobs) {
    List<RedundentHashedBlob> hashed = newBlobs.entrySet().stream()
      .map(this::visitAppendEntry)
      .collect(Collectors.toList());
    
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
      dataAdded = true;
    }
    
    if(!hashed.isEmpty()) {
      logger.append(System.lineSeparator());
    }
  }
  
  private void visitRemove(Collection<String> removeBlobs) {
    if(!removeBlobs.isEmpty()) {
      logger.append("Removing following:").append(System.lineSeparator());
    }
    for(String name : removeBlobs) {
      logger.append(System.lineSeparator()).append("  - ").append(name);
      if(nextTree.containsKey(name)) {
        nextTree.remove(name);
        dataDeleted = true;
        logger.append(" | deleted");
      } else {
        logger.append(" | doesn't exist");
      }
    }
    if(!removeBlobs.isEmpty()) {
      logger.append(System.lineSeparator());
    }
  }
  
  private RedundentHashedBlob visitAppendEntry(Map.Entry<String, String> entry) {
    return ImmutableRedundentHashedBlob.builder()
      .hash(Sha2.blobId(entry.getValue()))
      .blob(entry.getValue())
      .name(entry.getKey())
      .build();
  }
}
