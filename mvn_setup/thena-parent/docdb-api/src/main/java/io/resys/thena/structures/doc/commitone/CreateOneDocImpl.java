package io.resys.thena.structures.doc.commitone;

import java.util.List;

import io.resys.thena.api.actions.DocCommitActions.CreateOneDoc;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForMany;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.support.BatchForOneDocCreate;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneDocImpl implements CreateOneDoc {

  private final DbState state;
  
  private JsonObject branchContent;
  private List<JsonObject> commands;
  private JsonObject docMeta;

  private String parentDocId;
  private final String repoId;
  private String docId;
  private String externalId;
  private String docType;
  private String branchName;
  private String author;
  private String message;
  private String ownerId;


  @Override public CreateOneDocImpl branchName(String branchName) { this.branchName = RepoAssert.isName(branchName,     () -> "branchName has invalid charecters!"); return this; }
  @Override public CreateOneDocImpl branchContent(JsonObject blob) {this.branchContent = RepoAssert.notNull(blob,         () -> "branchContent can't be empty!"); return this; }
  @Override public CreateOneDocImpl commitAuthor(String author) {   this.author = RepoAssert.notEmpty(author,           () -> "commitAuthor can't be empty!"); return this; }
  @Override public CreateOneDocImpl commitMessage(String message) { this.message = RepoAssert.notEmpty(message,         () -> "commitMessage can't be empty!"); return this; }
  @Override public CreateOneDocImpl docType(String docType) {       this.docType = RepoAssert.notEmpty(docType,         () -> "docType can't be empty!"); return this;}

  @Override public CreateOneDocImpl parentDocId(String parentId) {  this.parentDocId = parentId; return this; }
  @Override public CreateOneDocImpl docId(String docId) {           this.docId = docId; return this; }
  @Override public CreateOneDocImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOneDocImpl ownerId(String ownerId) {       this.ownerId = ownerId; return this; }
  @Override public CreateOneDocImpl commands(List<JsonObject> commands) {  this.commands = commands; return this; }
  @Override public CreateOneDocImpl meta(JsonObject docMeta) {      this.docMeta = docMeta; return this; }
  
  
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.notNull(branchContent, () -> "Nothing to commit, no content!");
        
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, this::doInTx);
  }
  
  private Uni<OneDocEnvelope> doInTx(DocState tx) {  
    final var batch = new BatchForOneDocCreate(tx.getTenantId(), docType, author, message, branchName)
        .docId(docId)
        .ownerId(ownerId)
        .externalId(externalId)
        .parentDocId(parentDocId)
        .commands(commands)
        .meta(docMeta)
        .branchContent(branchContent)
        .create();

    return tx.insert().batchMany(ImmutableDocBatchForMany.builder()
        .addItems(batch)
        .repo(repoId)
        .status(BatchStatus.OK)
        .log("")
        .build())
      .onItem().transform(rsp -> {
        if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
          throw new CreateOneDocException("Failed to create document!", rsp);
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

  
  public static class CreateOneDocException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final DocBatchForMany batch;
    public CreateOneDocException(String message, DocBatchForMany batch) {
      super(message);
      this.batch = batch;
    }
    public DocBatchForMany getBatch() {
      return batch;
    }
  }
}
