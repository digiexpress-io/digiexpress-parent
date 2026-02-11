package io.resys.thena.fs.spi.commitbuilder;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.api.commits.ImmutableCommitResult;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.spi.branch.BranchConstants;
import io.resys.thena.fs.spi.snapshot.ChangeCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.MergeFileCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.MergeFolderCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.NewFileCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.NewFolderCommand;
import io.resys.thena.fs.spi.snapshot.ChangeCommand.RmCommand;
import io.resys.thena.fs.spi.snapshot.Snapshot;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.FsDbBuilder.FsBuilderException;
import io.resys.thena.fs.tables.FsDbBuilder.PersistenceUnit;
import io.resys.thena.fs.tables.filters.ImmutableRefTableLockFilter;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CommitBuilderImpl implements CommitBuilder {
  private final Uni<FsDb> db_uni;
  private final String tenantId;
  
  // Builder state
  private String branchHeadId;
  private String branchName = BranchConstants.DEFAULT_BRANCH;
  private String commitAuthor;
  private String commitMessage;
  private OffsetDateTime commitCreatedAt;
  
  private final List<ChangeCommand> changes = new ArrayList<>();
  private final List<String> allIds = new ArrayList<>();

  @Override
  public CommitBuilder branchHead(String commitId) {
    this.branchHeadId = RepoAssert.notEmpty(commitId, () -> "branchHead commitId can't be empty!");
    return this;
  }
  @Override
  public CommitBuilder branchName(String branchName) {
    this.branchName = RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    return this;
  }
  @Override
  public CommitBuilder newFolder(Consumer<NewFolder> doc) {
    this.changes.add(new NewFolderCommand(RepoAssert.notNull(doc, () -> "newFolder consumer can't be null!")));
    return this;
  }
  @Override
  public CommitBuilder mergeFolder(String docId, BiConsumer<Node, MergeFolder> doc) {
    RepoAssert.notEmpty(docId, () -> "mergeFolder docId can't be empty!");
    RepoAssert.notNull(doc, () -> "mergeFolder consumer can't be null!");
    this.changes.add(new MergeFolderCommand(docId, doc));
    this.allIds.add(docId);
    return this;
  }
  @Override
  public CommitBuilder newFile(Consumer<NewFile> doc) {
    this.changes.add(new NewFileCommand(RepoAssert.notNull(doc, () -> "newFile consumer can't be null!")));
    return this;
  }
  @Override
  public CommitBuilder mergeFile(String docId, BiConsumer<Node, MergeFile> doc) {
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
  public CommitBuilder commitAuthor(String commitAuthor) {
    this.commitAuthor = RepoAssert.notEmpty(commitAuthor, () -> "commitAuthor can't be empty!");
    return this;
  }
  @Override
  public CommitBuilder commitMessage(String commitMessage) {
    this.commitMessage = RepoAssert.notEmpty(commitMessage, () -> "commitMessage can't be empty!");
    return this;
  }
  @Override
  public CommitBuilder commitCreatedAt(OffsetDateTime createdAt) {
    this.commitCreatedAt = createdAt;
    return this;
  }
  @Override
  public Uni<CommitResult> build() {
    // Double validation - ensure required fields were set
    RepoAssert.notEmpty(commitAuthor, () -> "commitAuthor must be set before calling build()!");
    RepoAssert.notEmpty(commitMessage, () -> "commitMessage must be set before calling build()!");
    
    // Ensure either branch name or branch head is set
    RepoAssert.isTrue(branchName != null || branchHeadId != null, 
        () -> "Either branchName or branchHead must be set before calling build()!");
    
    // Ensure at least one operation is defined
    RepoAssert.isTrue(!changes.isEmpty(), 
        () -> "At least one operation (newFolder, mergeFolder, newFile, mergeFile, or remove) must be added before calling build()!");
    
    final var scope = ImmutableTxScope.builder()
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage)
        .tenantId(tenantId)
        .build();
    return this.db_uni
        .onItem().transformToUni(db -> db.withTransaction(scope, this::visitTransaction))
        .onFailure(t -> !(t instanceof CommitBuilderException))
        .recoverWithItem(this::visitFailure);
  }
  

  
  private Uni<CommitResult> visitTransaction(FsDb tx) {
    return visitLock(tx)
        .onItem().transformToUni(lock -> visitPersistenceUnit(tx, lock))
        .onItem().transform(this::visitSuccess);
  }
  
  private CommitResult visitFailure(Throwable t) {
    if(t instanceof FsBuilderException) {
      final var fs = (FsBuilderException) t;
      final var builder = ImmutableCommitResult.builder()
          .tenantId(tenantId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .exception(fs)
              .text(fs.getMessage())
              .build());
      
      if(t.getCause() != null) {
        return builder
          .addMessages(ImmutableMessage.builder().text(fs.getCause().getMessage()).build())
          .build();
      }
      
      return builder.build();
    }
    
    return ImmutableCommitResult.builder()
        .tenantId(tenantId)
        .status(CommitResultStatus.ERROR)
        .addMessages(ImmutableMessage.builder()
            .text("Commit has failed with unknown exception!")
            .exception(t)
            .build())
        .build();
    
  }
  
  private CommitResult visitSuccess(Snapshot.SnapshotResult result) {
    final PersistenceUnit unit = result.getPersistenceUnit();
    return ImmutableCommitResult.builder()
      .addAllMessages(
          unit.getCommitMessages().stream()
            .map(text -> ImmutableMessage.builder().text(text).build())
            .toList()
      )
      .addAllMessages(unit.getCommitLogs())
      .tenantId(tenantId)
      .status(mapCommitStatus(unit.getStatus()))
      .log(result.getLog())
      .commit(unit.getCommitInserts().isEmpty() ? null : unit.getCommitInserts().getLast())
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
      .refName(branchName)
      .docIds(Optional.ofNullable(allIds.isEmpty() ? null : allIds))
      .build();
    return tx.query().queryRef().findOneWithLock(query);
  }
  
  private Uni<Snapshot.SnapshotResult> visitPersistenceUnit(FsDb tx, Optional<Ref> lock) {
    try {
      final var createdAt = commitCreatedAt != null ? commitCreatedAt : OffsetDateTime.now();
      final var snapshot = new Snapshot(tenantId, lock, branchName, createdAt);
      final var unit = snapshot.addAll(this.changes).build(commitAuthor, commitMessage, createdAt);

      return tx.builder().from(unit.getPersistenceUnit()).persist()
          .map(persisted -> new Snapshot.SnapshotResult(persisted, unit.getLog()));
      
    } catch(Exception e) {
      throw new CommitBuilderException(e, JsonObject.of("tenantId", tenantId, "message", e.getMessage()));
    }
  }
}
