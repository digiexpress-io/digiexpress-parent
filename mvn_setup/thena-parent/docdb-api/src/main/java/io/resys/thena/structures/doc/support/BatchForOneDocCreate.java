package io.resys.thena.structures.doc.support;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocLog;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.Doc.DocStatus;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.resys.thena.structures.git.commits.CommitLogger;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.support.Sha2;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneDocCreate {

  private final String repoId;
  private final String docType;
  private final String author;
  private final String message;
  private final String branchName;

  private String docId;
  private String parentDocId;
  private String externalId;
  private String ownerId;
  private JsonObject appendBlobs;
  private JsonObject appendLogs;
  private JsonObject appendMeta;

  public BatchForOneDocCreate parentDocId(String parentId) {  this.parentDocId = parentId; return this; }
  public BatchForOneDocCreate docId(String docId) {           this.docId = docId; return this; }
  public BatchForOneDocCreate externalId(String externalId) { this.externalId = externalId; return this; }
  public BatchForOneDocCreate ownerId(String ownerId) {       this.ownerId = ownerId; return this; }
  public BatchForOneDocCreate log(JsonObject log) {           this.appendLogs = log; return this; }
  public BatchForOneDocCreate meta(JsonObject meta) {         this.appendMeta = meta; return this; }
  public BatchForOneDocCreate append(JsonObject blob) {       this.appendBlobs = RepoAssert.notNull(blob, () -> "append can't be empty!"); return this; }
  
  
  public DocBatchForOne create() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notNull(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.notNull(appendBlobs, () -> "Nothing to commit, no content!");
    
    // fallbacks: json.id ?? this.docId ?? generate doc id
    final var docId = Optional.ofNullable(appendBlobs.getString("id")).orElseGet(() -> Optional.ofNullable(this.docId).orElse(OidUtils.gen()));
    final var branchId = OidUtils.gen(); 
    final var doc = ImmutableDoc.builder()
        .id(docId)
        .ownerId(ownerId)
        .parentId(parentDocId)
        .externalId(Optional.ofNullable(this.externalId == null || this.externalId.trim().isEmpty() ? null : this.externalId).orElse(OidUtils.gen()))
        .type(docType)
        .status(Doc.DocStatus.IN_FORCE)
        .meta(appendMeta)
        .build();
    
    final var template = ImmutableDocCommit.builder()
      .id("commit-template")
      .docId(doc.getId())
      .branchId(branchId)
      .dateTime(LocalDateTime.now())      
      .author(this.author)
      .message(this.message)
      .parent(Optional.empty())
      .build();
    final var commit = ImmutableDocCommit.builder()
      .from(template)
      .id(Sha2.commitId(template))
      .branchId(branchId)
      .build();
    final var docBranch = ImmutableDocBranch.builder()
      .id(branchId)
      .docId(doc.getId())
      .commitId(commit.getId())
      .branchName(branchName)
      .value(appendBlobs)
      .status(Doc.DocStatus.IN_FORCE)
      .build();
    
    final List<DocLog> docLogs = appendLogs == null ? Collections.emptyList() : Arrays.asList(
        ImmutableDocLog.builder()
          .id(OidUtils.gen())
          .docId(doc.getId())
          .branchId(branchId)
          .docCommitId(commit.getId())
          .value(appendLogs)
          .build()
        );

    final var logger = new CommitLogger();
    logger
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + doc:        ").append(doc.getId())
      .append(System.lineSeparator())
      .append("  + doc branch: ").append(docBranch.getId())
      .append(System.lineSeparator())
      .append("  + doc commit: ").append(commit.getId())
      .append(System.lineSeparator());
    
    if(!docLogs.isEmpty()) {
      logger
      .append("  + doc log:    ").append(docLogs.stream().findFirst().get().getId())
      .append(System.lineSeparator());
    }

    final var batch = ImmutableDocBatchForOne.builder()
      .repoId(repoId)
      .status(BatchStatus.OK)
      .doc(doc)
      .addDocBranch(docBranch)
      .addDocCommit(commit)
      .addAllDocLogs(docLogs)
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
    return batch;
  }
}
