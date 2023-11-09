package io.resys.thena.docdb.spi.doc.commits;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyOneDocBranch;
import io.resys.thena.docdb.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.docdb.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocStatus;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.DocDbState.DocRepo;
import io.resys.thena.docdb.spi.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.spi.ImmutableDocDbBatchForOne;
import io.resys.thena.docdb.spi.ImmutableDocBranchLockCriteria;
import io.resys.thena.docdb.spi.OidUtils;
import io.resys.thena.docdb.spi.git.commits.CommitLogger;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.spi.support.Sha2;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneDocBranchImpl implements ModifyOneDocBranch {

  private final DbState state;
  private JsonObject appendBlobs = null;
  private JsonObject appendLogs = null;
  private JsonObjectMerge appendMerge = null;
  private String versionToModify = null;
  
  private String repoId;
  private String branchId;
  private String author;
  private String message;
  private boolean remove;

  @Override
  public ModifyOneDocBranchImpl repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }

  @Override
  public ModifyOneDocBranch parent(String versionToModify) {
    this.versionToModify = versionToModify;
    return this;
  }

  @Override
  public ModifyOneDocBranch parentIsLatest() {
    this.versionToModify = null;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl remove() {
    this.remove = true;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl branchId(String branchId) {
    RepoAssert.notNull(branchId, () -> "branchId can't be empty!");
    this.branchId = branchId;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl append(JsonObject blob) {
    RepoAssert.notNull(blob, () -> "blob can't be empty!");
    this.appendBlobs = blob;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl merge(JsonObjectMerge blob) {
    RepoAssert.notNull(blob, () -> "blob can't be empty!");
    this.appendMerge = blob;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }
  @Override
  public ModifyOneDocBranchImpl log(JsonObject doc) {
    this.appendLogs = doc;
    return this;
  }
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchId, () -> "branchId can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(appendBlobs != null, () -> "Nothing to commit, no content!");
    final var crit = ImmutableDocBranchLockCriteria.builder()
        .branchId(branchId)
        .build();
    
    return this.state.toDocState().withTransaction(repoId, tx -> tx.query().branches().getLock(crit).onItem().transformToUni(lock -> {
      final OneDocEnvelope validation = validateRepo(lock);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(lock, tx);
    }))
    .onFailure(err -> state.getErrorHandler().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }
  
  private OneDocEnvelope validateRepo(DocBranchLock state) {
    // Wrong parent commit
    if(state.getCommit().isPresent() && versionToModify != null && 
        !versionToModify.equals(state.getCommit().get().getId())) {
      
      final var text = new StringBuilder()
        .append("Commit to: '").append(repoId).append("'")
        .append(" is rejected.")
        .append(" Your head is: '").append(versionToModify).append("')")
        .append(" but remote is: '").append(state.getCommit().get().getId()).append("'!")
        .toString();
      
      return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder().text(text).build())
          .status(CommitResultStatus.ERROR)
          .build();
    }

    
    // cant merge on first commit
    if(state.getBranch().isEmpty()) {
      return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(repoId).append("'")
                  .append(" is rejected.")
                  .append(" Unknown branchId: '").append(branchId).append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }
  
  
  private Uni<OneDocEnvelope> doInLock(DocBranchLock lock, DocRepo tx) {  
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

    final var batch = ImmutableDocDbBatchForOne.builder()
      .repo(tx.getRepo())
      .status(BatchStatus.OK)
      .doc(Optional.empty())
      .addDocBranch(docBranch)
      .addDocCommit(commit)
      .addAllDocLogs(docLogs)
      .addDocLock(lock)
      .log(ImmutableMessage.builder().text(logger.toString()).build())
      .build();

    return tx.insert().batchOne(batch)
      .onItem().transform(rsp -> ImmutableOneDocEnvelope.builder()
        .repoId(repoId)
        .doc(doc)
        .commit(rsp.getDocCommit().iterator().next())
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(mapStatus(rsp.getStatus()))
        .build());
  }
  
  private static CommitResultStatus mapStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return CommitResultStatus.CONFLICT;
    }
    return CommitResultStatus.ERROR; 
  }
}
