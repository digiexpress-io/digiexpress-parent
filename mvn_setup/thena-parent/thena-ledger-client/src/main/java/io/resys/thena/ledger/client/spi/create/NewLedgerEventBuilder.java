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

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewLedgerEvent;
import io.resys.thena.ledger.client.entities.ImmutableLedgerEvent;
import io.resys.thena.ledger.client.entities.LedgerEvent;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewLedgerEventBuilder implements NewLedgerEvent {
  private final LedgerCommitBuilder logger;
  private final Map<String, LedgerEvent> allLedgerEvents;
  private final ImmutableLedgerEvent.Builder next;

  private boolean built;

  
  public NewLedgerEventBuilder(
      LedgerCommitBuilder logger, 
      String ledgerId,
      PersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.next = ImmutableLedgerEvent.builder()
        .id(OidUtils.genUUID())
        .ledgerId(ledgerId)
        .createdCommitId(logger.getCommitId());
    
    // LedgerEvents are immutable, so no updates/deletes to consider
    this.allLedgerEvents = Stream.of(
        // from current TX
        currentTx.getLedgerEventInserts().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getLedgerEvents().stream())
          .orElse(Stream.empty())
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewLedgerEvent externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewLedgerEvent type(String type) {
    this.next.type(type);
    return this;
  }

  @Override
  public NewLedgerEvent subType(String subType) {
    this.next.subType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewLedgerEvent description(String description) {
    this.next.description(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewLedgerEvent date(LocalDate date) {
    this.next.date(date);
    return this;
  }

  @Override
  public NewLedgerEvent body(JsonObject body) {
    this.next.body(Optional.ofNullable(body));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public LedgerEvent close() {
    RepoAssert.isTrue(built, () -> "you must call NewLedgerEvent.build() to finalize ledger event CREATE!");

    final var built = this.next.build();
    this.logger.add(built);
    return built;
  }
}