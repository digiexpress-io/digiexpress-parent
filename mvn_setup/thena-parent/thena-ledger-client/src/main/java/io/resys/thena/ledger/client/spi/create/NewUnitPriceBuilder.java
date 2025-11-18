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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewUnitPrice;
import io.resys.thena.ledger.client.entities.ImmutableUnitPrice;
import io.resys.thena.ledger.client.entities.UnitPrice;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewUnitPriceBuilder implements NewUnitPrice {
  private final LedgerCommitBuilder logger;
  private final Map<String, UnitPrice> allUnitPrices;
  private final ImmutableUnitPrice.Builder next;
  private boolean built;
  
  public NewUnitPriceBuilder(
      LedgerCommitBuilder logger, 
      PersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.next = ImmutableUnitPrice.builder()
        .id(OidUtils.genUUID())
        .createdCommitId(logger.getCommitId());
    
    // UnitPrices are immutable, so no updates/deletes to consider
    this.allUnitPrices = Stream.of(
        // from current TX
        currentTx.getUnitPriceInserts().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getUnitPrices().stream())
          .orElse(Stream.empty())
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewUnitPrice externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewUnitPrice type(String type) {
    this.next.unitType(type);
    return this;
  }

  @Override
  public NewUnitPrice subType(String subType) {
    this.next.unitSubType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewUnitPrice description(String description) {
    this.next.unitDescription(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewUnitPrice date(LocalDate date) {
    this.next.unitDate(date);
    return this;
  }

  @Override
  public NewUnitPrice value(BigDecimal value) {
    this.next.unitValue(value);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public UnitPrice close() {
    RepoAssert.isTrue(built, () -> "you must call NewUnitPrice.build() to finalize unit price CREATE!");
    final var built = next.build();
    this.logger.add(built);
    return built;
  }
}