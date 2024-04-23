package io.resys.thena.structures.doc.support;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranchLock;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.entities.doc.ImmutableDoc;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocLog;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.git.commits.CommitLogger;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.support.Sha2;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneDocModify {

  private final DocLock docLock;
  private final DocState tx;
  private final String author;
  private String message;
  private JsonObject appendLogs;
  private JsonObject appendMeta;
  private boolean remove;

  public BatchForOneDocModify remove(boolean remove) { this.remove = remove; return this; }
  public BatchForOneDocModify log(JsonObject log) { this.appendLogs = log; return this; }
  public BatchForOneDocModify meta(JsonObject meta) { this.appendMeta = meta; return this; }
  public BatchForOneDocModify message(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  
  public DocBatchForOne create() {
    RepoAssert.notNull(docLock, () -> "docLock can't be empty!");
    RepoAssert.notNull(tx, () -> "tx to commit, no content!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    

    final var doc = ImmutableDoc.builder()
      .from(docLock.getDoc().get())
      .meta(appendMeta)
      .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
      .externalId(remove ?  OidUtils.gen(): docLock.getDoc().get().getExternalId())
      .externalIdDeleted(remove ? docLock.getDoc().get().getExternalId() : null)
      .build();
    
    final var batchBuilder = ImmutableDocBatchForOne.builder()
      .repoId(tx.getDataSource().getTenant().getId())
      .status(BatchStatus.OK)
      .doc(doc)
      .addAllDocLock(docLock.getBranches());

    final var logger = new CommitLogger();
    docLock.getBranches().forEach(branchLock -> appendToBranch(doc, branchLock, batchBuilder, logger));
    
    return batchBuilder.log(ImmutableMessage.builder().text(logger.toString()).build()).build();
  }
  

  private void appendToBranch(Doc doc, DocBranchLock lock, ImmutableDocBatchForOne.Builder batch, CommitLogger logger) {
    final var branchId = lock.getBranch().get().getId();

    
    final var template = ImmutableDocCommit.builder()
      .id("commit-template")
      .docId(doc.getId())
      .branchId(branchId)
      .dateTime(LocalDateTime.now())      
      .author(this.author)
      .message(this.message)
      .parent(lock.getBranch().get().getCommitId())
      .build();
    final var commit = ImmutableDocCommit.builder()
      .from(template)
      .id(Sha2.commitId(template))
      .branchId(branchId)
      .build();
    final var docBranch = ImmutableDocBranch.builder()
      .from(lock.getBranch().get())
      .status(remove ? Doc.DocStatus.ARCHIVED : Doc.DocStatus.IN_FORCE)
      .branchName(remove ? OidUtils.gen(): lock.getBranch().get().getBranchName())
      .branchNameDeleted(remove ? lock.getBranch().get().getBranchName() : null)
      .commitId(commit.getId())
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

    batch.addDocBranch(docBranch).addDocCommit(commit).addAllDocLogs(docLogs);
  }
}
