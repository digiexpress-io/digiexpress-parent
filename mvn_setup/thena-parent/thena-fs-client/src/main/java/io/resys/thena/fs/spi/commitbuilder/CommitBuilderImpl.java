package io.resys.thena.fs.spi.commitbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.FsDbBuilder.PersistenceUnit;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
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
  private String branchNameValue;
  private String commitAuthorValue;
  private String commitMessageValue;
  private final List<Consumer<NewFolder>> newFolders = new ArrayList<>();
  private final List<MergeFolderCommand> mergeFolders = new ArrayList<>();
  private final List<Consumer<NewFile>> newFiles = new ArrayList<>();
  private final List<MergeFileCommand> mergeFiles = new ArrayList<>();
  private final List<String> removedDocIds = new ArrayList<>();
  
  private static record MergeFolderCommand(String docId, Consumer<MergeFolder> consumer) {}
  private static record MergeFileCommand(String docId, Consumer<MergeFile> consumer) {}

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
    this.newFolders.add(doc);
    return this;
  }

  @Override
  public CommitBuilder mergeFolder(String docId, Consumer<MergeFolder> doc) {
    RepoAssert.notEmpty(docId, () -> "mergeFolder docId can't be empty!");
    RepoAssert.notNull(doc, () -> "mergeFolder consumer can't be null!");
    this.mergeFolders.add(new MergeFolderCommand(docId, doc));
    return this;
  }

  @Override
  public CommitBuilder newFile(Consumer<NewFile> doc) {
    RepoAssert.notNull(doc, () -> "newFile consumer can't be null!");
    this.newFiles.add(doc);
    return this;
  }

  @Override
  public CommitBuilder mergeFile(String docId, Consumer<MergeFile> doc) {
    RepoAssert.notEmpty(docId, () -> "mergeFile docId can't be empty!");
    RepoAssert.notNull(doc, () -> "mergeFile consumer can't be null!");
    this.mergeFiles.add(new MergeFileCommand(docId, doc));
    return this;
  }

  @Override
  public CommitBuilder remove(String docId) {
    RepoAssert.notEmpty(docId, () -> "remove docId can't be empty!");
    this.removedDocIds.add(docId);
    return this;
  }

  @Override
  public CommitBuilder remove(List<String> docIds) {
    RepoAssert.notNull(docIds, () -> "remove docIds can't be null!");
    RepoAssert.isTrue(!docIds.isEmpty(), () -> "remove docIds can't be empty!");
    
    // Validate each docId
    for (final var docId : docIds) {
      RepoAssert.notEmpty(docId, () -> "remove docId in list can't be empty!");
    }
    
    this.removedDocIds.addAll(docIds);
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
  public Uni<CommitResult> build() {
    // Double validation - ensure required fields were set
    RepoAssert.notEmpty(commitAuthorValue, () -> "commitAuthor must be set before calling build()!");
    RepoAssert.notEmpty(commitMessageValue, () -> "commitMessage must be set before calling build()!");
    
    // Ensure either branch name or branch head is set
    RepoAssert.isTrue(branchNameValue != null || branchHeadId != null, 
        () -> "Either branchName or branchHead must be set before calling build()!");
    
    // Ensure at least one operation is defined
    final boolean hasOperations = !newFolders.isEmpty() || !mergeFolders.isEmpty() || 
        !newFiles.isEmpty() || !mergeFiles.isEmpty() || !removedDocIds.isEmpty();
    RepoAssert.isTrue(hasOperations, 
        () -> "At least one operation (newFolder, mergeFolder, newFile, mergeFile, or remove) must be added before calling build()!");
    
    final var scope = ImmutableTxScope.builder()
        .commitAuthor(commitAuthorValue)
        .commitMessage(commitMessageValue)
        .tenantId(tenantId)
        .build();
    return this.db_uni.onItem().transformToUni(db -> db.withTransaction(scope, this::doInTx));
  }
  
  private Uni<CommitResult> doInTx(FsDb tx) {
    
    // choose locking 
    tx.query().queryRef().findOneWithLock(null);
    
    return tx.builder().from(visitPersistenceUnit()).persist();
  }
  
  private PersistenceUnit visitPersistenceUnit() {
    final var unit = ImmutablePersistenceUnit.builder().build();
  }
}
