package io.resys.thena.docdb.models.doc.support;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocStatus;
import io.resys.thena.docdb.models.doc.DocDbInserts.DocDbBatchForOne;
import io.resys.thena.docdb.models.doc.DocDbState.DocRepo;
import io.resys.thena.docdb.models.doc.ImmutableDocDbBatchForOne;
import io.resys.thena.docdb.models.git.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.models.git.commits.CommitLogger;
import io.resys.thena.docdb.support.OidUtils;
import io.resys.thena.docdb.support.RepoAssert;
import io.resys.thena.docdb.support.Sha2;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchForOneBranchModify {

  private final DocBranchLock lock; 
  private final DocRepo tx;
  private final String author;
  
  private JsonObject appendBlobs;
  private JsonObject appendLogs;
  private JsonObjectMerge appendMerge;
  
  private String message;
  private boolean remove;

  public BatchForOneBranchModify remove(boolean remove) { this.remove = remove; return this; }
  public BatchForOneBranchModify append(JsonObject append) { this.appendBlobs = append; return this; }
  public BatchForOneBranchModify merge(JsonObjectMerge merge) { this.appendMerge = merge; return this; }
  public BatchForOneBranchModify message(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  public BatchForOneBranchModify log(JsonObject doc) { this.appendLogs = doc; return this; }
  

  public DocDbBatchForOne create() {
    RepoAssert.notNull(lock, () -> "lock can't be null!");
    RepoAssert.notNull(tx, () -> "repo can't be null!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(appendBlobs != null || appendMerge != null, () -> "nothing to commit, no content!");
    
    final var branchId = lock.getBranch().get().getId();
    final var doc = lock.getDoc().get();
    
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
      .parent(lock.getCommit().get().getId())
      .build();
    final var docBranch = ImmutableDocBranch.builder()
      .from(lock.getBranch().get())
      .value(appendBlobs)
      .commitId(commit.getId())
      .status(DocStatus.IN_FORCE)
      .value(appendBlobs == null ? appendMerge.apply(lock.getBranch().get().getValue()): appendBlobs)
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
      .append(" | changed")
      .append(System.lineSeparator())
      .append("  + doc:        ").append(doc.getId())
      .append(System.lineSeparator())
      .append("  + doc branch: ").append(docBranch.getId())
      .append(System.lineSeparator())
      .append("  + doc commit: ").append(commit.getId())
      .append(System.lineSeparator())
      .append("  + doc parent: ").append(commit.getParent().get())
      .append(System.lineSeparator());
    
    if(!docLogs.isEmpty()) {
      logger
      .append("  + doc log:    ").append(docLogs.stream().findFirst().get().getId())
      .append(System.lineSeparator());
    }

    return ImmutableDocDbBatchForOne.builder()
      .repoId(tx.getRepo().getId())
      .status(BatchStatus.OK)
      .doc(doc)
      .addDocBranch(docBranch)
      .addDocCommit(commit)
      .addAllDocLogs(docLogs)
      .addDocLock(lock)
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();
  }
  
}
