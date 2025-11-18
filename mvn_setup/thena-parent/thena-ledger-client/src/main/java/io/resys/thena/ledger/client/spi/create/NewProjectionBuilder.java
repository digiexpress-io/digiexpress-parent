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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewProjection;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewProjectionDetail;
import io.resys.thena.ledger.client.entities.ImmutableProjection;
import io.resys.thena.ledger.client.entities.Projection;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewProjectionBuilder implements NewProjection {
  private final LedgerCommitBuilder logger;
  private final String projectionId;
  private final Map<String, Projection> allProjections;
  private final ImmutableProjection.Builder next;
  private final LedgerContainer savedState;

  private boolean built;
  private ImmutablePersistenceUnit.Builder batch;
  
  public NewProjectionBuilder(
      LedgerCommitBuilder logger, 
      String ledgerId,
      ImmutablePersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.savedState = savedState;
    this.projectionId = OidUtils.genUUID();
    this.next = ImmutableProjection.builder()
        .id(projectionId)
        .ledgerId(ledgerId)
        .createdCommitId(logger.getCommitId());
    
    // Projections are immutable, so no updates/deletes to consider
    this.allProjections = Stream.of(
        // from current TX
        currentTx.getProjectionInserts().stream()
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewProjection externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewProjection type(String type) {
    this.next.projectionType(type);
    return this;
  }

  @Override
  public NewProjection subType(String subType) {
    this.next.projectionSubType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewProjection description(String description) {
    this.next.projectionDescription(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewProjection targetDate(LocalDate targetDate) {
    this.next.projectionTargetDate(targetDate);
    return this;
  }

  @Override
  public NewProjection startDate(LocalDate startDate) {
    this.next.projectionStartDate(startDate);
    return this;
  }

  @Override
  public NewProjection endDate(LocalDate endDate) {
    this.next.projectionEndDate(endDate);
    return this;
  }

  @Override
  public NewProjection amount(BigDecimal amount) {
    this.next.projectionAmount(amount);
    return this;
  }

  @Override
  public NewProjection addProjectionDetail(Consumer<NewProjectionDetail> projectionDetail) {
    final var allDetails = this.batch.build();
    final var builder = new NewProjectionDetailBuilder(logger, projectionId, allDetails, savedState);
    projectionDetail.accept(builder);
    final var built = builder.close();
    this.batch.addProjectionDetailInserts(built);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call NewProjection.build() to finalize projection CREATE!");

    final var projection = next.build();
  
    this.logger.add(projection);
    return batch.addProjectionInserts(projection).build();
  }
}