package io.resys.thena.docdb.models.doc.support;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.thena.docdb.api.models.ImmutableDoc;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocStatus;
import io.resys.thena.docdb.models.doc.DocInserts.DocBatchForOne;
import io.resys.thena.docdb.models.doc.DocState.DocRepo;
import io.resys.thena.docdb.models.doc.ImmutableDocBatchForOne;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.git.commits.CommitLogger;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import io.resys.thena.docdb.support.Sha2;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneDocModify {

  private final DocLock docLock;
  private final DocRepo tx;
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
      .build();
    
    final var batchBuilder = ImmutableDocBatchForOne.builder()
      .repoId(tx.getRepo().getId())
      .status(BatchStatus.OK)
      .doc(doc)
      .addAllDocLock(docLock.getBranches());

    final var logger = new CommitLogger();
    docLock.getBranches().forEach(branchLock -> appendToBranch(branchLock, tx, batchBuilder, logger));
    
    return batchBuilder.log(ImmutableMessage.builder().text(logger.toString()).build()).build();
  }
  

  private void appendToBranch(DocBranchLock lock, DocRepo tx, ImmutableDocBatchForOne.Builder batch, CommitLogger logger) {
    final var branchId = lock.getBranch().get().getId();
    
    final var doc = ImmutableDoc.builder()
        .from(lock.getDoc().get())
        .meta(appendMeta)
        .build();
    
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
      .status(DocStatus.IN_FORCE)
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

    logger
      .append(" | changed")
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

    batch.addDocBranch(docBranch).addDocCommit(commit).addAllDocLogs(docLogs);
  }
}
