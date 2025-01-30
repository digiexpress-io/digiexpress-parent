package io.resys.thena.structures.doc.support;

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
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.doc.commitlog.DocCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class BatchForOneDocModify {

  private final DocLock docLock;
  private final DocState tx;
  private final String author;
  private final String message;
  
  private List<JsonObject> commands = null;
  private Optional<String> ownerId;
  private Optional<String> parentId;
  private Optional<String> externalId;
  private Optional<String> docName;
  private Optional<String> docSubStatus;
  private Optional<OffsetDateTime> docStartsAt;
  private Optional<OffsetDateTime> docEndsAt;
  private Optional<JsonObject> meta;
  private boolean remove;

  
  
  public DocBatchForOne create() {
    RepoAssert.notNull(docLock, () -> "docLock can't be empty!");
    RepoAssert.notNull(tx, () -> "tx to commit, no content!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    
    final var now = OffsetDateTime.now();
    
    final var commitBuilder = new DocCommitBuilder(tx.getTenantId(), false, ImmutableDocCommit.builder()
        .id(OidUtils.gen())
        .docId(docLock.getDoc().get().getId())
        .createdAt(now)
        .commitAuthor(this.author)
        .commitMessage(this.message)
        .parent(docLock.getDoc().get().getCommitId())
        .commitLog("")
        .build());
    

    final var doc = ImmutableDoc.builder()
      .from(docLock.getDoc().get())
      .meta(meta == null ? docLock.getDoc().get().getMeta() : meta.get())
      .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
      
      .name(this.docName == null ? docLock.getDoc().get().getName() : this.docName.orElse(null))
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
    
    return batchBuilder
        .doc(doc)
        .addDocCommit(commit.getItem1())
        .addAllDocCommitTree(commit.getItem2())
        .addAllDocCommands(docLogs)
        .log(commit.getItem1().getCommitLog())
        .addAllDocLock(docLock.getBranches())
        .build();
  }
  


}
