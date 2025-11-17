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

import com.google.common.base.Objects;

import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewProjectionDetail;
import io.resys.thena.ledger.client.entities.ImmutableProjectionDetail;
import io.resys.thena.ledger.client.entities.ProjectionDetail;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewProjectionDetailBuilder implements NewProjectionDetail {
  private final LedgerCommitBuilder logger;
  private final Map<String, ProjectionDetail> allProjectionDetails;
  private final ImmutableProjectionDetail.Builder next;
  private boolean built;
  
  public NewProjectionDetailBuilder(
      LedgerCommitBuilder logger, 
      String projectionId,
      PersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.next = ImmutableProjectionDetail.builder()
        .id(OidUtils.genUUID())
        .projectionId(projectionId)
        .createdCommitId(logger.getCommitId());
    
    // ProjectionDetails are immutable, so no updates/deletes to consider
    this.allProjectionDetails = Stream.of(
        // from current TX
        currentTx.getProjectionDetailInserts().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getProjectionDetails().get(projectionId))
          .map(groupedDetails -> groupedDetails.stream())
          .orElse(Stream.empty())
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewProjectionDetail externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewProjectionDetail type(String type) {
    this.next.type(type);
    return this;
  }

  @Override
  public NewProjectionDetail subType(String subType) {
    this.next.subType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewProjectionDetail description(String description) {
    this.next.description(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewProjectionDetail targetId(String targetId) {
    this.next.targetId(Optional.ofNullable(targetId));
    return this;
  }

  @Override
  public NewProjectionDetail startDate(LocalDate startDate) {
    this.next.startDate(startDate);
    return this;
  }

  @Override
  public NewProjectionDetail endDate(LocalDate endDate) {
    this.next.endDate(endDate);
    return this;
  }

  @Override
  public NewProjectionDetail amount(BigDecimal amount) {
    this.next.amount(amount);
    return this;
  }

  @Override
  public NewProjectionDetail formula(String formula) {
    this.next.formula(Optional.ofNullable(formula));
    return this;
  }

  @Override
  public NewProjectionDetail body(JsonObject body) {
    this.next.body(Optional.ofNullable(body));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ProjectionDetail close() {
    RepoAssert.isTrue(built, () -> "you must call NewProjectionDetail.build() to finalize projection detail CREATE!");
    
    final var built = next.build();
    
    // Validate uniqueness - no duplicate allocations with same code for same investment plan
    RepoAssert.isTrue(
        this.allProjectionDetails.values().stream()
        .filter(a -> (
            a.getStartDate().equals(built.getStartDate())
            && Objects.equal(a.getTargetId().orElse(null), built.getTargetId().orElse(null))
            && a.getType().equals(built.getType())
        ))
        .count() == 0
        , () -> "can't have duplicate projection details with same type/start-date for same projection!");

    this.logger.add(built);
    return built;
  }
}