package io.resys.thena.ledger.client.spi.create;

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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.ledger.client.api.ImmutableLedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBook;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewLedgerEvent;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewMoneyRequest;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewProjection;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewSettlement;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewUnitPrice;
import io.resys.thena.ledger.client.entities.ImmutableLedger;
import io.resys.thena.ledger.client.entities.ImmutableLedgerTransitives;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;



public class NewLedgerBuilder implements ThenaLedgerNewObject.NewLedger {
  private final LedgerCommitBuilder logger;
  private final ImmutableLedger.Builder ledger;
  private final String ledgerId;
  private final String commitId;
  
  private ImmutablePersistenceUnit.Builder next;
  private Consumer<LedgerContainer> handleNewState;
  private boolean built;
  
  public NewLedgerBuilder(LedgerCommitBuilder logger) {
    super();
    this.next = logger.createPersistenceUnit();
    this.commitId = logger.getCommitId();
    this.ledgerId = OidUtils.genUUID();
    this.ledger = ImmutableLedger.builder()
        .id(ledgerId)
        .createdCommitId(commitId)
        .commitId(commitId)
        .updatedTreeCommitId(commitId)
        .description(Optional.empty());
        
    this.logger = logger;
  }
  
  @Override
  public NewLedger externalId(String externalId) {
    this.ledger.externalId(externalId);
    return this;
  }

  @Override
  public NewLedger name(String name) {
    this.ledger.name(name);
    return this;
  }

  @Override
  public NewLedger description(@Nullable String description) {
    this.ledger.description(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewLedger addMoneyRequest(Consumer<NewMoneyRequest> moneyRequest) {
    final var allMoneyRequests = this.next.build();
    final var builder = new NewMoneyRequestBuilder(logger, ledgerId, allMoneyRequests, null);
    moneyRequest.accept(builder);
    final var built = builder.close();
    this.next.addMoneyRequestInserts(built);
    return this;
  }

  @Override
  public NewLedger addPayment(Consumer<NewPayment> payment) {
    final var allPayments = this.next.build();
    final var builder = new NewPaymentBuilder(logger, ledgerId, allPayments, null);
    payment.accept(builder);
    final var built = builder.close();
    this.next.addPaymentInserts(built);
    return this;
  }

  @Override
  public NewLedger addSettlement(Consumer<NewSettlement> settlement) {
    final var allSettlements = this.next.build();
    final var builder = new NewSettlementBuilder(logger, ledgerId, allSettlements, null);
    settlement.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }

  @Override
  public NewLedger addBlackBook(Consumer<NewBlackBook> blackBook) {
    final var allBlackBooks = this.next.build();
    final var builder = new NewBlackBookBuilder(logger, ledgerId, allBlackBooks, null);
    blackBook.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }

  @Override
  public NewLedger addProjection(Consumer<NewProjection> projection) {
    final var allProjections = this.next.build();
    final var builder = new NewProjectionBuilder(logger, ledgerId, allProjections, null);
    projection.accept(builder);
    final var built = builder.close();
    this.next.from(built);
    return this;
  }

  @Override
  public NewLedger addUnitPrice(Consumer<NewUnitPrice> unitPrice) {
    final var allUnitPrices = this.next.build();
    final var builder = new NewUnitPriceBuilder(logger, allUnitPrices, null);
    unitPrice.accept(builder);
    final var built = builder.close();
    this.next.addUnitPriceInserts(built);
    return this;
  }

  @Override
  public NewLedger addLedgerEvent(Consumer<NewLedgerEvent> ledgerEvent) {
    final var allLedgerEvents = this.next.build();
    final var builder = new NewLedgerEventBuilder(logger, ledgerId, allLedgerEvents, null);
    ledgerEvent.accept(builder);
    final var built = builder.close();
    this.next.addLedgerEventInserts(built);
    return this;
  }

  @Override
  public NewLedger onNewState(Consumer<LedgerContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public PersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call NewLedger.build() to finalize ledger CREATE!");

    final var ledger = this.ledger
        .transitives(ImmutableLedgerTransitives.builder()
            .createdAt(logger.getCreatedAt())
            .updatedAt(logger.getCreatedAt())
            .updatedTreeAt(logger.getCreatedAt())
           .build())
        .build();
    
    logger.add(ledger);
    
    next.addLedgerInserts(ledger);
    final var batch = next.build();
    
    onNewState(batch);
    
    return batch;
  }
  
  private void onNewState(PersistenceUnit batch) {
    if(handleNewState == null) {
      return;
    }
    final var ledger = batch.getLedgerInserts().iterator().next();
    final var container = ImmutableLedgerContainer.builder()
        .ledger(ledger)
        .moneyRequests(batch.getMoneyRequestInserts())
        .payments(batch.getPaymentInserts())
        .settlements(batch.getSettlementInserts())
        .blackBooks(batch.getBlackBookInserts())
        .projections(batch.getProjectionInserts())
        .unitPrices(batch.getUnitPriceInserts())
        .ledgerEvents(batch.getLedgerEventInserts())
        .settlementPayments(batch.getSettlementPaymentInserts().stream()
            .collect(Collectors.groupingBy(sp -> sp.getSettlementId())))
        .blackBookDetails(batch.getBlackBookDetailInserts().stream()
            .collect(Collectors.groupingBy(bbd -> bbd.getBlackBookId())))
        .projectionDetails(batch.getProjectionDetailInserts().stream()
            .collect(Collectors.groupingBy(pd -> pd.getProjectionId())))
        .build();
    
    handleNewState.accept(container);
  }
}
