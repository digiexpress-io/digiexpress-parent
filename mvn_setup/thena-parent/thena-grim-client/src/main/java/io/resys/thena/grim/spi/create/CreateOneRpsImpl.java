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
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRps;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.grim.api.GrimCommitActions.CreateOneRps;
import io.resys.thena.grim.api.GrimCommitActions.OneRpsEnvelope;
import io.resys.thena.grim.api.ImmutableOneRpsEnvelope;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchMissions;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.grim.spi.commitlog.GrimCommitBuilder;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneRpsImpl implements CreateOneRps {

  private final GrimDataSource state;
  private final String tenantId;
  
  private String author;
  private String message;
  private Consumer<NewRps> newRps;
  
  @Override
  public CreateOneRps commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public CreateOneRps commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public CreateOneRps rps(Consumer<NewRps> newRps) {
    RepoAssert.notNull(newRps, () -> "newRps can't be empty!");
    this.newRps = newRps;
    return this;
  }

  @Override
  public Uni<OneRpsEnvelope> build() {
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }
  
  private Uni<OneRpsEnvelope> doInTx(GrimState tx) {
    return createResponse(tx, createRequest(tx))
        .onFailure(CreateOneRpsException.class).recoverWithItem(ex -> {
          final CreateOneRpsException error = (CreateOneRpsException) ex;          
          return ImmutableOneRpsEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'").append(" is rejected for RPS.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(error.getMessage())
                  .toString())
                .exception(error)
                .build())
            .status(CommitResultStatus.ERROR)
          .build();
        });
  }
  
  private Uni<OneRpsEnvelope> createResponse(GrimState tx, GrimBatchMissions request) {
    return tx.batchMany(request).onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneRpsException("Failed to create RPS!", rsp);
      }
      
      final OneRpsEnvelope result = ImmutableOneRpsEnvelope.builder()
          .repoId(tenantId)
          .rps(rsp.getRps().iterator().next())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    });
  }
  
  private GrimBatchMissions createRequest(GrimState tx) {
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
    
    final var newRpsBuilder = new NewRpsBuilder(logger);
    this.newRps.accept(newRpsBuilder);
    final var created = newRpsBuilder.close();
    
    next = ImmutableGrimBatchMissions.builder()
        .from(start)
        .from(created)
        .from(logger.close())
        .build();
  
    return next;
  }
  
  public static class CreateOneRpsException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final GrimBatchMissions batch;
    public CreateOneRpsException(String message, GrimBatchMissions batch) {
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
