package io.resys.thena.docdb.spi.doc.commits;

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
import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.models.ImmutableDoc;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.spi.DocDbInserts.DocBatch;
import io.resys.thena.docdb.spi.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.spi.ImmutableDocBatch;
import io.resys.thena.docdb.spi.OidUtils;
import io.resys.thena.docdb.spi.git.commits.CommitLogger;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.spi.support.Sha2;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class DocCommitBatchBuilderImpl implements DocCommitBatchBuilder {

  private final DocCommitState currentState;
  
  // changes to current state
  private String commitParent;
  private String commitAuthor;
  private String commitMessage;
  private JsonObject toBeInserted;
  private JsonObjectMerge toBeMerged;
  private JsonObject toBeLogged;
  private boolean toBeRemoved;

  
  @Override
  public DocBatch build() {
    final var branchId = OidUtils.gen();
    
    final var doc = currentState.getDoc().orElse(ImmutableDoc.builder()
        .id(Optional.ofNullable(currentState.getDocId()).orElse(OidUtils.gen()))
        .externalId(Optional.ofNullable(currentState.getExternalId()).orElse(OidUtils.gen()))
        .build());
    
    final var template = ImmutableDocCommit.builder()
      .id("commit-template")
      .docId(doc.getId())
      .branchId(branchId)
      .dateTime(LocalDateTime.now())      
      .author(this.commitAuthor)
      .message(this.commitMessage)
      .parent(this.currentState.getCommit().map(r -> r.getId()))
      .build();
    final var commit = ImmutableDocCommit.builder()
      .from(template)
      .id(Sha2.commitId(template))
      .branchId(branchId)
      .build();
    final var docBranch = ImmutableDocBranch.builder()
      .id(OidUtils.gen())
      .branchId(branchId)
      .docId(doc.getId())
      .commitId(commit.getId())
      .branchName(this.currentState.getBranch().map(e -> e.getBranchName()).orElse(this.currentState.getBranchName()))
      .value(Optional.ofNullable(toBeMerged).map(e -> e.apply(currentState.getBranch().get().getValue())).orElse(toBeInserted))
      .build();
    
    final var docLogs = Optional.ofNullable(toBeLogged).map((value) -> ImmutableDocLog.builder()
        .id(OidUtils.gen())
        .docCommitId(commit.getId())
        .value(value)
        .build());
  
    final var logger = new CommitLogger();
    visitAddLogger(logger, docBranch);
    visitDeleteLogger(logger);
    
    final var log = ImmutableMessage.builder().text(logger.toString()).build();
    final var batch = ImmutableDocBatch.builder()
        .repo(currentState.getRepo())
        .status(visitEmpty())
        .doc(doc)
        .docBranch(docBranch)
        .docCommit(commit)
        .docLogs(docLogs)
        .log(log)
        .build();
     return batch;
  }
  
  
  
  
  private BatchStatus visitEmpty() {
    boolean isEmpty = toBeMerged == null && toBeInserted == null && toBeRemoved;
    return isEmpty ? BatchStatus.EMPTY : BatchStatus.OK;
  }
  
  private void visitAddLogger(CommitLogger logger, DocBranch branch) {
    if(this.toBeInserted == null && this.toBeMerged == null) {
      return;
    }
    
    final var hash = Sha2.blobId(branch.getValue());
    
    if(this.toBeMerged != null) {
      RepoAssert.isTrue(currentState.getBranch().isPresent(), () -> "Can't merge branch object with id: '" + currentState.getDocId() + "' because it's not found!");
      final var previous = Sha2.blobId(currentState.getBranch().get().getValue());
      if(previous.equals(hash)) {
        logger.append(" | no changes");
      } else {
        logger.append(" | changed");        
      }
    } else {
      logger.append(" | added");
    }
    
    logger
      .append(System.lineSeparator())
      .append("  + ").append(branch.getId());
    
    logger.append(System.lineSeparator());

  }
  
  private void visitDeleteLogger(CommitLogger logger) {
    if(!this.toBeRemoved) {
      return;
    }
    logger.append("Removing following:").append(System.lineSeparator());
    
    
    logger.append(System.lineSeparator()).append("  - ")
      .append(this.currentState.getBranch().get().getId())
      .append("/")
      .append(this.currentState.getBranch().get().getBranchName())
      .append(" | deleted");
    
    logger.append(System.lineSeparator());
  }
}
