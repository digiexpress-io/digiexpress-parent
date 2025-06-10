package io.resys.thena.structures.fs.actions.modify;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.actions.FsCommitActions.ModifyOneDirent;
import io.resys.thena.api.actions.FsCommitActions.OneDirentEnvelope;
import io.resys.thena.api.actions.ImmutableOneDirentEnvelope;
import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.fs.ImmutableFsCommit;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject.MergeDirent;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.fs.FsInserts.FsBatchDirents;
import io.resys.thena.structures.fs.FsState;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneDirentImpl implements ModifyOneDirent {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private String direntId;
  private Consumer<MergeDirent> dirent;
  
  @Override
  public ModifyOneDirent commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public ModifyOneDirent commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public ModifyOneDirent direntId(String direntId) {
    this.direntId = RepoAssert.notEmpty(direntId, () -> "direntId can't be empty!");
    return this;
  }
  @Override
  public ModifyOneDirent modifyDirent(Consumer<MergeDirent> modifyDirent) {
    RepoAssert.notNull(modifyDirent, () -> "modifyDirent can't be empty!");
    dirent = modifyDirent;
    return this;
  }
  @Override
  public Uni<OneDirentEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(dirent, () -> "modifyDirent can't be empty!");
    RepoAssert.notEmpty(direntId, () -> "dirents can't be empty!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withFsTransaction(scope, this::doInTx);
  }

  private Uni<OneDirentEnvelope> doInTx(FsState tx) {
    return createRequest(tx)
        .collect().asList()
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyOneDirentException.class).recoverWithItem(ex -> {
          final ModifyOneDirentException error = (ModifyOneDirentException) ex;          
          return ImmutableOneDirentEnvelope.builder()
            .tenantId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'").append(" is rejected.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(error.getMessage())
                  .toString())
                .exception(error)
                .build())
            .status(CommitResultStatus.ERROR)
          .build();
        });
  }

  private OneDirentEnvelope validateRequest(FsState tx, List<FsBatchDirents> request) {
    if(request.size() != 1) {
      return ImmutableOneDirentEnvelope.builder()
            .tenantId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'")
                  .append(" is rejected.")
                  .append(" Could not find dirent, expected: '1' but found: '").append(request.size()).append("'!\r\n")
                  .append("  - not found: ").append(String.join(",", direntId))
                  .toString())
                .build())
            .status(CommitResultStatus.ERROR)
            .build();
    }
    return null;
  }
  
  private Uni<OneDirentEnvelope> createResponse(FsState tx, List<FsBatchDirents> request) {
    final var isErrors = validateRequest(tx, request);
    if(isErrors != null) {
      return Uni.createFrom().item(isErrors);
    }
    
    // Merge requests
    final var start = ImmutableFsBatchDirents.builder()
        .tenantId(tenantId)
        .log("")
        .status(BatchStatus.OK);
    
    
    request.forEach(r -> start.from(r));
    
    // Patch all in current TX
    return tx.insert().batchMany(start.build()).onItem().transformToUni(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyOneDirentException("Failed to modify dirents!", rsp);
      }

      return tx.query().dirents()
          .direntId(this.direntId)
          
          .findAll().collect().asList()
          .onItem().transform(container -> {
            final var item = container.iterator().next();
            return ImmutableOneDirentEnvelope.builder()
              .tenantId(tenantId)
              .dirent(item.getDirent())
              .addAllLabels(item.getDirentLabels().values())
              .addAllLinks(item.getLinks().values())
              .addAllRemarks(item.getRemarks().values())
              .addAllAssignments(item.getAssignments().values())
              .addAllMessages(rsp.getMessages())
              .status(BatchStatus.mapStatus(rsp.getStatus()))
              .build();
          });
            
    });
  }
  
  private Multi<FsBatchDirents> createRequest(FsState tx) {
    return tx.query().dirents()
    .direntId(this.direntId)
    .lockForUpdate()
    .findAll().onItem().transform(labels -> createRequest(tx, labels));
  }
  
  
  private FsBatchDirents createRequest(FsState tx, FsDirentContainer container) {
    RepoAssert.isTrue(container.getDirents().size() == 1, () -> "Dirent container must be grouped by dirents, one dirent per container!");
    
    final var dirent = container.getDirents().values().iterator().next();
    final var direntId = dirent.getId();
    
    final var start = ImmutableFsBatchDirents.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    
    ImmutableFsBatchDirents next = start;    
    final var logger = new FsCommitBuilder(tenantId, 
        ImmutableFsCommit.builder()
          .commitId(OidUtils.gen())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .parentCommitId(Optional.ofNullable(dirent.getUpdatedTreeWithCommitId()).orElse(dirent.getCommitId()))
          .build()
    );
    
    final var mergeDirent = new MergeDirentBuilder(container, logger);
    this.dirent.accept(mergeDirent);
    final var created = mergeDirent.close();
    
    next = ImmutableFsBatchDirents.builder()
        .from(start)
        .from(created)
        .from(logger.withDirentId(direntId).close())
        .build();
    return next;
  }
  
  
  public static class ModifyOneDirentException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final FsBatchDirents batch;
    public ModifyOneDirentException(String message, FsBatchDirents batch) {
      super(message);
      this.batch = batch;
    }
    public FsBatchDirents getBatch() {
      return batch;
    }
  }
}
