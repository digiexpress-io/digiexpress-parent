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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Objects;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBook;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBookDetail;
import io.resys.thena.ledger.client.entities.BlackBook;
import io.resys.thena.ledger.client.entities.ImmutableBlackBook;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;


public class NewBlackBookBuilder implements NewBlackBook {
  private final LedgerCommitBuilder logger;
  private final String blackBookId;
  private final Map<String, BlackBook> allBlackBooks;
  private final ImmutableBlackBook.Builder next;
  private final LedgerContainer savedState;
  private boolean built;
  private ImmutablePersistenceUnit.Builder batch;
  
  
  public NewBlackBookBuilder(
      LedgerCommitBuilder logger, 
      String ledgerId,
      ImmutablePersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.savedState = savedState;
    this.blackBookId = OidUtils.genUUID();
    this.next = ImmutableBlackBook.builder()
        .id(this.blackBookId)
        .ledgerId(ledgerId)
        .createdCommitId(logger.getCommitId());
    
    this.allBlackBooks = Stream.of(
        // from current TX
        currentTx.getBlackBookInserts().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getBlackBooks())
          .orElse(Collections.emptyList())
          .stream()
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewBlackBook externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewBlackBook type(String type) {
    this.next.type(type);
    return this;
  }

  @Override
  public NewBlackBook subType(String subType) {
    this.next.subType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewBlackBook description(String description) {
    this.next.description(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewBlackBook date(LocalDate date) {
    this.next.date(date);
    return this;
  }

  @Override
  public NewBlackBook amount(BigDecimal amount) {
    this.next.amount(amount);
    return this;
  }

  @Override
  public NewBlackBook addBlackBookDetail(Consumer<NewBlackBookDetail> blackBookDetail) {
    final var allDetails = this.batch.build();
    final var builder = new NewBlackBookDetailBuilder(logger, blackBookId, allDetails, savedState);
    blackBookDetail.accept(builder);
    final var built = builder.close();
    this.batch.addBlackBookDetailInserts(built);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call NewBlackBook.build() to finalize black book CREATE!");
    final var blackbook = next.build();
    
    
    // Validate uniqueness - no duplicates
    RepoAssert.isTrue(
        this.allBlackBooks.values().stream()
        .filter(a -> (
            a.getDate().equals(blackbook.getDate())
            && a.getType().equals(blackbook.getType())
            && Objects.equal(a.getExternalId(), blackbook.getExternalId())
        ))
        .count() == 0
        , () -> "can't have duplicate black book  with same type/date/externalid for same black book!");

    this.logger.add(blackbook);
    return batch.addBlackBookInserts(blackbook).build();
  }
}