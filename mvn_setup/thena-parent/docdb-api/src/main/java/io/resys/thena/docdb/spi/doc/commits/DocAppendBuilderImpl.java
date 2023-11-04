package io.resys.thena.docdb.spi.doc.commits;

import java.time.Duration;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.actions.DocAppendActions.AppendResultEnvelope;
import io.resys.thena.docdb.api.actions.DocAppendActions.DocAppendBuilder;
import io.resys.thena.docdb.api.actions.ImmutableAppendResultEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommitLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommitLockStatus;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.spi.DocDbState.DocRepo;
import io.resys.thena.docdb.spi.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.spi.ImmutableDocLockCriteria;
import io.resys.thena.docdb.spi.doc.commits.DocCommitBatchBuilder.DocCommitTreeState;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocAppendBuilderImpl implements DocAppendBuilder {

  private final DbState state;
  private JsonObject appendBlobs = null;
  private JsonObject appendLogs = null;
  private boolean deleteBlobs = false;
  private JsonObjectMerge mergeBlobs = null;

  private String repoId;
  private String docId;
  private String externalId;
  private String branchName;
  private String author;
  private String message;
  private String parentCommit;
  private Boolean parentIsLatest = Boolean.FALSE;

  @Override
  public DocAppendBuilder repoId(String repoId) {
    RepoAssert.isEmpty(repoId, () -> "Can't defined id when head is defined!");
    this.repoId = repoId;
    return this;
  }
  @Override
  public DocAppendBuilder branchName(String branchName) {
    RepoAssert.isName(branchName, () -> "branchName has invalid charecters!");
    this.branchName = branchName;
    return this;
  }
  @Override
  public DocAppendBuilder append(JsonObject blob) {
    RepoAssert.isTrue(!deleteBlobs, () -> "can't append blob while removing it!");
    RepoAssert.notNull(blob, () -> "blob can't be empty!");
    RepoAssert.isNull(this.mergeBlobs, () -> "can't append and merge at the same time!");
    this.appendBlobs = blob;
    return this;
  }
  @Override
  public DocAppendBuilder remove() {
    RepoAssert.isNull(appendBlobs, () -> "can't append blob while removing it!");
    this.deleteBlobs = true;
    return this;
  }

  @Override
  public DocAppendBuilder merge(JsonObjectMerge blob) {
    RepoAssert.isTrue(!deleteBlobs, () -> "can't merge blob while removing it!");
    RepoAssert.isNull(this.appendBlobs, () -> "can't append and merge at the same time!");
    RepoAssert.notNull(blob, () -> "merge can't be null!");
    this.mergeBlobs = blob;
    return this;
  }
  @Override
  public DocAppendBuilder author(String author) {
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    this.author = author;
    return this;
  }
  @Override
  public DocAppendBuilder message(String message) {
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    this.message = message;
    return this;
  }
  @Override
  public DocAppendBuilder parent(String parentCommit) {
    this.parentCommit = parentCommit;
    return this;
  }
  @Override
  public DocAppendBuilder parentIsLatest() {
    this.parentIsLatest = true;
    return this;
  }
  @Override
  public DocAppendBuilder docId(String docId) {
    this.docId = docId;
    return this;
  }
  @Override
  public DocAppendBuilder externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }
  @Override
  public DocAppendBuilder log(JsonObject doc) {
    this.appendLogs = doc;
    return this;
  }
  @Override
  public Uni<AppendResultEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(appendBlobs != null || deleteBlobs || mergeBlobs != null, () -> "Nothing to commit, no content!");
        
    final var crit = ImmutableDocLockCriteria.builder().branchName(branchName).versionId(parentCommit).branchName(branchName).build();
    return this.state.toDocState().withTransaction(repoId, tx -> tx.query().commits().getLock(crit)
      .onItem().transformToUni(lock -> {
        final var validation = validateRepo(lock, parentCommit);
        if(validation != null) {
          return Uni.createFrom().item(validation);
        }
        return doInLock(lock, tx);

      })
    )
    .onFailure(err -> state.getErrorHandler().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
   /*
    .onFailure(err -> state.getErrorHandler().isLocked(err)).invoke(error -> {
      error.printStackTrace();
      System.err.println(error.getMessage());
    });*/    
  }
  
  private Uni<AppendResultEnvelope> doInLock(DocCommitLock lock, DocRepo tx) {  
    final var init = DocCommitTreeState.builder().repo(tx.getRepo()).docId(docId).branchName(branchName).externalId(externalId);
    
    if(lock.getStatus() == DocCommitLockStatus.NOT_FOUND) {
      // nothing to add
    } else {
      init.commit(lock.getCommit()).branch(lock.getBranch()).doc(lock.getDoc());
    }
    
    final var batch = new DocCommitBatchBuilderImpl(init.build())
        .commitParent(parentCommit)
        .commitAuthor(author)
        .commitMessage(message)
        .toBeInserted(appendBlobs)
        .toBeLogged(appendLogs)
        .toBeRemoved(deleteBlobs)
        .toBeMerged(mergeBlobs)
        .build();
    
    return tx.insert().batch(batch)
        .onItem().transform(rsp -> ImmutableAppendResultEnvelope.builder()
          .repoId(repoId)
          .commit(rsp.getCommit())
          .addMessages(rsp.getLog())
          .addAllMessages(rsp.getMessages())
          .status(visitStatus(rsp.getStatus()))
          .build());
  }

  private AppendResultEnvelope validateRepo(DocCommitLock state, String commitParent) {
    
    // cant merge on first commit
    if(state.getCommit().isEmpty() && mergeBlobs != null) {
      return (AppendResultEnvelope) ImmutableAppendResultEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(repoId).append("'")
                  .append(", docId: '").append(docId).append("'")
                  .append(", branchName: '").append(branchName == null ? "main" : branchName).append("'")
                  .append(" is rejected.")
                  .append(" Your trying to merge objects to non existent head!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    
    
    // Unknown parent
    if(state.getCommit().isEmpty() && commitParent != null) {
      return (AppendResultEnvelope) ImmutableAppendResultEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(repoId).append("'")
                  .append(" is rejected.")
                  .append(" Your head is: '").append(commitParent).append("')")
                  .append(" but remote has no head.").append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    
    
    // No parent commit defined for existing head
    if(state.getCommit().isPresent() && commitParent == null && !Boolean.TRUE.equals(parentIsLatest)) {
      return (AppendResultEnvelope) ImmutableAppendResultEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Parent commit can only be undefined for the first commit!")
                  .append(" Parent commit for:")
                  .append(" '").append(repoId).append("'")
                  .append(", docId: '").append(docId).append("'")
                  .append(", branchName: '").append(branchName == null ? "main" : branchName).append("'")
                  .append(" is: '").append(state.getCommit().get().getId()).append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
    }
    
    // Wrong parent commit
    if(state.getCommit().isPresent() && commitParent != null && 
        !commitParent.equals(state.getCommit().get().getId()) &&
        !Boolean.TRUE.equals(parentIsLatest)) {
      
      final var text = new StringBuilder()
        .append("Commit to: '").append(repoId).append("'")
        .append(", docId: '").append(docId).append("'")
        .append(", branchName: '").append(branchName == null ? "main" : branchName).append("'")
        .append(" is rejected.")
        .append(" Your head is: '").append(commitParent).append("')")
        .append(" but remote is: '").append(state.getCommit().get().getId()).append("'!")
        .toString();
      
      return ImmutableAppendResultEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder().text(text).build())
          .status(CommitResultStatus.ERROR)
          .build();
    }

    return null;
  }
  
  private static CommitResultStatus visitStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return CommitResultStatus.CONFLICT;
    }
    return CommitResultStatus.ERROR; 
  }
}
