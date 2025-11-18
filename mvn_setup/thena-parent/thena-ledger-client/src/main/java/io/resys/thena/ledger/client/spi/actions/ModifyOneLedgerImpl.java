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
import java.util.function.Consumer;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.ledger.client.api.ImmutableLedgerContainer;
import io.resys.thena.ledger.client.api.ImmutableOneLedgerEnvelope;
import io.resys.thena.ledger.client.api.LedgerCommitActions.ModifyOneLedger;
import io.resys.thena.ledger.client.api.LedgerCommitActions.OneLedgerEnvelope;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeLedger;
import io.resys.thena.ledger.client.entities.ImmutableCommit;
import io.resys.thena.ledger.client.entities.LedgerDocType;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.spi.modify.MergeLedgerBuilder;
import io.resys.thena.ledger.client.spi.queries.LedgerQueryImpl;
import io.resys.thena.ledger.client.tables.BbDb;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneLedgerImpl implements ModifyOneLedger {

  private final BbDb state;
  private final String tenantId;
  
  private String author;
  private String message;
  private String ledgerId;
  private Consumer<MergeLedger> modifyLedger;
  
  @Override
  public ModifyOneLedger commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  
  @Override
  public ModifyOneLedger commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  
  @Override
  public ModifyOneLedger LedgerId(String ledgerId) {
    this.ledgerId = RepoAssert.notEmpty(ledgerId, () -> "ledgerId can't be empty!");
    return this;
  }
  
  @Override
  public ModifyOneLedger modifyLedger(Consumer<MergeLedger> modifyLedger) {
    RepoAssert.notNull(modifyLedger, () -> "modifyLedger can't be empty!");
    this.modifyLedger = modifyLedger;
    return this;
  }

  @Override
  public Uni<OneLedgerEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(ledgerId, () -> "ledgerId can't be empty!");
    RepoAssert.notNull(modifyLedger, () -> "modifyLedger can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withTransaction(scope, this::doInTx);
  }

  private Uni<OneLedgerEnvelope> doInTx(BbDb tx) {
    return createRequest(tx)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyOneLedgerException.class).recoverWithItem(ex -> {
          final ModifyOneLedgerException error = (ModifyOneLedgerException) ex;          
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
        throw new ModifyOneLedgerException("Failed to modify ledger!", rsp);
      }
      
      final OneLedgerEnvelope result = ImmutableOneLedgerEnvelope.builder()
          .repoId(tenantId)
          .ledger(ImmutableLedgerContainer.builder()
            .ledger(rsp.getLedgerUpdates().isEmpty() ? null : rsp.getLedgerUpdates().iterator().next())
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
    });
  }
  
  private Uni<PersistenceUnit> createRequest(BbDb tx) {
    return LedgerQueryImpl.of(tx)
      .addLedgerId(this.ledgerId)
      .lockForUpdate()
      .excludeDocs(LedgerDocType.COMMIT)
      .findAll().onItem()
      .transform(container -> createRequest(tx, container));
  }
  
  private PersistenceUnit createRequest(BbDb tx, QueryEnvelopeList<LedgerContainer> env) {
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    ImmutablePersistenceUnit next = start;

    final var logger = new LedgerCommitBuilder(tenantId, 
        ImmutableCommit.builder()
          .id(OidUtils.genUUID())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .build()
    );
    
    final var mergeLedger = new MergeLedgerBuilder(env.getObjects().get(0), logger);
    this.modifyLedger.accept(mergeLedger);
    final var modified = mergeLedger.close();
    
    next = ImmutablePersistenceUnit.builder()
        .from(start)
        .from(modified)
        .from(logger.withLedgerId(ledgerId).close())
        .build();
  
    return next;
  }
  

  
  public static class ModifyOneLedgerException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final PersistenceUnit batch;
    
    public ModifyOneLedgerException(String message, PersistenceUnit batch) {
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
