package io.resys.thena.fs.spi.commitbuilder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.api.commits.ImmutableCommitResult;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.spi.snapshot.Snapshot;
import io.resys.thena.fs.spi.snapshot.Snapshot.ChangeCommand;
import io.resys.thena.fs.spi.snapshot.Snapshot.MergeFileCommand;
import io.resys.thena.fs.spi.snapshot.Snapshot.MergeFolderCommand;
import io.resys.thena.fs.spi.snapshot.Snapshot.NewFileCommand;
import io.resys.thena.fs.spi.snapshot.Snapshot.NewFolderCommand;
import io.resys.thena.fs.spi.snapshot.Snapshot.RmCommand;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.FsDbBuilder.PersistenceUnit;
import io.resys.thena.fs.tables.filters.ImmutableRefTableLockFilter;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CommitBuilderImpl implements CommitBuilder {
  private final Uni<FsDb> db_uni;
  private final String tenantId;
  
  // Builder state
  private String branchHeadId;
  private String branchNameValue = "main";
  private String commitAuthorValue;
  private String commitMessageValue;
  private OffsetDateTime commitCreatedAtValue;
  
  private final List<ChangeCommand> changes = new ArrayList<>();
  private final List<String> allIds = new ArrayList<>();

  @Override
  public CommitBuilder branchHead(String commitId) {
    RepoAssert.notEmpty(commitId, () -> "branchHead commitId can't be empty!");
    this.branchHeadId = commitId;
    return this;
  }

  @Override
  public CommitBuilder branchName(String branchName) {
    RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    this.branchNameValue = branchName;
    return this;
  }

  @Override
  public CommitBuilder newFolder(Consumer<NewFolder> doc) {
    RepoAssert.notNull(doc, () -> "newFolder consumer can't be null!");
    this.changes.add(new NewFolderCommand(doc));
    return this;
  }

  @Override
  public CommitBuilder mergeFolder(String docId, Consumer<MergeFolder> doc) {
    RepoAssert.notEmpty(docId, () -> "mergeFolder docId can't be empty!");
    RepoAssert.notNull(doc, () -> "mergeFolder consumer can't be null!");
    this.changes.add(new MergeFolderCommand(docId, doc));
    this.allIds.add(docId);
    return this;
  }

  @Override
  public CommitBuilder newFile(Consumer<NewFile> doc) {
    RepoAssert.notNull(doc, () -> "newFile consumer can't be null!");
    this.changes.add(new NewFileCommand(doc));
    return this;
  }

  @Override
  public CommitBuilder mergeFile(String docId, Consumer<MergeFile> doc) {
    RepoAssert.notEmpty(docId, () -> "mergeFile docId can't be empty!");
    RepoAssert.notNull(doc, () -> "mergeFile consumer can't be null!");
    this.changes.add(new MergeFileCommand(docId, doc));
    this.allIds.add(docId);
    return this;
  }

  @Override
  public CommitBuilder remove(String docId) {
    RepoAssert.notEmpty(docId, () -> "remove docId can't be empty!");
    this.changes.add(new RmCommand(docId));
    this.allIds.add(docId);
    return this;
  }

  @Override
  public CommitBuilder remove(List<String> docIds) {
    RepoAssert.notNull(docIds, () -> "remove docIds can't be null!");
    RepoAssert.isTrue(!docIds.isEmpty(), () -> "remove docIds can't be empty!");
    
    // Validate each docId
    for (final var docId : docIds) {
      RepoAssert.notEmpty(docId, () -> "remove docId in list can't be empty!");
      this.changes.add(new RmCommand(docId));
    }
    this.allIds.addAll(docIds);
    return this;
  }

  @Override
  public CommitBuilder commitAuthor(String author) {
    RepoAssert.notEmpty(author, () -> "commitAuthor can't be empty!");
    this.commitAuthorValue = author;
    return this;
  }

  @Override
  public CommitBuilder commitMessage(String message) {
    RepoAssert.notEmpty(message, () -> "commitMessage can't be empty!");
    this.commitMessageValue = message;
    return this;
  }

  @Override
  public CommitBuilder commitCreatedAt(OffsetDateTime createdAt) {
    this.commitCreatedAtValue = createdAt;
    return this;
  }

  @Override
  public Uni<CommitResult> build() {
    // Double validation - ensure required fields were set
    RepoAssert.notEmpty(commitAuthorValue, () -> "commitAuthor must be set before calling build()!");
    RepoAssert.notEmpty(commitMessageValue, () -> "commitMessage must be set before calling build()!");
    
    // Ensure either branch name or branch head is set
    RepoAssert.isTrue(branchNameValue != null || branchHeadId != null, 
        () -> "Either branchName or branchHead must be set before calling build()!");
    
    // Ensure at least one operation is defined
    RepoAssert.isTrue(!changes.isEmpty(), 
        () -> "At least one operation (newFolder, mergeFolder, newFile, mergeFile, or remove) must be added before calling build()!");
    
    final var scope = ImmutableTxScope.builder()
        .commitAuthor(commitAuthorValue)
        .commitMessage(commitMessageValue)
        .tenantId(tenantId)
        .build();
    return this.db_uni.onItem().transformToUni(db -> db.withTransaction(scope, this::visitTransaction));
  }
  
  private Uni<CommitResult> visitTransaction(FsDb tx) {
    return visitLock(tx)
        .onItem().transformToUni(lock -> visitPersistenceUnit(tx, lock))
        .onItem().transform(this::visitSuccess)
        .onFailure().recoverWithItem(this::visitFailure);
  }
  
  private CommitResult visitFailure(Throwable t) {
    return ImmutableCommitResult.builder()
        .tenantId(tenantId)
        .status(CommitResultStatus.ERROR)
        .addMessages(ImmutableMessage.builder()
            .text("Commit has failed with unknown exception!")
            .exception(t)
            .build())
        .build();
    
  }
  
  private CommitResult visitSuccess(PersistenceUnit unit) {
    return ImmutableCommitResult.builder()
      .addAllMessages(
          unit.getCommitMessages().stream()
            .map(text -> ImmutableMessage.builder().text(text).build())
            .toList()
      )
      .addAllMessages(unit.getCommitLogs())
      .status(mapCommitStatus(unit.getStatus()))
      .commit(unit.getCommitInserts().getLast())
      .build();
  }

  private static CommitResultStatus mapCommitStatus(BatchStatus src) {
    if(src == BatchStatus.OK) {
      return CommitResultStatus.OK;
    } else if(src == BatchStatus.CONFLICT) {
      return CommitResultStatus.CONFLICT;
    }
    return CommitResultStatus.ERROR;
  }
  
  private Uni<Optional<Ref>> visitLock(FsDb tx) {    
    final var query = ImmutableRefTableLockFilter.builder()
      .refName(branchNameValue)
      .docIds(Optional.ofNullable(allIds.isEmpty() ? null : allIds))
      .build();
    return tx.query().queryRef().findOneWithLock(query);
  }
  
  private Uni<PersistenceUnit> visitPersistenceUnit(FsDb tx, Optional<Ref> lock) {
    final var createdAt = commitCreatedAtValue != null ? commitCreatedAtValue : OffsetDateTime.now();
    final var snapshot = new Snapshot(lock, branchNameValue);
    final var unit = snapshot.addAll(this.changes).build(commit -> commit
        .commitAuthor(commitAuthorValue)
        .commitMessage(commitMessageValue)
        .commitCreatedAt(createdAt));
    return tx.builder().from(unit).persist();
  }
}
