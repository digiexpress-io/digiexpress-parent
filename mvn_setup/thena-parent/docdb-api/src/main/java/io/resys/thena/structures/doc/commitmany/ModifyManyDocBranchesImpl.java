package io.resys.thena.structures.doc.commitmany;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocCommitActions.AddItemToModifyDocBranch;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.GitCommitActions.JsonObjectMerge;
import io.resys.thena.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.doc.ImmutableDocBatchForMany;
import io.resys.thena.structures.doc.ImmutableDocBranchLockCriteria;
import io.resys.thena.structures.doc.support.BatchForOneBranchModify;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyManyDocBranchesImpl implements ModifyManyDocBranches {

  private final DbState state;
  private final String repoId;
  private String branchName;
  private String author;
  private String message;
  private final List<ItemModData> items = new ArrayList<ItemModData>();
  private AddItemToModifyDocBranch lastItem;
  
  @Data @Builder
  private static class ItemModData {
    private Boolean parentIsLatest;
    private Boolean remove;
    private String versionToModify;
    private String branchName;
    private String docId;
    private JsonObject replace;
    private List<JsonObject> commands;
    private JsonObjectMerge merge;
  }
  @Override public int getItemsAdded() { return items.size();}
  @Override public ModifyManyDocBranchesImpl commitAuthor(String author) { this.author = RepoAssert.notEmpty(author, () -> "commitAuthor can't be empty!"); return this; }
  @Override public ModifyManyDocBranchesImpl commitMessage(String message) { this.message = RepoAssert.notEmpty(message, () -> "commitMessage can't be empty!"); return this; }
  @Override public AddItemToModifyDocBranch item() {
    final var parent = this;
    final var item = ItemModData.builder().branchName(branchName);
    lastItem = new AddItemToModifyDocBranch() {
      @Override public AddItemToModifyDocBranch docId(String docId) { item.docId(docId); return this; }
      @Override public AddItemToModifyDocBranch remove() { item.remove(true); return this; }
      @Override public AddItemToModifyDocBranch parentIsLatest() { item.parentIsLatest(true); return this; }
      @Override public AddItemToModifyDocBranch commit(String versionToModify) { item.versionToModify(versionToModify); return this; }
      @Override public AddItemToModifyDocBranch merge(JsonObjectMerge merge) { item.merge(merge); return this; }
      @Override public AddItemToModifyDocBranch commands(List<JsonObject> doc) { item.commands(doc); return this; }
      @Override public AddItemToModifyDocBranch branchName(String branchName) { item.branchName(branchName); return this; }
      @Override public AddItemToModifyDocBranch replace(JsonObject doc) { item.replace(doc); return this; }
      @Override
      public ModifyManyDocBranches next() {
        final var result = item.build();
        RepoAssert.notEmpty(result.branchName, () -> "branchName can't be empty!");
        RepoAssert.notEmpty(result.docId, () -> "docId can't be empty!");
        RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
        RepoAssert.notEmpty(author, () -> "author can't be empty!");
        RepoAssert.isTrue(result.replace != null || result.merge != null, () -> "Nothing to commit, no content!");
        
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
    RepoAssert.isTrue(!items.isEmpty(), () -> "Nothing to commit, no content!");
    
    final var crit = this.items.stream()
      .map(item -> (DocBranchLockCriteria) ImmutableDocBranchLockCriteria.builder()
          .branchName(item.getBranchName())
          .docId(item.getDocId())
          .build())
      .collect(Collectors.toList());
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(repoId).build();
    return this.state.withDocTransaction(scope, tx -> tx.query().branches().getBranchLocks(crit).onItem().transformToUni(locks -> {
      final ManyDocsEnvelope validation = validateRepo(locks, items);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(locks, items, tx);
    }))
    .onFailure(err -> state.getDataSource().isLocked(err)).retry()
      .withJitter(0.3) // every retry increase time by x 3
      .withBackOff(Duration.ofMillis(100))
      .atMost(100);
  }

  private ManyDocsEnvelope validateRepo(List<DocBranchLock> state, List<ItemModData> items) {
    // cant merge on first commit
    final var found = state.stream()
        .filter(i -> i.getDoc().isPresent() && i.getBranch().isPresent())
        .map(i -> i.getDoc().get().getId() + "/" + i.getBranch().get().getBranchName())
        .toList();
    final var source = items.stream().map(i -> i.getDocId() + "/" + i.getBranchName()).toList();
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
  private ManyDocsEnvelope validateRepoLock(DocBranchLock state, ItemModData item) {
    // Wrong parent commit
    final var versionToModify = item.getVersionToModify();
    if(state.getCommit().isPresent() && versionToModify != null && 
        !versionToModify.equals(state.getCommit().get().getId())) {
      
      final var text = new StringBuilder()
        .append("Commit to: '").append(repoId).append("'")
        .append(" is rejected.")
        .append(" Your head is: '").append(versionToModify).append("')")
        .append(" but remote is: '").append(state.getCommit().get().getId()).append("'!")
        .toString();
      
      return ImmutableManyDocsEnvelope.builder()
          .repoId(repoId)
          .addMessages(ImmutableMessage.builder().text(text).build())
          .status(CommitResultStatus.ERROR)
          .build();
    }
    return null;
  }

  private Uni<ManyDocsEnvelope> doInLock(List<DocBranchLock> locks, List<ItemModData> items, DocState tx) {
    final var lockByName = locks.stream()
      .filter(i -> i.getDoc().isPresent() && i.getBranch().isPresent())
      .collect(Collectors.toMap(
        i -> i.getDoc().get().getId() + "/" + i.getBranch().get().getBranchName(),
        i -> i
      ));
    
    final var logs = new ArrayList<String>();
    final var many = ImmutableDocBatchForMany.builder()
        .repo(tx.getDataSource().getTenant().getId())
        .status(BatchStatus.OK);
    for(ItemModData item : items) {
      final var lock = lockByName.get(item.getDocId() + "/" + item.getBranchName());
      final var valid = validateRepoLock(lock, item);;
      if(valid != null) {
        many.status(BatchStatus.ERROR).addAllMessages(valid.getMessages());
      }
      
      final var batch = new BatchForOneBranchModify(lock, tx, author, message)
        .replace(item.getReplace())
        .merge(item.getMerge())
        .commands(item.getCommands())
        .remove(item.getRemove() == null ? false : item.getRemove())
        .create();
      
      logs.add(batch.getLog());
      many.addItems(batch);
    }
    final var changes = many
        .log(String.join("\r\n" + "\r\n", logs))
        .build();
    if(changes.getStatus() != BatchStatus.OK) {
      return Uni.createFrom().item(BatchForOneBranchModify.mapTo(changes));
    }
    
    return tx.insert().batchMany(changes).onItem().transform(BatchForOneBranchModify::mapTo);
  }  

}
