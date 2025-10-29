package io.resys.thena.contract.client.spi.modify;

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
import java.util.Map;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeCoverage;
import io.resys.thena.contract.client.entities.Coverage;
import io.resys.thena.contract.client.entities.ImmutableCoverage;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;

public class MergeCoverageBuilder implements MergeCoverage {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final Coverage currentCoverage; 
  private final ImmutableCoverage.Builder nextCoverage;
  private final Map<String, Coverage> allCoverages;
  private boolean built;

  public MergeCoverageBuilder(ContractContainer container, ContractCommitBuilder logger, String contractId, String coverageId,
      Map<String, Coverage> allCoverages) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentCoverage = container.getCoverages().stream()
        .filter(c -> c.getId().equals(coverageId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentCoverage, () -> "Can't find coverage with id: '" + coverageId + "' for contract: '" + contractId + "'!");
    this.nextCoverage = ImmutableCoverage.builder().from(currentCoverage);
    this.allCoverages = allCoverages;
  }

  @Override
  public MergeCoverage coverageType(String coverageType) {
    this.nextCoverage.coverageType(coverageType);
    return this;
  }

  @Override
  public MergeCoverage insuredId(String insuredId) {
    this.nextCoverage.insuredId(insuredId);
    return this;
  }

  @Override
  public MergeCoverage externalId(String externalId) {
    this.nextCoverage.externalId(externalId);
    return this;
  }

  @Override
  public MergeCoverage coverageCode(String coverageCode) {
    this.nextCoverage.coverageCode(coverageCode);
    return this;
  }

  @Override
  public MergeCoverage coverageSumInsured(BigDecimal coverageSumInsured) {
    this.nextCoverage.coverageSumInsured(coverageSumInsured);
    return this;
  }

  @Override
  public MergeCoverage coverageRate(BigDecimal coverageRate) {
    this.nextCoverage.coverageRate(coverageRate);
    return this;
  }

  @Override
  public MergeCoverage coverageRateType(String coverageRateType) {
    this.nextCoverage.coverageRateType(coverageRateType);
    return this;
  }

  @Override
  public MergeCoverage coverageStatus(String coverageStatus) {
    this.nextCoverage.coverageStatus(coverageStatus);
    return this;
  }

  @Override
  public MergeCoverage coverageEffectiveFrom(LocalDate coverageEffectiveFrom) {
    this.nextCoverage.coverageEffectiveFrom(coverageEffectiveFrom);
    return this;
  }

  @Override
  public MergeCoverage coverageEffectiveTo(LocalDate coverageEffectiveTo) {
    this.nextCoverage.coverageEffectiveTo(coverageEffectiveTo);
    return this;
  }

  @Override
  public MergeCoverage coverageTermStartDate(LocalDate coverageTermStartDate) {
    this.nextCoverage.coverageTermStartDate(coverageTermStartDate);
    return this;
  }

  @Override
  public MergeCoverage coverageTermStartDateInterval(Duration coverageTermStartDateInterval) {
    this.nextCoverage.coverageTermStartDateInterval(coverageTermStartDateInterval);
    return this;
  }

  @Override
  public MergeCoverage coverageTermStartDateType(String coverageTermStartDateType) {
    this.nextCoverage.coverageTermStartDateType(coverageTermStartDateType);
    return this;
  }

  @Override
  public MergeCoverage coverageTermEndDate(LocalDate coverageTermEndDate) {
    this.nextCoverage.coverageTermEndDate(coverageTermEndDate);
    return this;
  }

  @Override
  public MergeCoverage coverageTermEndDateInterval(Duration coverageTermEndDateInterval) {
    this.nextCoverage.coverageTermEndDateInterval(coverageTermEndDateInterval);
    return this;
  }

  @Override
  public MergeCoverage coverageTermEndDateType(String coverageTermEndDateType) {
    this.nextCoverage.coverageTermEndDateType(coverageTermEndDateType);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeCoverage.build() to finalize coverage MERGE!");
    
    var nextCoverage = this.nextCoverage.build();
    final var isModified = !nextCoverage.equals(currentCoverage);
    if(isModified) {
      nextCoverage = ImmutableCoverage.builder()
          .from(nextCoverage)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentCoverage, nextCoverage);
      batch.addCoverageUpdates(nextCoverage);
    }
    return batch.build();
  }
}