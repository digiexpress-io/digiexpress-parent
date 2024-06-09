package io.resys.thena.structures.doc.commitone;

import java.time.Duration;

import io.resys.thena.api.actions.DocCommitActions.DeleteOneDoc;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.DocQueryActions.Branches;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocBranchLockCriteria;
import io.resys.thena.structures.doc.support.BatchForOneDocDelete;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteOneDocImpl implements DeleteOneDoc {
  private final DbState state;
  private final String repoId;
  private String docId;
  private String author;
  private String message;
  
  @Override
  public DeleteOneDoc docId(String docId) {
    this.docId = docId;
    return this;
  }
  @Override
  public DeleteOneDoc commitAuthor(String author) {
    this.author = author;
    return this;
  }
  @Override
  public DeleteOneDoc commitMessage(String message) {
    this.message = message;
    return this;
  }
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(docId, () -> "docId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
  
    final var crit = ImmutableDocBranchLockCriteria.builder()
        .docId(docId)
        .branchName(Branches.main.name())
        .build();
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, tx -> tx.query().branches().getBranchLock(crit).onItem().transformToUni(lock -> {
      final OneDocEnvelope validation = validateRepo(lock, docId);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(lock, tx);
    }))
    .onFailure(err -> state.getDataSource().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }

  private OneDocEnvelope validateRepo(DocBranchLock state, String docId) {
    if(state.getBranch().isEmpty()) {
      return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(repoId).append("'")
                  .append(" is rejected.")
                  .append(" Can't find doc by id: '").append(docId).append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }

  private Uni<OneDocEnvelope> doInLock(DocBranchLock lock, DocState tx) {
    final var batch = new BatchForOneDocDelete(lock, tx, author, message).create();
    final var many = ImmutableDocBatchForMany.builder()
        .addItems(batch)
        .repo(repoId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    return tx.insert().batchMany(many)
        .onItem().transform(rsp -> {
          if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
            throw new DeleteOneDocException("Failed to create document!", rsp);
          }

          return ImmutableOneDocEnvelope.builder()
            .repoId(repoId)
            .doc(batch.getDoc().get())
            .commit(batch.getDocCommit().iterator().next())
            .branch(batch.getDocBranch().iterator().next())
            .commands(batch.getDocCommands())
            .commitTree(batch.getDocCommitTree())
            .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
            .addAllMessages(rsp.getMessages())
            .status(BatchStatus.mapStatus(rsp.getStatus()))
            .build();
        });
  }  

  
  
  public static class DeleteOneDocException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final DocBatchForMany batch;
    public DeleteOneDocException(String message, DocBatchForMany batch) {
      super(message);
      this.batch = batch;
    }
    public DocBatchForMany getBatch() {
      return batch;
    }
  }
}
