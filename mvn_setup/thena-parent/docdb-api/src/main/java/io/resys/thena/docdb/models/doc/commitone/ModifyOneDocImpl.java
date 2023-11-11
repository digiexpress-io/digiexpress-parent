package io.resys.thena.docdb.models.doc.commitone;

import java.time.Duration;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyOneDoc;
import io.resys.thena.docdb.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.docdb.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLock;
import io.resys.thena.docdb.models.doc.DocState.DocRepo;
import io.resys.thena.docdb.models.doc.ImmutableDocLockCriteria;
import io.resys.thena.docdb.models.doc.support.BatchForOneDocCreate;
import io.resys.thena.docdb.models.doc.support.BatchForOneDocModify;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneDocImpl implements ModifyOneDoc {

  private final DbState state;
  private JsonObject appendLogs = null;
  private JsonObject appendMeta = null;
  private boolean remove;
  
  private String repoId;
  private String docId;
  private String author;
  private String message;

  @Override public ModifyOneDocImpl repoId(String repoId) { this.repoId = RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!"); return this; }
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
    return this.state.toDocState().withTransaction(repoId, tx -> tx.query().branches().getDocLock(crit).onItem().transformToUni(lock -> {
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
  
  private Uni<OneDocEnvelope> doInLock(DocLock docLock, DocRepo tx) {

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
      .status(BatchForOneDocCreate.mapStatus(rsp.getStatus()))
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
