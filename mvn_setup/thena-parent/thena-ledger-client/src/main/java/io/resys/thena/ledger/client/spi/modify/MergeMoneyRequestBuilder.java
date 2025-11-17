package io.resys.thena.ledger.client.spi.modify;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeMoneyRequest;
import io.resys.thena.ledger.client.entities.ImmutableMoneyRequest;
import io.resys.thena.ledger.client.entities.ImmutableMoneyRequestTransitives;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestFrequency;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeMoneyRequestBuilder implements MergeMoneyRequest {

  private final LedgerCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final MoneyRequest currentMoneyRequest; 
  private final ImmutableMoneyRequest.Builder nextMoneyRequest;
  private final ImmutableMoneyRequestTransitives.Builder nextTransitives;
  private final LedgerContainer container;
  private final String ledgerId;
  private boolean built;

  public MergeMoneyRequestBuilder(
      LedgerContainer container, 
      LedgerCommitBuilder logger, 
      String ledgerId, 
      String moneyRequestId,
      ImmutablePersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    super();
    this.ledgerId = ledgerId;
    this.logger = logger;
    this.container = container;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    
    // Find the money request to modify - first check current transaction
    MoneyRequest fromTx = currentTx.getMoneyRequestInserts().stream()
        .filter(mr -> mr.getId().equals(moneyRequestId))
        .findFirst()
        .orElse(null);
    
    if (fromTx != null) {
      this.currentMoneyRequest = fromTx;
    } else {
      // Find in container
      this.currentMoneyRequest = container.getMoneyRequests().stream()
          .filter(mr -> mr.getId().equals(moneyRequestId))
          .findFirst()
          .orElse(null);
    }
    
    RepoAssert.notNull(currentMoneyRequest, () -> "Can't find money request with id: '" + moneyRequestId + "' for ledger: '" + ledgerId + "'!");
    
    this.nextMoneyRequest = ImmutableMoneyRequest.builder().from(currentMoneyRequest);
    this.nextTransitives = ImmutableMoneyRequestTransitives.builder()
        .from(currentMoneyRequest.getTransitives());
  }

  @Override
  public MergeMoneyRequest externalId(String externalId) {
    this.nextMoneyRequest.externalId(externalId);
    updateVersion();
    return this;
  }

  @Override
  public MergeMoneyRequest type(String type) {
    this.nextMoneyRequest.type(type);
    updateVersion();
    return this;
  }

  @Override
  public MergeMoneyRequest subType(String subType) {
    this.nextMoneyRequest.subType(Optional.ofNullable(subType));
    updateVersion();
    return this;
  }

  @Override
  public MergeMoneyRequest status(MoneyRequestStatus status) {
    this.nextMoneyRequest.status(status);
    updateVersion();
    return this;
  }

  @Override
  public MergeMoneyRequest frequency(MoneyRequestFrequency frequency) {
    this.nextMoneyRequest.frequency(frequency);
    updateVersion();
    return this;
  }

  @Override
  public MergeMoneyRequest description(String description) {
    this.nextMoneyRequest.description(Optional.ofNullable(description));
    updateVersion();
    return this;
  }

  @Override
  public MergeMoneyRequest dueDate(LocalDate dueDate) {
    this.nextMoneyRequest.dueDate(dueDate);
    updateVersion();
    return this;
  }

  @Override
  public MergeMoneyRequest amount(BigDecimal amount) {
    this.nextMoneyRequest.amount(amount);
    updateVersion();
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeMoneyRequest.build() to finalize money request MERGE!");

    final var moneyRequest = nextMoneyRequest
        .updatedCommit(logger.getCommitId())
        .transitives(nextTransitives.updatedAt(logger.getCommitAt()).build())
        .build();
    
    this.logger.add(moneyRequest);
    return batch.addMoneyRequestUpdates(moneyRequest).build();
  }

  private void updateVersion() {
    this.nextTransitives.updatedAt(logger.getCommitAt());
  }
}