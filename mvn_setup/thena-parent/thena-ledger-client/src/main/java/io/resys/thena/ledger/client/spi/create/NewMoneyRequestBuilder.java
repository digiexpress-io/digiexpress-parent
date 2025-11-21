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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewMoneyRequest;
import io.resys.thena.ledger.client.entities.ImmutableMoneyRequest;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestType;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewMoneyRequestBuilder implements NewMoneyRequest {
  private final LedgerCommitBuilder logger;
  
  private final Map<String, MoneyRequest> allMoneyRequests;
  private final ImmutableMoneyRequest.Builder next;
  
  private boolean built;
  
  public NewMoneyRequestBuilder(
      LedgerCommitBuilder logger, 
      String ledgerId,
      PersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.next = ImmutableMoneyRequest.builder()
        .id(OidUtils.genUUID())
        .ledgerId(ledgerId)
        .createdCommitId(logger.getCommitId())
        .commitId(logger.getCommitId())
        .paymentId(Optional.empty())
        .requestSubType(Optional.empty())
        .requestDescription(Optional.empty());
    
    final var updates = currentTx.getMoneyRequestUpdates().stream().map(e -> e.getId()).toList();
    
    this.allMoneyRequests = Stream.of(
        // from current TX
        currentTx.getMoneyRequestInserts().stream(),
        currentTx.getMoneyRequestUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getMoneyRequests())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewMoneyRequest externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewMoneyRequest paymentId(@Nullable String paymentId) {
    this.next.paymentId(Optional.ofNullable(paymentId));
    return this;
  }

  @Override
  public NewMoneyRequest type(MoneyRequestType type) {
    this.next.requestType(type);
    return this;
  }

  @Override
  public NewMoneyRequest subType(@Nullable String subType) {
    this.next.requestSubType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewMoneyRequest status(MoneyRequestStatus status) {
    this.next.requestStatus(status);
    return this;
  }


  @Override
  public NewMoneyRequest description(@Nullable String description) {
    this.next.requestDescription(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewMoneyRequest targetDate(LocalDate targetDate) {
    this.next.requestTargetDate(targetDate);
    return this;
  }

  @Override
  public NewMoneyRequest amount(BigDecimal amount) {
    this.next.requestAmount(amount);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public MoneyRequest close() {
    RepoAssert.isTrue(built, () -> "you must call NewMoneyRequest.build() to finalize money request CREATE!");

    final var built = this.next.build();
    this.logger.add(built);
    return built;
  }
}
