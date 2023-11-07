package io.resys.thena.docdb.spi.doc.commits;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.DocCommitActions.AppendDoc;
import io.resys.thena.docdb.api.actions.DocCommitActions.AppendResultEnvelope;
import io.resys.thena.docdb.api.actions.ImmutableAppendResultEnvelope;
import io.resys.thena.docdb.api.models.ImmutableDoc;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocStatus;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.DocDbState.DocRepo;
import io.resys.thena.docdb.spi.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.spi.ImmutableDocBatch;
import io.resys.thena.docdb.spi.ImmutableDocLockCriteria;
import io.resys.thena.docdb.spi.OidUtils;
import io.resys.thena.docdb.spi.git.commits.CommitLogger;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.resys.thena.docdb.spi.support.Sha2;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AppendDocImpl implements AppendDoc {

  private final DbState state;
  private JsonObject appendLogs = null;
  private JsonObject appendMeta = null;
  private boolean remove;
  
  private String repoId;
  private String docId;
  private String author;
  private String message;

  @Override
  public AppendDocImpl repoId(String repoId) {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    this.repoId = repoId;
    return this;
  }

  @Override
  public AppendDocImpl remove() {
    this.remove = true;
    return this;
  }
  @Override
  public AppendDocImpl meta(JsonObject blob) {
    RepoAssert.notNull(blob, () -> "merge can't be null!");
    this.appendMeta = blob;
    return this;
  }
  @Override
  public AppendDocImpl docId(String docId) {
    this.docId = docId;
    return this;
  }

  @Override
  public AppendDocImpl author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public AppendDocImpl message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }
  @Override
  public AppendDocImpl log(JsonObject doc) {
    this.appendLogs = doc;
    return this;
  }
  @Override
  public Uni<AppendResultEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");

    final var crit = ImmutableDocLockCriteria.builder().docId(docId).build();    
    return this.state.toDocState().withTransaction(repoId, tx -> tx.query().branches().getLock(crit).onItem().transformToUni(lock -> {
      final AppendResultEnvelope validation = validateRepo(lock);
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
  
  private Uni<AppendResultEnvelope> doInLock(DocLock docLock, DocRepo tx) {
    final var doc = ImmutableDoc.builder()
      .from(docLock.getDoc().get())
      .meta(appendMeta)
      .build();
    
    final var batchBuilder = ImmutableDocBatch.builder()
      .repo(tx.getRepo())
      .status(BatchStatus.OK)
      .doc(doc)
      .addAllDocLock(docLock.getBranches());

    final var logger = new CommitLogger();
    docLock.getBranches().forEach(branchLock -> appendToBranch(branchLock, tx, batchBuilder, logger));
    
    final var batch = batchBuilder.log(ImmutableMessage.builder().text(logger.toString()).build()).build();
    
    return tx.insert().batch(batch)
    .onItem().transform(rsp -> ImmutableAppendResultEnvelope.builder()
      .repoId(repoId)
      .doc(doc)
      .addMessages(rsp.getLog())
      .addAllMessages(rsp.getMessages())
      .status(mapStatus(rsp.getStatus()))
      .build());
  }
  
  private AppendResultEnvelope validateRepo(DocLock state) {
    
    // cant merge on first commit
    if(state.getDoc().isEmpty()) {
      return (AppendResultEnvelope) ImmutableAppendResultEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(repoId).append("'")
                  .append(" is rejected.")
                  .append(" Unknown docId: '").append(docId).append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }
  
  private void appendToBranch(DocBranchLock lock, DocRepo tx, ImmutableDocBatch.Builder batch, CommitLogger logger) {
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

    batch.addDocBranch(docBranch)
    .addDocCommit(commit)
    .addAllDocLogs(docLogs);

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
