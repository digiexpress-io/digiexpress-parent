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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewSettlementPayment;
import io.resys.thena.ledger.client.entities.ImmutableSettlementPayment;
import io.resys.thena.ledger.client.entities.SettlementPayment;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewSettlementPaymentBuilder implements NewSettlementPayment {
  private final LedgerCommitBuilder logger;

  private final Map<String, SettlementPayment> allSettlementPayments;
  private final ImmutableSettlementPayment.Builder next;

  private boolean built;

  
  public NewSettlementPaymentBuilder(
      LedgerCommitBuilder logger, 
      String settlementId,
      PersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.next = ImmutableSettlementPayment.builder()
        .id(OidUtils.genUUID())
        .settlementId(settlementId)
        .createdCommitId(logger.getCommitId());
    
    // SettlementPayments are immutable, so no updates/deletes to consider
    this.allSettlementPayments = Stream.of(
        // from current TX
        currentTx.getSettlementPaymentInserts().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getSettlementPayments().get(settlementId))
          .map(saved -> saved.stream())
          .orElse(Stream.empty())
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewSettlementPayment paymentId(String paymentId) {
    this.next.paymentId(paymentId);
    return this;
  }

  @Override
  public NewSettlementPayment allocationAmount(BigDecimal allocationAmount) {
    this.next.allocationAmount(allocationAmount);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public SettlementPayment close() {
    RepoAssert.isTrue(built, () -> "you must call NewSettlementPayment.build() to finalize settlement payment CREATE!");
    final var result = this.next.build();
    final var built = next.build();
    this.logger.add(built);
    return result;
  }
}