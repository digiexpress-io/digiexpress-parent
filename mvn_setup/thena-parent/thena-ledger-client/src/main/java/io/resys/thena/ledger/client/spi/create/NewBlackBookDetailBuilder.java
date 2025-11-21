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
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewBlackBookDetail;
import io.resys.thena.ledger.client.entities.BlackBookDetail;
import io.resys.thena.ledger.client.entities.ImmutableBlackBookDetail;
import io.resys.thena.ledger.client.spi.commitlog.LedgerCommitBuilder;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewBlackBookDetailBuilder implements NewBlackBookDetail {
  private final LedgerCommitBuilder logger;
  private final Map<String, BlackBookDetail> allBlackBookDetails;
  private final ImmutableBlackBookDetail.Builder next;
  
  private boolean built;
  
  public NewBlackBookDetailBuilder(
      LedgerCommitBuilder logger, 
      String blackBookId,
      ImmutablePersistenceUnit currentTx,
      @Nullable LedgerContainer savedState) {
    
    super();
    this.logger = logger;
    this.next = ImmutableBlackBookDetail.builder()
        .id(OidUtils.genUUID())
        .blackBookId(blackBookId)
        .targetId(Optional.empty())
        .paymentId(Optional.empty())
        .externalId(Optional.empty())
        .detailStartDate(Optional.empty())
        .detailEndDate(Optional.empty())
        .createdCommitId(logger.getCommitId());
    
    // BlackBookDetails are immutable, so no updates/deletes to consider
    this.allBlackBookDetails = Stream.of(
        // from current TX
        currentTx.getBlackBookDetailInserts().stream()
          .filter(b -> b.getBlackBookId().equals(blackBookId)),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getBlackBookDetails().get(blackBookId))
          .map(groupedDetails -> groupedDetails.stream())
          .orElse(Stream.empty())
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewBlackBookDetail externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewBlackBookDetail type(String type) {
    this.next.detailType(type);
    return this;
  }

  @Override
  public NewBlackBookDetail subType(String subType) {
    this.next.detailSubType(Optional.ofNullable(subType));
    return this;
  }

  @Override
  public NewBlackBookDetail description(String description) {
    this.next.detailDescription(Optional.ofNullable(description));
    return this;
  }

  @Override
  public NewBlackBookDetail targetId(String targetId) {
    this.next.targetId(Optional.ofNullable(targetId));
    return this;
  }

  @Override
  public NewBlackBookDetail startDate(LocalDate startDate) {
    this.next.detailStartDate(startDate);
    return this;
  }

  @Override
  public NewBlackBookDetail endDate(LocalDate endDate) {
    this.next.detailEndDate(endDate);
    return this;
  }

  @Override
  public NewBlackBookDetail amount(BigDecimal amount) {
    this.next.detailAmount(amount);
    return this;
  }

  @Override
  public NewBlackBookDetail formula(String formula) {
    this.next.detailFormula(Optional.ofNullable(formula));
    return this;
  }
  @Override
  public NewBlackBookDetail paymentId(String paymentId) {
    this.next.paymentId(Optional.ofNullable(paymentId));
    return this;
  }  
  @Override
  public NewBlackBookDetail body(JsonObject body) {
    this.next.detailBody(Optional.ofNullable(body));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public BlackBookDetail close() {
    RepoAssert.isTrue(built, () -> "you must call NewBlackBookDetail.build() to finalize black book detail CREATE!");

    
    final var built = next.build();
    
    // Validate uniqueness - no duplicate allocations with same code for same investment plan
    RepoAssert.isTrue(
        this.allBlackBookDetails.values().stream()
        .filter(a -> (
            a.getDetailStartDate().equals(built.getDetailStartDate())
            && a.getDetailType().equals(built.getDetailType())
            && Objects.equal(a.getTargetId().orElse(null), built.getTargetId().orElse(null))
        ))
        .count() == 0
        , () -> "can't have duplicate black book details with same type/start-date for same black book!");

    this.logger.add(built);
    return built;
  }
}