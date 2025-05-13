package io.resys.thena.structures.fs.actions.create;

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
import java.util.List;
import java.util.function.Consumer;

import io.resys.thena.api.actions.FsCommitActions.CreateManyDirents;
import io.resys.thena.api.actions.FsCommitActions.ManyDirentsEnvelope;
import io.resys.thena.api.actions.ImmutableManyDirentsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.fs.FsCommit;
import io.resys.thena.api.entities.fs.ImmutableFsCommit;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirent;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.fs.FsInserts.FsBatchDirents;
import io.resys.thena.structures.fs.FsState;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.structures.fs.actions.create.CreateOneDirentImpl.CreateOneDirentException;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateManyDirentsImpl implements CreateManyDirents {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private final List<Consumer<NewDirent>> dirents = new ArrayList<>();
  
  @Override
  public CreateManyDirents commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public CreateManyDirents commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public CreateManyDirents addDirent(Consumer<NewDirent> addDirent) {
    RepoAssert.notNull(addDirent, () -> "addDirent can't be empty!");
    dirents.add(addDirent);
    return this;
  }

  @Override
  public Uni<ManyDirentsEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(dirents, () -> "dirents can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withFsTransaction(scope, this::doInTx);
  }

  private Uni<ManyDirentsEnvelope> doInTx(FsState tx) {
    return 
        tx.query().direntSequences().nextVal(this.dirents.size())
        .onItem().transformToUni(nextVal -> createRequest(tx, nextVal))
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(CreateManyDirentException.class).recoverWithItem(ex -> {
          final CreateOneDirentException error = (CreateOneDirentException) ex;          
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
  
  private Uni<ManyDirentsEnvelope> createResponse(FsState tx, FsBatchDirents request) {
    return tx.insert().batchMany(request).onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneDirentException("Failed to create dirents!", rsp);
      }
      
      final ManyDirentsEnvelope result = ImmutableManyDirentsEnvelope.builder()
          .tenantId(tenantId)
          .log(rsp.getLog())
          .dirents(rsp.getDirents())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    });
  }
  
  private Uni<FsBatchDirents> createRequest(FsState tx, List<Long> nextVal) {
  
    final var start = ImmutableFsBatchDirents.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    ImmutableFsBatchDirents next = start;
    final FsCommit parentCommit;
    if(this.dirents.size() == 1) {
      parentCommit = null;
    } else {
      parentCommit = ImmutableFsCommit.builder()
        .commitId(OidUtils.gen())
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("batch of: " + this.dirents.size() + " entries")
        .build();
      next = next.withCommits(parentCommit);
    }
    
    final var sequences = nextVal.iterator();
    
    for(final var entry : this.dirents) {
      
      final var logger = new FsCommitBuilder(tenantId, 
          ImmutableFsCommit.builder()
            .commitId(OidUtils.gen())
            .commitAuthor(author)
            .commitMessage(message)
            .commitLog("")
            .createdAt(createdAt)
            .parentCommitId(parentCommit == null ? null : parentCommit.getCommitId())
            .build()
      );
      
      final var newDirent = new NewDirentBuilder(logger, sequences.next());
      entry.accept(newDirent);
      final var created = newDirent.close();
      
      final var direntId = created.getDirents().iterator().next().getId();
      
      next = ImmutableFsBatchDirents.builder()
          .from(next)
          .from(created)
          .from(logger.withDirentId(direntId).close())
          .build();
    }
    return Uni.createFrom().item(next);
  }
  
  public static class CreateManyDirentException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final FsBatchDirents batch;
    public CreateManyDirentException(String message, FsBatchDirents batch) {
      super(message);
      this.batch = batch;
    }
    public FsBatchDirents getBatch() {
      return batch;
    }
  }
}
