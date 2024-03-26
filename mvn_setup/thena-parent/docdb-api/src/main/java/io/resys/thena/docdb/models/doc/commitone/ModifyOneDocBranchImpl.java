package io.resys.thena.docdb.models.doc.commitone;

import java.time.Duration;

import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyOneDocBranch;
import io.resys.thena.docdb.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.docdb.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.models.doc.DocState.DocRepo;
import io.resys.thena.docdb.models.doc.ImmutableDocBranchLockCriteria;
import io.resys.thena.docdb.models.doc.support.BatchForOneBranchModify;
import io.resys.thena.docdb.spi.DataMapper;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
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
  
  private String docId;
  private final String repoId;
  private String branchName;
  private String author;
  private String message;
  private boolean remove;

  @Override public ModifyOneDocBranchImpl docId(String docId) { this.docId = RepoAssert.notEmpty(docId, () -> "docId can't be empty!"); return this; }
  @Override public ModifyOneDocBranchImpl parent(String versionToModify) { this.versionToModify = versionToModify; return this; }
  @Override public ModifyOneDocBranchImpl parentIsLatest() { this.versionToModify = null; return this; }
  @Override public ModifyOneDocBranchImpl remove() { this.remove = true; return this; }
  @Override public ModifyOneDocBranchImpl branchName(String branchName) { this.branchName = RepoAssert.notNull(branchName, () -> "branchName can't be empty!"); return this; }
  @Override public ModifyOneDocBranchImpl append(JsonObject append) { this.appendBlobs = RepoAssert.notNull(append, () -> "append can't be empty!"); return this; }
  @Override public ModifyOneDocBranchImpl merge(JsonObjectMerge merge) { this.appendMerge = RepoAssert.notNull(merge, () -> "merge can't be empty!"); return this; }
  @Override public ModifyOneDocBranchImpl author(String author) { this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); return this; }
  @Override public ModifyOneDocBranchImpl message(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  @Override public ModifyOneDocBranchImpl log(JsonObject doc) { this.appendLogs = doc; return this; }
  
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(docId, () -> "docId can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(appendBlobs != null || appendMerge != null, () -> "Nothing to commit, no content!");
    final var crit = ImmutableDocBranchLockCriteria.builder()
        .branchName(branchName)
        .docId(docId)
        .build();
    
    return this.state.toDocState().withTransaction(repoId, tx -> tx.query().branches().getBranchLock(crit).onItem().transformToUni(lock -> {
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
          .status(Repo.CommitResultStatus.ERROR)
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
                .append(" Unknown branch: '").append(branchName).append("'!")
                .toString())
              .build())
          .status(Repo.CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }

  private Uni<OneDocEnvelope> doInLock(DocBranchLock lock, DocRepo tx) {  
    final var batch = new BatchForOneBranchModify(lock, tx, author)
      .append(appendBlobs)
      .merge(appendMerge)
      .message(message)
      .log(appendLogs)
      .remove(remove)
      .create();

    return tx.insert().batchOne(batch)
      .onItem().transform(rsp -> ImmutableOneDocEnvelope.builder()
        .repoId(repoId)
        .doc(batch.getDoc().get())
        .commit(rsp.getDocCommit().iterator().next())
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(DataMapper.mapStatus(rsp.getStatus()))
        .build());
  }
  
}
