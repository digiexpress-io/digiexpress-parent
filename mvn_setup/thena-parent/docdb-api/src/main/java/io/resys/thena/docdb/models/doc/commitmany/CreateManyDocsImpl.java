package io.resys.thena.docdb.models.doc.commitmany;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.DocCommitActions.AddItemToCreateDoc;
import io.resys.thena.docdb.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.docdb.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.docdb.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.models.doc.DocDbInserts.DocDbBatchForOne;
import io.resys.thena.docdb.models.doc.DocDbState.DocRepo;
import io.resys.thena.docdb.models.doc.support.BatchForOneDocCreate;
import io.resys.thena.docdb.models.doc.ImmutableDocDbBatchForMany;
import io.resys.thena.docdb.models.git.GitDbInserts.BatchStatus;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateManyDocsImpl implements CreateManyDocs {
  private final DbState state;
  private final List<DocDbBatchForOne> items = new ArrayList<DocDbBatchForOne>();
  
  private String repoId;
  private String branchName;
  private String docType;
  private String author;
  private String message;

  private AddItemToCreateDoc lasItemBuilder;
  
  @Override public CreateManyDocs repoId(String repoId)         { this.repoId = RepoAssert.notEmpty(repoId,       () -> "repoId can't be empty!"); return this; }
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
      @Override public AddItemToCreateDoc externalId(String externalId) { oneDoc.externalId(externalId); return this;}
      @Override public AddItemToCreateDoc log(JsonObject log)           { oneDoc.log(log); return this; }
      @Override public AddItemToCreateDoc meta(JsonObject meta)         { oneDoc.meta(meta); return this; }
      @Override public AddItemToCreateDoc append(JsonObject blob)       { oneDoc.append(blob); return this;}
      @Override public AddItemToCreateDoc docId(String docId)           { oneDoc.docId(docId); return this;}      
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
    
    
    return this.state.toDocState().withTransaction(repoId, this::doInTx);
  }
  
  private Uni<ManyDocsEnvelope> doInTx(DocRepo tx) {  
    final var batch = ImmutableDocDbBatchForMany.builder()
        .repo(tx.getRepo())
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
        
        .status(BatchForOneDocCreate.mapStatus(rsp.getStatus()))
        .build());
  }
}
