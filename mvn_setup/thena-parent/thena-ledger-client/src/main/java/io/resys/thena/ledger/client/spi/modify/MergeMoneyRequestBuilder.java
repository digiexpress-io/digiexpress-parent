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

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeMoneyRequest;
import io.resys.thena.ledger.client.entities.ImmutableMoneyRequest;
import io.resys.thena.ledger.client.entities.ImmutableMoneyRequestTransitives;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestType;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeMoneyRequestBuilder implements MergeMoneyRequest {

  private final LedgerCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final MoneyRequest currentMoneyRequest; 
  private final ImmutableMoneyRequest.Builder nextMoneyRequest;
  private boolean built;

  public MergeMoneyRequestBuilder(
      LedgerContainer container, 
      LedgerCommitBuilder logger, 
      String ledgerId, 
      String moneyRequestId,
      ImmutablePersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    super();
    this.logger = logger;
    this.batch = logger.createPersistenceUnit();
    
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
  }

  @Override
  public MergeMoneyRequest externalId(String externalId) {
    this.nextMoneyRequest.externalId(externalId);
    return this;
  }

  @Override
  public MergeMoneyRequest paymentId(@Nullable String paymentId) {
    this.nextMoneyRequest.paymentId(Optional.ofNullable(paymentId));
    return this;
  }

  @Override
  public MergeMoneyRequest type(MoneyRequestType type) {
    this.nextMoneyRequest.requestType(type);
    return this;
  }

  @Override
  public MergeMoneyRequest subType(String subType) {
    this.nextMoneyRequest.requestSubType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public MergeMoneyRequest status(MoneyRequestStatus status) {
    this.nextMoneyRequest.requestStatus(status);
    return this;
  }


  @Override
  public MergeMoneyRequest description(String description) {
    this.nextMoneyRequest.requestDescription(Optional.ofNullable(description));
    return this;
  }

  @Override
  public MergeMoneyRequest targetDate(LocalDate targetDate) {
    this.nextMoneyRequest.requestTargetDate(targetDate);
    return this;
  }

  @Override
  public MergeMoneyRequest amount(BigDecimal amount) {
    this.nextMoneyRequest.requestAmount(amount);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeMoneyRequest.build() to finalize money request MERGE!");

    final var moneyRequest = nextMoneyRequest
        .commitId(logger.getCommitId())
        .transitives(ImmutableMoneyRequestTransitives.builder()
            .from(currentMoneyRequest.getTransitives())
            .updatedAt(logger.getCreatedAt())
            .build())
        .build();
    
    this.logger.add(moneyRequest);
    return batch.addMoneyRequestUpdates(moneyRequest).build();
  }
}