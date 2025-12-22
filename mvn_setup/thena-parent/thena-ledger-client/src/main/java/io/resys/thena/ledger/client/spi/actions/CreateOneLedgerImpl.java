package io.resys.thena.ledger.client.spi.actions;

/*-
 * #%L
 * thena-ledger-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.ledger.client.api.LedgerCommitActions.CreateOneLedger;
import io.resys.thena.ledger.client.api.LedgerCommitActions.OneLedgerEnvelope;
import io.resys.thena.ledger.client.api.ImmutableLedgerContainer;
import io.resys.thena.ledger.client.api.ImmutableOneLedgerEnvelope;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewLedger;
import io.resys.thena.ledger.client.entities.ImmutableCommit;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.spi.create.NewLedgerBuilder;
import io.resys.thena.ledger.client.tables.BbDb;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneLedgerImpl implements CreateOneLedger {

  private final BbDb state;
  private final String tenantId;
  
  private String author;
  private String message;
  private Consumer<NewLedger> ledger;
  private Consumer<LedgerContainer> handleNewState;
  
  @Override
  public CreateOneLedger commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  
  @Override
  public CreateOneLedger commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  
  @Override
  public CreateOneLedger ledger(Consumer<NewLedger> addLedger) {
    RepoAssert.notNull(addLedger, () -> "addLedger can't be empty!");
    ledger = addLedger;
    return this;
  }

  @Override
  public Uni<OneLedgerEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(ledger, () -> "ledger can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withTransaction(scope, this::doInTx);
  }
  
  @Override
  public CreateOneLedger onNewLedger(Consumer<LedgerContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }

  private Uni<OneLedgerEnvelope> doInTx(BbDb tx) {
    return createRequest(tx)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(CreateOneLedgerException.class).recoverWithItem(ex -> {
          final CreateOneLedgerException error = (CreateOneLedgerException) ex;          
          return ImmutableOneLedgerEnvelope.builder()
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
  
  private Uni<OneLedgerEnvelope> createResponse(BbDb tx, PersistenceUnit request) {
    return tx.builder().from(request).persist().onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneLedgerException("Failed to create ledger!", rsp);
      }
      
      final OneLedgerEnvelope result = ImmutableOneLedgerEnvelope.builder()
          .repoId(tenantId)
          .ledger(ImmutableLedgerContainer.builder()
            .ledger(rsp.getLedgerInserts().iterator().next())
            .moneyRequests(rsp.getMoneyRequestInserts())
            .payments(rsp.getPaymentInserts())
            .settlements(rsp.getSettlementInserts())
            .settlementPayments(java.util.Collections.emptyMap())
            .blackBooks(rsp.getBlackBookInserts())
            .blackBookDetails(java.util.Collections.emptyMap())
            .projections(rsp.getProjectionInserts())
            .projectionDetails(java.util.Collections.emptyMap())
            .unitPrices(rsp.getUnitPriceInserts())
            .ledgerEvents(rsp.getLedgerEventInserts())
            .build())
          .addAllMessages(rsp.getCommitLogs().stream().map(log -> ImmutableMessage.builder()
              .exception(log.getException())
              .text(log.getText())
              .build()).toList())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    })
    .onItem().invoke(newState -> {
      if(handleNewState != null) {
        handleNewState.accept(newState.getLedger());
      }
    });
  }
  
  private Uni<ImmutablePersistenceUnit> createRequest(BbDb tx) {
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    ImmutablePersistenceUnit next = start;

    final var ledgerId = OidUtils.genUUID();
    final var logger = new LedgerCommitBuilder(tenantId, 
        ImmutableCommit.builder()
          .id(OidUtils.genUUID())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .ledgerId(ledgerId)
          .build(),
        Optional.empty()
    );
    
    final var newLedger = new NewLedgerBuilder(logger);
    this.ledger.accept(newLedger);
    final var created = newLedger.close();
    
    
    next = ImmutablePersistenceUnit.builder()
        .from(start)
        .from(created)
        .from(logger.close())
        .build();
  
    return Uni.createFrom().item(next);
  }
  
  public static class CreateOneLedgerException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final PersistenceUnit batch;
    
    public CreateOneLedgerException(String message, PersistenceUnit batch) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", batch.getCommitLogs().stream().map(e -> e.getText()).toList()));
      
      batch.getCommitLogs().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
      this.batch = batch;
    }
    
    public PersistenceUnit getBatch() {
      return batch;
    }
  }
}