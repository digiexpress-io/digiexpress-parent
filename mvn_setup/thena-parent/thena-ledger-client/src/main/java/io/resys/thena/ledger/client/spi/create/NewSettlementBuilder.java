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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Objects;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewSettlement;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewSettlementPayment;
import io.resys.thena.ledger.client.entities.ImmutableSettlement;
import io.resys.thena.ledger.client.entities.Settlement;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewSettlementBuilder implements NewSettlement {
  private final LedgerCommitBuilder logger;
  private final String settlementId;
  private final Map<String, Settlement> allSettlements;
  private final ImmutableSettlement.Builder next;
  private final LedgerContainer savedState;
  
  private boolean built;
  private ImmutablePersistenceUnit.Builder batch;
  
  public NewSettlementBuilder(
      LedgerCommitBuilder logger, 
      String ledgerId,
      PersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.settlementId = OidUtils.genUUID();
    this.savedState = savedState;
    this.next = ImmutableSettlement.builder()
        .id(settlementId)
        .ledgerId(ledgerId)
        .createdCommitId(logger.getCommitId())
        .settlementSubType(Optional.empty())
        .settlementDescription(Optional.empty());
    
    // Settlements are immutable, so no updates/deletes to consider
    this.allSettlements = Stream.of(
        // from current TX
        currentTx.getSettlementInserts().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getSettlements())
          .orElse(Collections.emptyList())
          .stream()
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewSettlement externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewSettlement type(String type) {
    this.next.settlementType(type);
    return this;
  }

  @Override
  public NewSettlement subType(@Nullable String subType) {
    this.next.settlementSubType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewSettlement description(@Nullable String description) {
    this.next.settlementDescription(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewSettlement date(LocalDate date) {
    this.next.settlementDate(date);
    return this;
  }

  @Override
  public NewSettlement amount(BigDecimal amount) {
    this.next.settlementAmount(amount);
    return this;
  }

  @Override
  public NewSettlement addSettlementPayment(Consumer<NewSettlementPayment> settlementPayment) {
    final var allDetails = this.batch.build();
    final var builder = new NewSettlementPaymentBuilder(logger, settlementId, allDetails, savedState);
    settlementPayment.accept(builder);
    final var built = builder.close();
    this.batch.addSettlementPaymentInserts(built);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call NewSettlement.build() to finalize settlement CREATE!");

    final var settlement = next.build();
    
    
    // Validate uniqueness - no duplicates
    RepoAssert.isTrue(
        this.allSettlements.values().stream()
        .filter(a -> (
            a.getSettlementDate().equals(settlement.getSettlementDate())
            && a.getSettlementType().equals(settlement.getSettlementType())
            && Objects.equal(a.getExternalId(), settlement.getExternalId())
        ))
        .count() == 0
        , () -> "can't have duplicate black book  with same type/date/externalid for same black book!");

    this.logger.add(settlement);
    return batch.addSettlementInserts(settlement).build();
  }
}