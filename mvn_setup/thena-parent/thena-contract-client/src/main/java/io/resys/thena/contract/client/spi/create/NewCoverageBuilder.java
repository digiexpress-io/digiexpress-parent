package io.resys.thena.contract.client.spi.create;

/*-
 * #%L
 * thena-contract-client
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
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCoverage;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewNote;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractRelationType;
import io.resys.thena.contract.client.entities.Coverage;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableCoverage;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewCoverageBuilder implements NewCoverage {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, Coverage> allCoverages;
  private final ImmutableCoverage.Builder next;
  private final ImmutablePersistenceUnit currentTx;
  private final ContractContainer savedState;
  private boolean built;
  
  public NewCoverageBuilder(
      ContractCommitBuilder logger, 
      String contractId, 
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.currentTx = currentTx;
    this.savedState = savedState;
    this.next = ImmutableCoverage.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId);
    
    final var updates = currentTx.getCoverageUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getCoverageDeletes().stream().map(e -> e.getId()).toList();
    
    this.allCoverages = Stream.of(
        // from current TX
        currentTx.getCoverageInserts().stream(),
        currentTx.getCoverageUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getCoverages())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));

  }

  @Override
  public NewCoverage insuredId(String insuredId) {
    this.next.insuredId(insuredId);
    return this;
  }

  @Override
  public NewCoverage externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewCoverage coverageType(String coverageType) {
    this.next.coverageType(coverageType);
    return this;
  }

  @Override
  public NewCoverage coverageCode(String coverageCode) {
    this.next.coverageCode(coverageCode);
    return this;
  }

  @Override
  public NewCoverage coverageSumInsured(@Nullable BigDecimal coverageSumInsured) {
    this.next.coverageSumInsured(coverageSumInsured);
    return this;
  }

  @Override
  public NewCoverage coverageRate(@Nullable BigDecimal coverageRate) {
    this.next.coverageRate(coverageRate);
    return this;
  }

  @Override
  public NewCoverage coverageRateType(@Nullable String coverageRateType) {
    this.next.coverageRateType(coverageRateType);
    return this;
  }

  @Override
  public NewCoverage coverageStatus(String coverageStatus) {
    this.next.coverageStatus(coverageStatus);
    return this;
  }

  @Override
  public NewCoverage coverageEffectiveFrom(LocalDate coverageEffectiveFrom) {
    this.next.coverageEffectiveFrom(coverageEffectiveFrom);
    return this;
  }

  @Override
  public NewCoverage coverageEffectiveTo(@Nullable LocalDate coverageEffectiveTo) {
    this.next.coverageEffectiveTo(coverageEffectiveTo);
    return this;
  }

  @Override
  public NewCoverage coverageTermStartDate(LocalDate coverageTermStartDate) {
    this.next.coverageTermStartDate(coverageTermStartDate);
    return this;
  }

  @Override
  public NewCoverage coverageTermStartDateInterval(@Nullable Duration coverageTermStartDateInterval) {
    this.next.coverageTermStartDateInterval(coverageTermStartDateInterval);
    return this;
  }

  @Override
  public NewCoverage coverageTermStartDateType(@Nullable String coverageTermStartDateType) {
    this.next.coverageTermStartDateType(coverageTermStartDateType);
    return this;
  }

  @Override
  public NewCoverage coverageTermEndDate(@Nullable LocalDate coverageTermEndDate) {
    this.next.coverageTermEndDate(coverageTermEndDate);
    return this;
  }

  @Override
  public NewCoverage coverageTermEndDateInterval(@Nullable Duration coverageTermEndDateInterval) {
    this.next.coverageTermEndDateInterval(coverageTermEndDateInterval);
    return this;
  }

  @Override
  public NewCoverage coverageTermEndDateType(@Nullable String coverageTermEndDateType) {
    this.next.coverageTermEndDateType(coverageTermEndDateType);
    return this;
  }

  @Override
  public NewCoverage addNote(Consumer<NewNote> note) {
    final var coverageRel = ImmutableContractOneOfRelations.builder()
        .relationType(ContractRelationType.COVERAGE)
        .coverageId(this.next.build().getId())
        .build();
    
    final var builder = new NewNoteBuilder(logger, contractId, coverageRel, currentTx, savedState);
    note.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    return this;
  }

  @Override
  public NewCoverage addReference(Consumer<NewReference> reference) {
    final var coverageRel = ImmutableContractOneOfRelations.builder()
        .relationType(ContractRelationType.COVERAGE)
        .coverageId(this.next.build().getId())
        .build();
    
    final var builder = new NewReferenceBuilder(logger, contractId, coverageRel, currentTx, savedState);
    reference.accept(builder);
    final var built = builder.close();
    this.logger.add(built);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableCoverage close() {
    RepoAssert.isTrue(built, () -> "you must call NewCoverage.build() to finalize coverage CREATE!");
    
    final var built = next.build();
    
    this.logger.add(built);
    return built;
  }
}