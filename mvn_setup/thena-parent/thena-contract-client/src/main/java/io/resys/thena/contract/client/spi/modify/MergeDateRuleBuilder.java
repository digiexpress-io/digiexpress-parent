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

import java.time.Period;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeDateRule;
import io.resys.thena.contract.client.entities.DateRule;
import io.resys.thena.contract.client.entities.ImmutableDateRule;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergeDateRuleBuilder implements MergeDateRule {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final DateRule currentDateRule; 
  private final ImmutableDateRule.Builder nextDateRule;
  private final Map<String, DateRule> allDateRules;
  private boolean built;

  public MergeDateRuleBuilder(
      ContractContainer container, ContractCommitBuilder logger, 
      String contractId, String dateRuleId, 
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentDateRule = container.getDateRules().stream()
        .filter(dr -> dr.getId().equals(dateRuleId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentDateRule, () -> "Can't find date rule with id: '" + dateRuleId + "' for contract: '" + contractId + "'!");
    this.nextDateRule = ImmutableDateRule.builder().from(currentDateRule);

    
    final var updates = currentTx.getDateRuleUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = Collections.<String>emptyList(); // Date rule doesn't support deletes
    
    this.allDateRules = Stream.of(
        // from current TX
        currentTx.getDateRuleInserts().stream(),
        currentTx.getDateRuleUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getDateRules())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public MergeDateRule invPlanId(@Nullable String invPlanId) {
    this.nextDateRule.invPlanId(Optional.ofNullable(invPlanId));
    return this;
  }

  @Override
  public MergeDateRule coverageId(@Nullable String coverageId) {
    this.nextDateRule.coverageId(Optional.ofNullable(coverageId));
    return this;
  }

  @Override
  public MergeDateRule partyId(@Nullable String partyId) {
    this.nextDateRule.partyId(Optional.ofNullable(partyId));
    return this;
  }

  @Override
  public MergeDateRule paymentPlanId(@Nullable String paymentPlanId) {
    this.nextDateRule.paymentPlanId(Optional.ofNullable(paymentPlanId));
    return this;
  }

  @Override
  public MergeDateRule dateRuleEntity(String dateRuleEntity) {
    this.nextDateRule.dateRuleEntity(dateRuleEntity);
    return this;
  }

  @Override
  public MergeDateRule dateRuleEntityField(String dateRuleEntityField) {
    this.nextDateRule.dateRuleEntityField(dateRuleEntityField);
    return this;
  }

  @Override
  public MergeDateRule dateRuleType(String dateRuleType) {
    this.nextDateRule.dateRuleType(dateRuleType);
    return this;
  }

  @Override
  public MergeDateRule dateRulePeriod(@Nullable Period dateRulePeriod) {
    this.nextDateRule.dateRulePeriod(Optional.ofNullable(dateRulePeriod));
    return this;
  }

  @Override
  public MergeDateRule dateRuleName(@Nullable String dateRuleName) {
    this.nextDateRule.dateRuleName(Optional.ofNullable(dateRuleName));
    return this;
  }

  @Override
  public MergeDateRule dateRuleDescription(@Nullable String dateRuleDescription) {
    this.nextDateRule.dateRuleDescription(Optional.ofNullable(dateRuleDescription));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeDateRule.build() to finalize date rule MERGE!");
    
    var nextDateRule = this.nextDateRule.build();
    final var isModified = !nextDateRule.equals(currentDateRule);
    if(isModified) {
      nextDateRule = ImmutableDateRule.builder()
          .from(nextDateRule)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentDateRule, nextDateRule);
      batch.addDateRuleUpdates(nextDateRule);
    }
    return batch.build();
  }
}