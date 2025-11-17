package io.resys.thena.ledger.client.spi.modify;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeMoneyRequest;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBook;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewLedgerEvent;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewMoneyRequest;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewProjection;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewSettlement;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewUnitPrice;
import io.resys.thena.ledger.client.entities.ImmutableLedger;
import io.resys.thena.ledger.client.entities.ImmutableLedgerTransitives;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.spi.create.NewBlackBookBuilder;
import io.resys.thena.ledger.client.spi.create.NewLedgerEventBuilder;
import io.resys.thena.ledger.client.spi.create.NewMoneyRequestBuilder;
import io.resys.thena.ledger.client.spi.create.NewPaymentBuilder;
import io.resys.thena.ledger.client.spi.create.NewProjectionBuilder;
import io.resys.thena.ledger.client.spi.create.NewSettlementBuilder;
import io.resys.thena.ledger.client.spi.create.NewUnitPriceBuilder;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;

public class MergeLedgerBuilder implements MergeLedger {
  private final LedgerContainer container;
  private final LedgerCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final ImmutableLedger.Builder nextLedger;
  private final ImmutableLedgerTransitives.Builder nextTransitives;
  private final String ledgerId;
  private Consumer<LedgerContainer> handleCurrentState;
  private boolean built;
  
  public MergeLedgerBuilder(LedgerContainer container, LedgerCommitBuilder logger) {
    super();

    final var start = container.getLedger();
    this.nextTransitives = ImmutableLedgerTransitives.builder()
        .from(start.getTransitives());
    
    this.container = container;
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.nextLedger = ImmutableLedger.builder().from(start);
    this.ledgerId = container.getLedger().getId();
  }
  
  @Override
  public MergeLedger onCurrentState(Consumer<LedgerContainer> handleCurrentState) {
    this.handleCurrentState = handleCurrentState;
    return this;
  }
  
  @Override
  public LedgerContainer getCurrentState() {
    return container;
  }
  
  @Override
  public MergeLedger externalId(String externalId) {
    this.nextLedger.externalId(externalId);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeLedger name(String name) {
    this.nextLedger.name(name);
    updateVersion();
    return this;
  }
  
  @Override
  public MergeLedger description(String description) {
    this.nextLedger.description(Optional.ofNullable(description));
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger addMoneyRequest(Consumer<NewMoneyRequest> moneyRequest) {
    final var builder = new NewMoneyRequestBuilder(logger, ledgerId, this.batch.build(), container);
    moneyRequest.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    this.batch.addMoneyRequestInserts(built);
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger addPayment(Consumer<NewPayment> payment) {
    final var builder = new NewPaymentBuilder(logger, ledgerId, this.batch.build(), container);
    payment.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    this.batch.addPaymentInserts(built);
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger addSettlement(Consumer<NewSettlement> settlement) {
    final var builder = new NewSettlementBuilder(logger, ledgerId, this.batch.build(), container);
    settlement.accept(builder);
    final var builtData = builder.close();
    this.batch.from(builtData);
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger addBlackBook(Consumer<NewBlackBook> blackBook) {
    final var builder = new NewBlackBookBuilder(logger, ledgerId, this.batch.build(), container);
    blackBook.accept(builder);
    final var builtData = builder.close();
    this.batch.from(builtData);
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger addProjection(Consumer<NewProjection> projection) {
    final var builder = new NewProjectionBuilder(logger, ledgerId, this.batch.build(), container);
    projection.accept(builder);
    final var builtData = builder.close();
    this.batch.from(builtData);
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger addUnitPrice(Consumer<NewUnitPrice> unitPrice) {
    final var builder = new NewUnitPriceBuilder(logger, this.batch.build(), container);
    unitPrice.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    this.batch.addUnitPriceInserts(built);
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger addLedgerEvent(Consumer<NewLedgerEvent> ledgerEvent) {
    final var builder = new NewLedgerEventBuilder(logger, ledgerId, this.batch.build(), container);
    ledgerEvent.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    this.batch.addLedgerEventInserts(built);
    updateVersion();
    return this;
  }

  @Override
  public MergeLedger modifyMoneyRequest(String moneyRequestId, Consumer<MergeMoneyRequest> moneyRequest) {
    final var builder = new MergeMoneyRequestBuilder(container, logger, ledgerId, moneyRequestId, this.batch.build(), container);
    moneyRequest.accept(builder);
    final var builtData = builder.close();
    this.batch.from(builtData);
    updateVersion();
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeLedger.build() to finalize ledger MERGE!");

    if(handleCurrentState != null) {
      handleCurrentState.accept(container);
    }

    final var ledger = nextLedger
        .updatedCommit(logger.getCommitId())
        .transitives(nextTransitives.updatedAt(logger.getCreatedAt()).build())
        .build();
    
    this.logger.add(ledger);
    return batch.addLedgerUpdates(ledger).build();
  }

  private void updateVersion() {
    this.nextTransitives.updatedAt(logger.getCreatedAt());
  }
}