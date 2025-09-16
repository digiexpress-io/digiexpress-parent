package io.resys.thena.doc.spi.support;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommands;
import io.resys.thena.doc.api.DocDataSource.DocState;
import io.resys.thena.doc.api.DocInserts.DocBatchForOne;
import io.resys.thena.doc.api.ImmutableDocBatchForOne;
import io.resys.thena.doc.spi.commitlog.DocCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;



@Setter @Accessors(fluent = true)
public class BatchForOneDocModify {

  private final DocLock docLock;
  private final DocState tx;
  private final String author;
  private final String message;
  private final Boolean excludeBranchContentFromLog;
  private final boolean commitTreeEnabled;
  
  private List<JsonObject> commands = null;
  private Optional<String> ownerId;
  private Optional<String> parentId;
  private Optional<String> externalId;
  private Optional<String> docName;
  private Optional<String> docDescription;
  private Optional<String> docSubStatus;
  private Optional<OffsetDateTime> docStartsAt;
  private Optional<OffsetDateTime> docEndsAt;
  private Optional<JsonObject> meta;
  private boolean remove;


  public BatchForOneDocModify(
      DocLock docLock, DocState tx, String author,
      String message, Boolean excludeBranchContentFromLog,
      boolean commitTreeEnabled) {
    super();
    this.docLock = docLock;
    this.tx = tx;
    this.author = author;
    this.message = message;
    this.commitTreeEnabled = commitTreeEnabled;
    this.excludeBranchContentFromLog = Boolean.TRUE.equals(excludeBranchContentFromLog);
  }
  
  public DocBatchForOne create() {
    RepoAssert.notNull(docLock, () -> "docLock can't be empty!");
    RepoAssert.notNull(tx, () -> "tx to commit, no content!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    
    final var now = OffsetDateTime.now();
    
    final var commitBuilder = DocCommitBuilder.from(tx.getTenantId(), docLock)
      .excludeBranchContentFromLog(excludeBranchContentFromLog)
      .commitTreeEnabled(commitTreeEnabled)
      .commitMessage(this.message)
      .commitAuthor(this.author)
      .create();
  

    final var doc = ImmutableDoc.builder()
      .from(docLock.getDoc().get())
      .meta(meta == null ? docLock.getDoc().map(e -> e.getMeta()).orElse(null) : meta.orElse(null))
      .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
      
      .name(this.docName == null ? docLock.getDoc().get().getName() : this.docName.orElse(null))
      .description(this.docDescription == null? docLock.getDoc().get().getDescription() : this.docDescription.orElse(null))
      .subStatus(this.docSubStatus == null ? docLock.getDoc().get().getSubStatus() : this.docSubStatus.orElse(null))
      .startsAt(this.docStartsAt == null ? docLock.getDoc().get().getStartsAt() : this.docStartsAt.orElse(null))
      .endsAt(this.docEndsAt == null ? docLock.getDoc().get().getEndsAt() : this.docEndsAt.orElse(null))
      
      .parentId(this.parentId == null ? docLock.getDoc().get().getParentId() : this.parentId.orElse(null))
      .ownerId(this.ownerId == null ? docLock.getDoc().get().getOwnerId() : this.ownerId.orElse(null))
      .externalId(this.externalId == null ? docLock.getDoc().get().getExternalId() : this.externalId.orElse(null))
      .commitId(commitBuilder.getCommitId())
      .build();
    
    commitBuilder.merge(docLock.getDoc().get(), doc);
    
    final var batchBuilder = ImmutableDocBatchForOne.builder();
    
    for(final var lock : docLock.getBranches()) {
      if(remove && lock.getBranch().get().getStatus() == Doc.DocStatus.ARCHIVED) {
        continue;
      }
      
      final var docBranch = ImmutableDocBranch.builder()
        .from(lock.getBranch().get())
        .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
        .branchName(remove ? OidUtils.gen(): lock.getBranch().get().getBranchName())
        .build();
      
      batchBuilder.addDocBranch(docBranch);
      commitBuilder.merge(lock.getBranch().get(), docBranch);
    }
  
    final List<DocCommands> docLogs = commands == null ? Collections.emptyList() : Arrays.asList(
        ImmutableDocCommands.builder()
          .id(OidUtils.gen())
          .docId(doc.getId())
          .commitId(commitBuilder.getCommitId())
          .commands(commands)
          .createdAt(now)
          .createdBy(author)
          .build()
        );
    docLogs.forEach(command -> commitBuilder.add(command));
    
    final var commit = commitBuilder.close();
    
    return commit.merge(batchBuilder
        .doc(doc)
        .log("")
        .addAllDocCommands(docLogs)
        .addAllDocLock(docLock.getBranches())
        .build());
  }
}
