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
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocInserts.DocBatchForOne;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
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
 
  private String author;
  private String message;

  private AddItemToCreateDoc lasItemBuilder;
  @Override public CreateManyDocs commitAuthor(String author)  { this.author = RepoAssert.notEmpty(author,  () -> "commitAuthor can't be empty!"); return this; }
  @Override public CreateManyDocs commitMessage(String message){ this.message = RepoAssert.notEmpty(message,() -> "commitMessage can't be empty!"); return this; }

  @Override
  public AddItemToCreateDoc item() {
    RepoAssert.isNull(lasItemBuilder, () -> "previous item() method chain left unfinished, next() method must be called to finish item()!");
    
    final var parent = this;
    final var oneDoc = new BatchForOneDocCreate(repoId, author, message);

    lasItemBuilder = new AddItemToCreateDoc() {
      @Override public AddItemToCreateDoc branchName(String branchName) { oneDoc.branchName(branchName); return this;}
      @Override public AddItemToCreateDoc docType(String docType)       { oneDoc.docType(docType); return this; }
      @Override public AddItemToCreateDoc parentDocId(String parentId)  { oneDoc.parentDocId(parentId); return this; }
      @Override public AddItemToCreateDoc externalId(String externalId) { oneDoc.externalId(externalId); return this;}
      @Override public AddItemToCreateDoc commands(List<JsonObject> log){ oneDoc.commands(log); return this; }
      @Override public AddItemToCreateDoc meta(JsonObject meta)         { oneDoc.meta(meta); return this; }
      @Override public AddItemToCreateDoc branchContent(JsonObject blob){ oneDoc.branchContent(blob); return this;}
      @Override public AddItemToCreateDoc docId(String docId)           { oneDoc.docId(docId); return this;}      
      @Override public AddItemToCreateDoc ownerId(String ownerId)       { oneDoc.ownerId(ownerId); return this;}      
      @Override public CreateManyDocs next() {
        lasItemBuilder = null;
        final var newDoc = oneDoc.create();
        items.add(newDoc);
        return parent;
      }
    };
    lasItemBuilder.branchName(DocObjectsQueryImpl.BRANCH_MAIN);
    return lasItemBuilder;
  }

  @Override
  public Uni<ManyDocsEnvelope> build() {
    RepoAssert.isNull(lasItemBuilder, () -> "previous item() method chain left unfinished, next() method must be called to finish item()!");
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.isTrue(!items.isEmpty(), () -> "Nothing to commit, no items!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, this::doInTx);
  }
  
  private Uni<ManyDocsEnvelope> doInTx(DocState tx) {  
    final var batch = ImmutableDocBatchForMany.builder()
        .repo(tx.getDataSource().getTenant().getId())
        .status(BatchStatus.OK)
        .log(String.join("\r\n\r\n", items.stream().map(i -> i.getLog()).collect(Collectors.toList())))
        .addAllItems(items)
        .build();
    
    return tx.insert().batchMany(batch)
      .onItem().transform(rsp -> ImmutableManyDocsEnvelope.builder()
        .repoId(repoId)
        
        .doc(rsp.getItems().stream()
            .filter(i -> i.getDoc().isPresent())
            .map(i -> i.getDoc().get())
            .collect(Collectors.toList()))

        .branch(rsp.getItems().stream()
            .flatMap(i -> i.getDocBranch().stream())
            .collect(Collectors.toList()))
        
        .commits(rsp.getItems().stream()
            .flatMap(i -> i.getDocCommit().stream())
            .collect(Collectors.toList()))
        .addAllCommitTree(rsp.getItems().stream()
            .flatMap(i -> i.getDocCommitTree().stream())
            .collect(Collectors.toList()))
        .addAllCommands(rsp.getItems().stream()
            .flatMap(i -> i.getDocCommands().stream())
            .collect(Collectors.toList()))
        
        .addMessages(ImmutableMessage.builder().text(rsp.getLog()).build())
        .addAllMessages(rsp.getItems().stream()
            .flatMap(i -> i.getMessages().stream())
            .collect(Collectors.toList()))
        
        .status(BatchStatus.mapStatus(rsp.getStatus()))
        .build());
  }
}
