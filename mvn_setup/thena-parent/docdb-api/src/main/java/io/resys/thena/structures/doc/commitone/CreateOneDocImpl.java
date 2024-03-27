package io.resys.thena.structures.doc.commitone;

import io.resys.thena.api.actions.DocCommitActions.CreateOneDoc;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.actions.ImmutableOneDocEnvelope;
import io.resys.thena.spi.DataMapper;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.support.BatchForOneDocCreate;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneDocImpl implements CreateOneDoc {

  private final DbState state;
  
  private JsonObject appendBlobs;
  private JsonObject appendLogs;
  private JsonObject appendMeta;

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
  @Override public CreateOneDocImpl author(String author) {         this.author = RepoAssert.notEmpty(author,           () -> "author can't be empty!"); return this; }
  @Override public CreateOneDocImpl message(String message) {       this.message = RepoAssert.notEmpty(message,         () -> "message can't be empty!"); return this; }
  @Override public CreateOneDocImpl docType(String docType) {       this.docType = RepoAssert.notEmpty(docType,         () -> "docType can't be empty!"); return this;}
  @Override public CreateOneDocImpl append(JsonObject blob) {       this.appendBlobs = RepoAssert.notNull(blob,         () -> "append can't be empty!"); return this; }

  @Override public CreateOneDocImpl parentDocId(String parentId) {  this.parentDocId = parentId; return this; }
  @Override public CreateOneDocImpl docId(String docId) {           this.docId = docId; return this; }
  @Override public CreateOneDocImpl externalId(String externalId) { this.externalId = externalId; return this; }
  @Override public CreateOneDocImpl ownerId(String ownerId) {       this.ownerId = ownerId; return this; }
  @Override public CreateOneDocImpl log(JsonObject appendLogs) {    this.appendLogs = appendLogs; return this; }
  @Override public CreateOneDocImpl meta(JsonObject appendMeta) {   this.appendMeta = appendMeta; return this; }
  
  
  @Override
  public Uni<OneDocEnvelope> build() {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.notNull(appendBlobs, () -> "Nothing to commit, no content!");
        
    return this.state.withDocTransaction(repoId, this::doInTx);
  }
  
  private Uni<OneDocEnvelope> doInTx(DocState tx) {  
    final var batch = new BatchForOneDocCreate(tx.getTenantId(), docType, author, message, branchName)
        .docId(docId)
        .ownerId(ownerId)
        .externalId(externalId)
        .parentDocId(parentDocId)
        .log(appendLogs)
        .meta(appendMeta)
        .append(appendBlobs)
        .create();

    return tx.insert().batchOne(batch)
      .onItem().transform(rsp -> ImmutableOneDocEnvelope.builder()
        .repoId(repoId)
        .doc(batch.getDoc().get())
        .commit(rsp.getDocCommit().iterator().next())
        .branch(batch.getDocBranch().iterator().next())
        .addMessages(rsp.getLog())
        .addAllMessages(rsp.getMessages())
        .status(DataMapper.mapStatus(rsp.getStatus()))
        .build());
  }

}
