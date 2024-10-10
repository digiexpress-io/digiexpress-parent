package io.resys.thena.structures.doc.commitone;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.actions.DocCommitActions.CreateOneDocBranch;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommands;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocBatchForOne;
import io.resys.thena.structures.doc.ImmutableDocBranchLockCriteria;
import io.resys.thena.structures.doc.commitlog.DocCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneDocBranchImpl implements CreateOneDocBranch {

  private final DbState state;
  private final String repoId;
  
  private JsonObject appendbranchContents = null;
  private List<JsonObject> commands = null;

  private String docId;

  private String branchName = DocObjectsQueryImpl.BRANCH_MAIN;
  private String branchFrom;
  private String author;
  private String message;

  @Override public CreateOneDocBranchImpl docId(String docId) { this.docId = RepoAssert.notEmpty(docId,               () -> "docId can't be empty!"); return this; }
  @Override public CreateOneDocBranchImpl branchName(String branchName) { this.branchName = RepoAssert.isName(branchName, () -> "branchName has invalid charecters!"); return this; }
  @Override public CreateOneDocBranchImpl branchContent(JsonObject branchContent) { this.appendbranchContents = RepoAssert.notNull(branchContent, () -> "branchContent can't be empty!"); return this; }
  @Override public CreateOneDocBranchImpl commitAuthor(String author) { this.author = RepoAssert.notEmpty(author,     () -> "author can't be empty!"); return this; }
  @Override public CreateOneDocBranchImpl commitMessage(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  @Override public CreateOneDocBranchImpl branchFrom(String branchFrom) { this.branchFrom = branchFrom; return this; }
  @Override public CreateOneDocBranchImpl commands(List<JsonObject> commands) { this.commands = commands; return this; }

  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(docId,      () -> "docId can't be empty!");
    RepoAssert.notEmpty(author,     () -> "author can't be empty!");
    RepoAssert.notEmpty(branchFrom, () -> "branchFrom can't be empty!");
    RepoAssert.notEmpty(message,    () -> "message can't be empty!");
    RepoAssert.isTrue(appendbranchContents != null, () -> "Nothing to commit, no content!");
    
    final var crit = ImmutableDocBranchLockCriteria.builder().branchName(branchFrom).docId(docId).build();
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, tx -> tx.query().branches().getBranchLock(crit).onItem().transformToUni(lock -> {
      final OneDocEnvelope validation = validateRepo(lock);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(lock, tx);
    }))
    .onFailure(err -> this.state.getDataSource().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }
  
  private OneDocEnvelope validateRepo(DocBranchLock state) {
    
    // cant merge on first commit
    if(state.getBranch().isEmpty()) {
      return ImmutableOneDocEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                  .append("Commit to: '").append(repoId).append("'")
                  .append(" is rejected.")
                  .append(" Unknown branchId: '").append(branchFrom).append("'!")
                  .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }
  
  
  private Uni<OneDocEnvelope> doInLock(DocBranchLock lock, DocState tx) {  
    final var branchId = OidUtils.gen();
    final var doc = lock.getDoc().get();
    
    final var now = OffsetDateTime.now();
    
    final var commitBuilder = new DocCommitBuilder(repoId, ImmutableDocCommit.builder()
      .id(OidUtils.gen())
      .docId(doc.getId())
      .branchId(branchId)
      .createdAt(now)
      .commitAuthor(this.author)
      .commitMessage(this.message)
      .parent(lock.getCommit().get().getId())
      .commitLog("")
      .build()
    );

    final var docBranch = ImmutableDocBranch.builder()
      .id(branchId)
      .docId(doc.getId())
      .commitId(commitBuilder.getCommitId())
      .createdWithCommitId(commitBuilder.getCommitId())
      .branchName(branchName)
      .value(appendbranchContents)
      .createdAt(now)
      .updatedAt(now)
      .status(Doc.DocStatus.IN_FORCE)
      .build();
    commitBuilder.add(docBranch);
    
    final List<DocCommands> docLogs = commands == null ? Collections.emptyList() : Arrays.asList(
        ImmutableDocCommands.builder()
          .id(OidUtils.gen())
          .docId(doc.getId())
          .branchId(branchId)
          .commitId(commitBuilder.getCommitId())
          .commands(commands)
          .createdAt(now)
          .createdBy(author)
          .build()
        );
    docLogs.forEach(command -> commitBuilder.add(command));
    
    
    final var commit = commitBuilder.close();

    final var batch = ImmutableDocBatchForOne.builder()
      .doc(Optional.empty())
      .addDocBranch(docBranch)
      .addAllDocCommands(docLogs)
      .log(commit.getItem1().getCommitLog())
      .addDocCommit(commit.getItem1())
      .addAllDocCommitTree(commit.getItem2())
      .build();

    return tx.insert()
    .batchMany(ImmutableDocBatchForMany.builder().addItems(batch).repo(repoId).status(BatchStatus.OK).log("").build())
    .onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneDocBranchException("Failed to create document branch!", rsp);
      }

      return ImmutableOneDocEnvelope.builder()
        .repoId(repoId)
        .doc(doc)
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

  public static class CreateOneDocBranchException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final DocBatchForMany batch;
    public CreateOneDocBranchException(String message, DocBatchForMany batch) {
      super(message);
      this.batch = batch;
    }
    public DocBatchForMany getBatch() {
      return batch;
    }
  }
}
