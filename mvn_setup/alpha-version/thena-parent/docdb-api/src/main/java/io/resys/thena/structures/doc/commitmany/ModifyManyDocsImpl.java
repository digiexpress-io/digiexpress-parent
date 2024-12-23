package io.resys.thena.structures.doc.commitmany;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.AddItemToModifyDoc;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocLockCriteria;
import io.resys.thena.structures.doc.support.BatchForOneBranchModify;
import io.resys.thena.structures.doc.support.BatchForOneDocModify;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyManyDocsImpl implements ModifyManyDocs {

  private final DbState state;
  private final String repoId;
  private String author;
  private String message;
  
  private final List<ItemModData> items = new ArrayList<ItemModData>();
  private AddItemToModifyDoc lastItem;
  
  @Data @Builder
  private static class ItemModData {
    private Boolean remove;
    private String docId;
    private Optional<String> parentDocId;
    private Optional<String> ownerId;
    private Optional<String> externalId;
    private List<JsonObject> commands;
    private Optional<JsonObject> meta;
  }
  
  @Override public ModifyManyDocsImpl commitAuthor(String author) { this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); return this; }
  @Override public ModifyManyDocsImpl commitMessage(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  @Override public AddItemToModifyDoc item() {
    final var parent = this;
    final var item = ItemModData.builder();
    lastItem = new AddItemToModifyDoc() {
      @Override public AddItemToModifyDoc docId(String docId) { item.docId(docId); return this; }
      @Override public AddItemToModifyDoc externalId(String externalId) { item.externalId(Optional.ofNullable(externalId)); return this; }
      @Override public AddItemToModifyDoc parentDocId(String parentDocId) { item.parentDocId(Optional.ofNullable(parentDocId)); return this; }
      @Override public AddItemToModifyDoc ownerId(String ownerId) { item.ownerId(Optional.ofNullable(ownerId)); return this; }
      @Override public AddItemToModifyDoc remove() { item.remove(true); return this; }
      @Override public AddItemToModifyDoc commands(List<JsonObject> log) { item.commands(log); return this; }
      @Override public AddItemToModifyDoc meta(JsonObject meta) { item.meta(Optional.ofNullable(meta)); return this; }
      @Override public ModifyManyDocs next() {
        final var result = item.build();
        RepoAssert.notEmpty(result.docId, () -> "docId can't be empty!");
        RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
        
        lastItem = null;
        items.add(result);
        return parent;
      }
    };
    return lastItem;
  }

  @Override
  public Uni<ManyDocsEnvelope> build() {
    RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
  
    final var crit = this.items.stream()
        .map(item -> (DocLockCriteria) ImmutableDocLockCriteria.builder()
            .docId(item.getDocId())
            .build())
        .collect(Collectors.toList());
      
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, tx -> tx.query().branches().getDocLocks(crit).onItem().transformToUni(lock -> {
      final ManyDocsEnvelope validation = validateRepo(lock, items);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(lock, items, tx);
    }))
    .onFailure(err -> state.getDataSource().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }
  
  private Uni<ManyDocsEnvelope> doInLock(List<DocLock> locks, List<ItemModData> items, DocState tx) {
    final var lockById = locks.stream()
        .collect(Collectors.toMap(
          i -> i.getDoc().get().getId(),
          i -> i
        ));
      
      final var logs = new ArrayList<String>();
      final var many = ImmutableDocBatchForMany.builder()
          .repo(tx.getDataSource().getTenant().getId())
          .status(BatchStatus.OK);
      for(ItemModData item : items) {
        final var lock = lockById.get(item.getDocId());
        final var valid = validateDocLock(lock, item);
        if(valid != null) {
          many.status(BatchStatus.ERROR).addAllMessages(valid.getMessages());
        }
        
        final var batch = new BatchForOneDocModify(lock, tx, author, message)
          .meta(item.getMeta())
          .remove(item.getRemove() == null ? false : item.getRemove())
          .commands(item.getCommands())
          .parentId(item.getParentDocId())
          .ownerId(item.getOwnerId())
          .externalId(item.getExternalId())
          .create();
        
        logs.add(batch.getLog());
        many.addItems(batch);
      }
      final var changes = many.log(String.join("\r\n" + "\r\n", logs)).build();
      if(changes.getStatus() != BatchStatus.OK) {
        return Uni.createFrom().item(BatchForOneBranchModify.mapTo(changes));
      }
      return tx.insert().batchMany(changes).onItem().transform(BatchForOneBranchModify::mapTo);
  }
  
  private ManyDocsEnvelope validateRepo(List<DocLock> state, List<ItemModData> items) {
    final var found = state.stream()
        .filter(i -> i.getDoc().isPresent())
        .map(i -> i.getDoc().get().getId())
        .toList();
    final var source = items.stream().map(i -> i.getDocId()).toList();
    final var notFound = new ArrayList<>(source);
    notFound.removeAll(found);
    
    
    if(!notFound.isEmpty()) {      
      return ImmutableManyDocsEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                .append("Commit to: '").append(repoId).append("'")
                .append(" is rejected.")
                .append(" Could not find all items: expected: '").append(items.size()).append("' but found: '").append(state.size()).append("'!\r\n")
                .append("  - not found: ").append(String.join(",", notFound))
                .toString())
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }
  private ManyDocsEnvelope validateDocLock(DocLock state, ItemModData item) {
    return null;
  }
}
