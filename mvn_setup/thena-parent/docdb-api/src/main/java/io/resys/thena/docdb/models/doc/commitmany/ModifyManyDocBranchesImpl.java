package io.resys.thena.docdb.models.doc.commitmany;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.actions.DocCommitActions.AddItemToModifyDocBranch;
import io.resys.thena.docdb.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.docdb.api.actions.ImmutableManyDocsEnvelope;
import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.docdb.models.doc.DocInserts.DocBatchForMany;
import io.resys.thena.docdb.models.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.docdb.models.doc.DocState.DocRepo;
import io.resys.thena.docdb.models.doc.ImmutableDocBatchForMany;
import io.resys.thena.docdb.models.doc.ImmutableDocBranchLockCriteria;
import io.resys.thena.docdb.models.doc.support.BatchForOneBranchModify;
import io.resys.thena.docdb.models.doc.support.BatchForOneDocCreate;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyManyDocBranchesImpl implements ModifyManyDocBranches {

  private final DbState state;
  private String repoId;
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
    private String message;
    private String branchName;
    private String docId;
    private JsonObject appendBlob;
    private JsonObject appendLog;
    private JsonObjectMerge appendMerge;
  }
  
  @Override public ModifyManyDocBranchesImpl repoId(String repoId) { this.repoId = RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!"); return this; }
  @Override public ModifyManyDocBranchesImpl branchName(String branchName) { this.branchName = RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!"); return this; }
  @Override public ModifyManyDocBranchesImpl author(String author) { this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); return this; }
  @Override public ModifyManyDocBranchesImpl message(String message) { this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!"); return this; }
  @Override public AddItemToModifyDocBranch item() {
    final var parent = this;
    final var item = ItemModData.builder().branchName(branchName).message(message);
    lastItem = new AddItemToModifyDocBranch() {
      @Override public AddItemToModifyDocBranch docId(String docId) { item.docId(docId); return this; }
      @Override public AddItemToModifyDocBranch remove() { item.remove(true); return this; }
      @Override public AddItemToModifyDocBranch parentIsLatest() { item.parentIsLatest(true); return this; }
      @Override public AddItemToModifyDocBranch parent(String versionToModify) { item.versionToModify(versionToModify); return this; }
      @Override public AddItemToModifyDocBranch message(String message) { item.message(message); return this; }
      @Override public AddItemToModifyDocBranch merge(JsonObjectMerge merge) { item.appendMerge(merge); return this; }
      @Override public AddItemToModifyDocBranch log(JsonObject doc) { item.appendLog(doc); return this; }
      @Override public AddItemToModifyDocBranch branchName(String branchName) { item.branchName(branchName); return this; }
      @Override public AddItemToModifyDocBranch append(JsonObject doc) { item.appendBlob(doc); return this; }
      @Override
      public ModifyManyDocBranches next() {
        final var result = item.build();
        RepoAssert.notEmpty(result.branchName, () -> "branchName can't be empty!");
        RepoAssert.notEmpty(result.docId, () -> "docId can't be empty!");
        RepoAssert.notEmpty(repoId, () -> "repoId can't be empty!");
        RepoAssert.notEmpty(author, () -> "author can't be empty!");
        RepoAssert.notEmpty(result.message, () -> "message can't be empty!");
        RepoAssert.isTrue(result.appendBlob != null || result.appendMerge != null, () -> "Nothing to commit, no content!");
        
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
    
    
    return this.state.toDocState().withTransaction(repoId, tx -> tx.query().branches().getLocks(crit).onItem().transformToUni(locks -> {
      final ManyDocsEnvelope validation = validateRepo(locks, items);
      if(validation != null) {
        return Uni.createFrom().item(validation);
      }
      return doInLock(locks, items, tx);
    }))
    .onFailure(err -> state.getErrorHandler().isLocked(err)).retry()
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

  private Uni<ManyDocsEnvelope> doInLock(List<DocBranchLock> locks, List<ItemModData> items, DocRepo tx) {
    final var lockByName = locks.stream()
      .filter(i -> i.getDoc().isPresent() && i.getBranch().isPresent())
      .collect(Collectors.toMap(
        i -> i.getDoc().get().getId() + "/" + i.getBranch().get().getBranchName(),
        i -> i
      ));
    
    final var logs = new ArrayList<String>();
    final var many = ImmutableDocBatchForMany.builder()
        .repo(tx.getRepo())
        .status(BatchStatus.OK);
    for(ItemModData item : items) {
      final var lock = lockByName.get(item.getDocId() + "/" + item.getBranchName());
      final var valid = validateRepoLock(lock, item);;
      if(valid != null) {
        many.status(BatchStatus.ERROR).addAllMessages(valid.getMessages());
      }
      
      final var batch = new BatchForOneBranchModify(lock, tx, author)
        .append(item.getAppendBlob())
        .merge(item.getAppendMerge())
        .message(item.getMessage())
        .log(item.getAppendLog())
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
      return Uni.createFrom().item(mapTo(changes));
    }
    
    return tx.insert().batchMany(changes).onItem().transform(this::mapTo);
  }
  
  private ManyDocsEnvelope mapTo(DocBatchForMany rsp) {
    return ImmutableManyDocsEnvelope.builder()
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
    .build();
  }
}
