package io.resys.thena.structures.doc.commitmany;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.AddItemToCreateDoc;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.support.BatchForOneDocCreate;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateManyDocsImpl implements CreateManyDocs {
  private final DbState state;
  private final List<DocBatchForOne> items = new ArrayList<DocBatchForOne>();
  private final String repoId;
  
  private String branchName;
  private String docType;
  private String author;
  private String message;

  private AddItemToCreateDoc lasItemBuilder;
  
  @Override public CreateManyDocs branchName(String branchName) { this.branchName = RepoAssert.isName(branchName, () -> "branchName has invalid charecters!"); return this;}
  @Override public CreateManyDocs docType(String docType)       { this.docType = RepoAssert.notEmpty(docType,     () -> "docType can't be empty!"); return this; }
  @Override public CreateManyDocs author(String author)         { this.author = RepoAssert.notEmpty(author,       () -> "author can't be empty!"); return this; }
  @Override public CreateManyDocs message(String message)       { this.message = RepoAssert.notEmpty(message,     () -> "message can't be empty!"); return this; }

  @Override
  public AddItemToCreateDoc item() {
    RepoAssert.isNull(lasItemBuilder, () -> "previous item() method chain left unfinished, next() method must be called to finish item()!");
    
    final var parent = this;
    final var oneDoc = new BatchForOneDocCreate(repoId, docType, author, message, branchName);
    
    lasItemBuilder = new AddItemToCreateDoc() {
      @Override public AddItemToCreateDoc parentDocId(String parentId) {  oneDoc.parentDocId(parentId); return this; }
      @Override public AddItemToCreateDoc externalId(String externalId) { oneDoc.externalId(externalId); return this;}
      @Override public AddItemToCreateDoc log(JsonObject log)           { oneDoc.log(log); return this; }
      @Override public AddItemToCreateDoc meta(JsonObject meta)         { oneDoc.meta(meta); return this; }
      @Override public AddItemToCreateDoc append(JsonObject blob)       { oneDoc.branchContent(blob); return this;}
      @Override public AddItemToCreateDoc docId(String docId)           { oneDoc.docId(docId); return this;}      
      @Override public AddItemToCreateDoc ownerId(String ownerId)       { oneDoc.ownerId(ownerId); return this;}      
      @Override public CreateManyDocs next() {
        lasItemBuilder = null;
        final var newDoc = oneDoc.create();
        items.add(newDoc);
        return parent;
      }
    };
    
    return lasItemBuilder;
  }

  @Override
  public Uni<ManyDocsEnvelope> build() {
    RepoAssert.isNull(lasItemBuilder, () -> "previous item() method chain left unfinished, next() method must be called to finish item()!");
    
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(docType, () -> "docType can't be empty!");
    RepoAssert.isTrue(!items.isEmpty(), () -> "Nothing to commit, no items!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, this::doInTx);
  }
  
  private Uni<ManyDocsEnvelope> doInTx(DocState tx) {  
    final var batch = ImmutableDocBatchForMany.builder()
        .repo(tx.getDataSource().getTenant())
        .status(BatchStatus.OK)
        .log(ImmutableMessage.builder()
            .text(String.join("\r\n" + "\r\n", items.stream().map(i -> i.getLog().getText()).collect(Collectors.toList())))
            .build())
        .addAllItems(items)
        .build();
    
    return tx.insert().batchMany(batch)
      .onItem().transform(rsp -> ImmutableManyDocsEnvelope.builder()
        .repoId(repoId)
        .doc(rsp.getItems().stream()
            .filter(i -> i.getDoc().isPresent())
            .map(i -> i.getDoc().get())
            .collect(Collectors.toList()))
        .commit(rsp.getItems().stream()
            .flatMap(i -> i.getDocCommit().stream())
            .collect(Collectors.toList()))
        .branch(rsp.getItems().stream()
            .flatMap(i -> i.getDocBranch().stream())
            .collect(Collectors.toList()))
        .addAllMessages(rsp.getItems().stream()
            .map(i -> i.getLog())
            .collect(Collectors.toList()))
        .addAllMessages(rsp.getItems().stream()
            .flatMap(i -> i.getMessages().stream())
            .collect(Collectors.toList()))
        
        .status(BatchStatus.mapStatus(rsp.getStatus()))
        .build());
  }
}
