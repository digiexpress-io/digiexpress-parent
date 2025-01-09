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
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommands;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.doc.commitlog.DocCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneDocCreate {

  private final String repoId;

  private final String author;
  private final String message;
  
  private String docType;
  private String branchName;
  private String docId;
  private String parentDocId;
  private String externalId;
  private String ownerId;
  private JsonObject branchContent;
  private List<JsonObject> commands;
  private JsonObject docMeta;

  public BatchForOneDocCreate parentDocId(String parentId) {  this.parentDocId = parentId; return this; }
  public BatchForOneDocCreate docId(String docId) {           this.docId = docId; return this; }
  public BatchForOneDocCreate externalId(String externalId) { this.externalId = externalId; return this; }
  public BatchForOneDocCreate ownerId(String ownerId) {       this.ownerId = ownerId; return this; }
  public BatchForOneDocCreate commands(List<JsonObject> log) {this.commands = log; return this; }
  public BatchForOneDocCreate meta(JsonObject meta) {         this.docMeta = meta; return this; }
  public BatchForOneDocCreate branchContent(JsonObject blob) {this.branchContent = RepoAssert.notNull(blob, () -> "branchContent can't be empty!"); return this; }
  public BatchForOneDocCreate branchName(String branchName) { this.branchName = RepoAssert.isName(branchName, () -> "branchName has invalid charecters!"); return this;}
  public BatchForOneDocCreate docType(String docType)       { this.docType = RepoAssert.notEmpty(docType,     () -> "docType can't be empty!"); return this; }
  
  public DocBatchForOne create() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notNull(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.notNull(branchContent, () -> "Nothing to commit, no content!");
    
    // fallbacks: json.id ?? this.docId ?? generate doc id
    final var docId = Optional.ofNullable(this.docId).orElseGet(() -> Optional.ofNullable(branchContent.getString("id")).orElse(OidUtils.gen()));
    final var branchId = OidUtils.gen();
    final var now = OffsetDateTime.now();
    final var commitBuilder = new DocCommitBuilder(repoId, ImmutableDocCommit.builder()
        .id(OidUtils.gen())
        .docId(docId)
        .branchId(branchId)
        .createdAt(now)
        .commitAuthor(this.author)
        .commitMessage(this.message)
        .parent(Optional.empty())
        .commitLog("")
        .build());
    
    final var doc = ImmutableDoc.builder()
        .id(docId)
        .ownerId(ownerId)
        .parentId(parentDocId)
        .commitId(commitBuilder.getCommitId())
        .createdWithCommitId(commitBuilder.getCommitId())
        .externalId(Optional.ofNullable(this.externalId == null || this.externalId.trim().isEmpty() ? null : this.externalId).orElse(OidUtils.gen()))
        .type(docType)
        .status(Doc.DocStatus.IN_FORCE)
        .meta(docMeta)
        .createdAt(now)
        .updatedAt(now)
        .build();
    commitBuilder.add(doc);
    
    final var docBranch = ImmutableDocBranch.builder()
      .id(branchId)
      .docId(doc.getId())
      .commitId(commitBuilder.getCommitId())
      .createdWithCommitId(commitBuilder.getCommitId())
      .branchName(branchName)
      .value(branchContent)
      .status(Doc.DocStatus.IN_FORCE)
      .createdAt(now)
      .updatedAt(now)
      .build();
    commitBuilder.add(docBranch);
    
    final List<DocCommands> docLogs = commands == null ? Collections.emptyList() : Arrays.asList(
        ImmutableDocCommands.builder()
          .id(OidUtils.gen())
          .docId(doc.getId())
          .branchId(branchId)
          .commitId(commitBuilder.getCommitId())
          .commands(commands)
          .createdAt(now)
          .createdBy(author)
          .build()
        );
    docLogs.forEach(command -> commitBuilder.add(command));

    final var commit = commitBuilder.close();
    final var batch = ImmutableDocBatchForOne.builder()
      .doc(doc)
      .addDocBranch(docBranch)
      .addDocCommit(commit.getItem1())
      .addAllDocCommitTree(commit.getItem2())
      .addAllDocCommands(docLogs)
      .log(commit.getItem1().getCommitLog())
      .build();
    return batch;
  }
}
