package io.resys.thena.structures.doc.commitone;

import java.time.Duration;

import io.resys.thena.api.actions.DocCommitActions.ModifyOneDoc;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocLockCriteria;
import io.resys.thena.structures.doc.support.BatchForOneDocModify;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneDocImpl implements ModifyOneDoc {

  private final DbState state;
  private JsonObject appendLogs = null;
  private JsonObject appendMeta = null;
  private boolean remove;
  
  private final String repoId;
  private String docId;
  private String author;
  private String message;

  @Override public ModifyOneDocImpl remove() { this.remove = true; return this; }
  @Override public ModifyOneDocImpl meta(JsonObject blob) { this.appendMeta = RepoAssert.notNull(blob, () -> "merge can't be null!"); return this; }
  @Override public ModifyOneDocImpl docId(String docId) { this.docId = docId; return this; }
  @Override public ModifyOneDocImpl author(String author) { this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); return this; }
  @Override public ModifyOneDocImpl message(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  @Override public ModifyOneDocImpl log(JsonObject doc) { this.appendLogs = doc; return this; }
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");

    final var crit = ImmutableDocLockCriteria.builder().docId(docId).build();    
    return this.state.withDocTransaction(repoId, tx -> tx.query().branches().getDocLock(crit).onItem().transformToUni(lock -> {
      final OneDocEnvelope validation = validateRepo(lock);
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
  
  private Uni<OneDocEnvelope> doInLock(DocLock docLock, DocState tx) {

    final var batch = new BatchForOneDocModify(docLock, tx, author)
        .message(message)
        .log(appendLogs)
        .meta(appendMeta)
        .create();
    
    return tx.insert().batchOne(batch)
    .onItem().transform(rsp -> ImmutableOneDocEnvelope.builder()
      .repoId(repoId)
      .doc(batch.getDoc().get())
      .addMessages(rsp.getLog())
      .addAllMessages(rsp.getMessages())
      .status(BatchStatus.mapStatus(rsp.getStatus()))
      .build());
  }
  
  private OneDocEnvelope validateRepo(DocLock state) {
    // cant merge on first commit
    if(state.getDoc().isEmpty()) {
      return ImmutableOneDocEnvelope.builder()
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
}
