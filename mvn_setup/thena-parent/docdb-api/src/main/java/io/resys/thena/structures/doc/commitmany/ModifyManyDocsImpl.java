package io.resys.thena.structures.doc.commitmany;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.AddItemToModifyDoc;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocLock;
import io.resys.thena.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;
import io.resys.thena.structures.doc.DocState.DocRepo;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocLockCriteria;
import io.resys.thena.structures.doc.support.BatchForOneBranchModify;
import io.resys.thena.structures.doc.support.BatchForOneDocModify;
import io.resys.thena.structures.git.GitInserts.BatchStatus;
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
    private String message;
    private String docId;
    private JsonObject appendLog;
    private JsonObject appendMeta;
  }
  
  @Override public ModifyManyDocsImpl author(String author) { this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); return this; }
  @Override public ModifyManyDocsImpl message(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  @Override public AddItemToModifyDoc item() {
    final var parent = this;
    final var item = ItemModData.builder().message(message);
    lastItem = new AddItemToModifyDoc() {
      @Override public AddItemToModifyDoc docId(String docId) { item.docId(docId); return this; }
      @Override public AddItemToModifyDoc remove() { item.remove(true); return this; }
      @Override public AddItemToModifyDoc message(String message) { item.message(message); return this; }
      @Override public AddItemToModifyDoc log(JsonObject log) { item.appendLog(log); return this; }
      @Override public AddItemToModifyDoc meta(JsonObject meta) { item.appendMeta(meta); return this; }
      @Override public ModifyManyDocs next() {
        final var result = item.build();
        RepoAssert.notEmpty(result.docId, () -> "docId can't be empty!");
        RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
        RepoAssert.notEmpty(result.message, () -> "message can't be empty!");
        
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
      
    
    return this.state.toDocState().withTransaction(repoId, tx -> tx.query().branches().getDocLocks(crit).onItem().transformToUni(lock -> {
      final ManyDocsEnvelope validation = validateRepo(lock, items);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(lock, items, tx);
    }))
    .onFailure(err -> state.getErrorHandler().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }
  
  private Uni<ManyDocsEnvelope> doInLock(List<DocLock> locks, List<ItemModData> items, DocRepo tx) {
    final var lockById = locks.stream()
        .collect(Collectors.toMap(
          i -> i.getDoc().get().getId(),
          i -> i
        ));
      
      final var logs = new ArrayList<String>();
      final var many = ImmutableDocBatchForMany.builder()
          .repo(tx.getRepo())
          .status(BatchStatus.OK);
      for(ItemModData item : items) {
        final var lock = lockById.get(item.getDocId());
        final var valid = validateDocLock(lock, item);;
        if(valid != null) {
          many.status(BatchStatus.ERROR).addAllMessages(valid.getMessages());
        }
        
        final var batch = new BatchForOneDocModify(lock, tx, author)
          .message(item.getMessage())
          .log(item.getAppendLog())
          .meta(item.getAppendMeta())
          .remove(item.getRemove() == null ? false : item.getRemove())
          .create();
        
        logs.add(batch.getLog().getText());
        many.addItems(batch);
      }
      final var changes = many
          .log(ImmutableMessage.builder()
              .text(String.join("\r\n" + "\r\n", logs))
              .build())
          .build();
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
          .status(Tenant.CommitResultStatus.ERROR)
          .build();
      
    }
    return null;
  }
  private ManyDocsEnvelope validateDocLock(DocLock state, ItemModData item) {
    return null;
  }
}
