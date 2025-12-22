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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.actions.FsCommitActions.ManyDirentsEnvelope;
import io.resys.thena.api.actions.FsCommitActions.ModifyManyDirents;
import io.resys.thena.api.actions.ImmutableManyDirentsEnvelope;
import io.resys.thena.api.entities.fs.ImmutableFsCommit;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsMergeObject.MergeDirent;
import io.resys.thena.api.entities.fs.ThenaFsObject.FsDocType;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
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
public class ModifyManyDirentsImpl implements ModifyManyDirents {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private final Map<String, Consumer<MergeDirent>> dirents = new LinkedHashMap<>();
  private ImmutableFsCommit parentCommit;
  
  
  @Override
  public ModifyManyDirents commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public ModifyManyDirents commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public ModifyManyDirents modifyDirent(String direntId, Consumer<MergeDirent> modifyDirent) {
    RepoAssert.notNull(modifyDirent, () -> "modifyDirent can't be empty!");
    RepoAssert.isTrue(!dirents.containsKey(direntId), () -> "modifyDirent with id: '" + direntId + "' already exists!");
    dirents.put(direntId, modifyDirent);
    return this;
  }
  @Override
  public Uni<ManyDirentsEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(dirents, () -> "dirents can't be empty!");

    // Create parent commit to bind all 
    if(this.dirents.size() == 1) {
      parentCommit = null;
    } else {
      parentCommit = ImmutableFsCommit.builder()
        .commitId(OidUtils.gen())
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(OffsetDateTime.now())
        .commitLog("batch of: " + this.dirents.size() + " entries")
        .build();
    }
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withFsTransaction(scope, this::doInTx);
  }

  private Uni<ManyDirentsEnvelope> doInTx(FsState tx) {
    return createRequest(tx)
        .collect().asList()
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyManyDirentsException.class).recoverWithItem(ex -> {
          final ModifyManyDirentsException error = (ModifyManyDirentsException) ex;          
          return ImmutableManyDirentsEnvelope.builder()
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

  private ManyDirentsEnvelope validateRequest(FsState tx, List<FsBatchDirents> request) {
    if(request.size() != this.dirents.size()) {
      final var found = request.stream()
          .map(i -> i.getDirents().iterator().next().getId())
          .toList();
      final var source = this.dirents.keySet().stream().toList();
      final var notFound = new ArrayList<>(source);
      notFound.removeAll(found);
      return ImmutableManyDirentsEnvelope.builder()
            .tenantId(tenantId)
            .log("")
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'")
                  .append(" is rejected.")
                  .append(" Could not find all dirents: expected: '").append(this.dirents.size()).append("' but found: '").append(request.size()).append("'!\r\n")
                  .append("  - not found: ").append(String.join(",", notFound))
                  .toString())
                .build())
            .status(CommitResultStatus.ERROR)
            .build();
    }
    return null;
  }
  
  private Uni<ManyDirentsEnvelope> createResponse(FsState tx, List<FsBatchDirents> request) {
    final var isErrors = validateRequest(tx, request);
    if(isErrors != null) {
      return Uni.createFrom().item(isErrors);
    }
    
    // Merge requests
    final var start = ImmutableFsBatchDirents.builder()
        .tenantId(tenantId)
        .log("")
        .status(BatchStatus.OK);
    if(parentCommit != null) {
      start.addCommits(parentCommit);
    }
    
    
    request.forEach(r -> start.from(r));
    
    // Patch all in current TX
    return tx.insert().batchMany(start.build()).onItem().transform(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyManyDirentsException("Failed to modify dirents!", rsp);
      }
      
      return ImmutableManyDirentsEnvelope.builder()
          .tenantId(tenantId)
          .log(rsp.getLog())
          .dirents(rsp.getDirents())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();      
    });
  }
  
  private Multi<FsBatchDirents> createRequest(FsState tx) {
    return tx.query().dirents()
    .lockForUpdate()
    .direntId(this.dirents.keySet().toArray(new String[]{}))
    .excludeDocs(FsDocType.FS_COMMIT)
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
    this.dirents.get(direntId).accept(mergeDirent);
    final var created = mergeDirent.close();
    
    next = ImmutableFsBatchDirents.builder()
        .from(start)
        .from(created)
        .from(logger.withDirentId(direntId).close())
        .build();
    return next;
  }
  
  
  public static class ModifyManyDirentsException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final FsBatchDirents batch;
    public ModifyManyDirentsException(String message, FsBatchDirents batch) {
      super(message);
      this.batch = batch;
    }
    public FsBatchDirents getBatch() {
      return batch;
    }
  }
}
