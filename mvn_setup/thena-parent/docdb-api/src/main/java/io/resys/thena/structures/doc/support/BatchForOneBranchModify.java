package io.resys.thena.structures.doc.support;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocLog;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranchLock;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocLog;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocStatus;
import io.resys.thena.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.spi.DataMapper;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.DocState.DocRepo;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
import io.resys.thena.structures.git.commits.CommitLogger;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.support.Sha2;
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
  

  public DocBatchForOne create() {
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
      .status(remove ? DocStatus.ARCHIVED : DocStatus.IN_FORCE)
      .branchName(remove ? OidUtils.gen(): lock.getBranch().get().getBranchName())
      .branchNameDeleted(remove ? lock.getBranch().get().getBranchName() : null)
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

    return ImmutableDocBatchForOne.builder()
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
  
  public static ManyDocsEnvelope mapTo(DocBatchForMany rsp) {
    return ImmutableManyDocsEnvelope.builder()
    .repoId(rsp.getRepo().getId())
    .doc(rsp.getItems().stream()
        .filter(i -> i.getDoc().isPresent())
        .map(i -> i.getDoc().get())
        .collect(Collectors.toList()))
    .commit(rsp.getItems().stream()
        .flatMap(i -> i.getDocCommit().stream())
        .collect(Collectors.toList()))
    .branch(rsp.getItems().stream()
        .flatMap(i -> i.getDocBranch().stream())
        .collect(Collectors.toList()))
    .addAllMessages(rsp.getItems().stream()
        .map(i -> i.getLog())
        .collect(Collectors.toList()))
    .addAllMessages(rsp.getItems().stream()
        .flatMap(i -> i.getMessages().stream())
        .collect(Collectors.toList()))
    
    .status(DataMapper.mapStatus(rsp.getStatus()))
    .build();
  }
}
