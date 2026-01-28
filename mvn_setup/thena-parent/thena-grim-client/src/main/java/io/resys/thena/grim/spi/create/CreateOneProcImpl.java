package io.resys.thena.grim.spi.create;

/*-
 * #%L
 * thena-grim-client
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
import java.util.function.Consumer;

import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewProcess;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.grim.api.GrimCommitActions.CreateOneProc;
import io.resys.thena.grim.api.GrimCommitActions.OneProcEnvelope;
import io.resys.thena.grim.api.ImmutableOneProcEnvelope;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchMissions;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.grim.spi.commitlog.GrimCommitBuilder;
import io.resys.thena.grim.spi.create.CreateOneMissionsImpl.CreateOneMissionException;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneProcImpl implements CreateOneProc {

  private final GrimDataSource state;
  private final String tenantId;
  
  private String author;
  private String message;
  private Consumer<NewProcess> newProc;
  
  @Override
  public CreateOneProc commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public CreateOneProc commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public CreateOneProc proc(Consumer<NewProcess> newProc) {
    RepoAssert.notNull(newProc, () -> "newProc can't be empty!");
    this.newProc = newProc;
    return this;
  }

  @Override
  public Uni<OneProcEnvelope> build() {
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }
  
  private Uni<OneProcEnvelope> doInTx(GrimState tx) {
    return tx.missionProcSequences().nextVal().onItem()
        .transformToUni(nextVal -> createRequest(tx, nextVal))
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(CreateOneMissionException.class).recoverWithItem(ex -> {
          final CreateOneMissionException error = (CreateOneMissionException) ex;          
          return ImmutableOneProcEnvelope.builder()
            .repoId(tenantId)
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
  
  private Uni<OneProcEnvelope> createResponse(GrimState tx, GrimBatchMissions request) {
    return tx.batchMany(request).onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneMissionException("Failed to create mission!", rsp);
      }
      
      final OneProcEnvelope result = ImmutableOneProcEnvelope.builder()
          .repoId(tenantId)
          .proc(rsp.getProcs().iterator().next())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    });
  }
  
  private Uni<GrimBatchMissions> createRequest(GrimState tx, Long nextVal) {
    final var start = ImmutableGrimBatchMissions.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    ImmutableGrimBatchMissions next = start;

    
    final var logger = new GrimCommitBuilder(tenantId, 
        ImmutableGrimCommit.builder()
          .commitId(OidUtils.gen())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)

          .build()
    );
    
    final var newMissionProc = new NewProcessBuilder(logger, null, nextVal);
    this.newProc.accept(newMissionProc);
    final var created = newMissionProc.close();
    
    next = ImmutableGrimBatchMissions.builder()
        .from(start)
        .from(created)
        .from(logger.close())
        .build();
  
    return Uni.createFrom().item(next);
  }
  
  public static class CreateOneProcException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final GrimBatchMissions batch;
    public CreateOneProcException(String message, GrimBatchMissions batch) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", batch.getMessages().stream().map(e -> e.getText()).toList()));
      
      batch.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
      this.batch = batch;
    }
    public GrimBatchMissions getBatch() {
      return batch;
    }
  }
}
