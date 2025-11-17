package io.resys.thena.ledger.client.spi.create;

/*-
 * #%L
 * thena-ledger-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.resys.thena.ledger.client.entities.ImmutablePayment;
import io.resys.thena.ledger.client.entities.Payment;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewPaymentBuilder implements NewPayment {
  private final LedgerCommitBuilder logger;
  private final Map<String, Payment> allPayments;
  private final ImmutablePayment.Builder next;
  private boolean built;
  
  public NewPaymentBuilder(
      LedgerCommitBuilder logger, 
      String ledgerId,
      PersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.next = ImmutablePayment.builder()
        .id(OidUtils.genUUID())
        .ledgerId(ledgerId)
        .createdCommitId(logger.getCommitId())
        .subType(Optional.empty())
        .description(Optional.empty());
    
    // Payments are immutable, so no updates/deletes to consider
    this.allPayments = Stream.of(
        // from current TX
        currentTx.getPaymentInserts().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getPayments())
          .orElse(Collections.emptyList())
          .stream()
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewPayment externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewPayment type(String type) {
    this.next.type(type);
    return this;
  }

  @Override
  public NewPayment subType(@Nullable String subType) {
    this.next.subType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewPayment description(@Nullable String description) {
    this.next.description(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewPayment date(LocalDate date) {
    this.next.date(date);
    return this;
  }

  @Override
  public NewPayment amount(BigDecimal amount) {
    this.next.amount(amount);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public Payment close() {
    RepoAssert.isTrue(built, () -> "you must call NewPayment.build() to finalize payment CREATE!");
    final var built = next.build();
    
    // Validate uniqueness - no duplicate capabilities with same code
    RepoAssert.isTrue(
        this.allPayments.values().stream()
        .filter(c -> c.getExternalId().equals(built.getExternalId()))
        .count() == 0
        , () -> "can't have duplicate external id-s!");

    this.logger.add(built);
    return built;
  }
}