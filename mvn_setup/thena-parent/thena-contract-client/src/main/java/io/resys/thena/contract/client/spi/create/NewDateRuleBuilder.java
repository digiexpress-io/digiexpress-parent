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

import java.time.Period;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewDateRule;
import io.resys.thena.contract.client.entities.DateRule;
import io.resys.thena.contract.client.entities.ImmutableDateRule;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewDateRuleBuilder implements NewDateRule {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, DateRule> allDateRules;
  private final ImmutableDateRule.Builder next;
  private final String dateRuleId;
  private boolean built;
  
  public NewDateRuleBuilder(
      ContractCommitBuilder logger, 
      String contractId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState
  ) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.dateRuleId = OidUtils.genUUID();
    this.next = ImmutableDateRule.builder()
        .id(dateRuleId)
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .invPlanId(Optional.empty())
        .coverageId(Optional.empty())
        .partyId(Optional.empty())
        .paymentPlanId(Optional.empty())
        .dateRulePeriod(Optional.empty())
        .dateRuleName(Optional.empty())
        .dateRuleDescription(Optional.empty());
    
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
  public NewDateRule invPlanId(@Nullable String invPlanId) {
    this.next.invPlanId(Optional.ofNullable(invPlanId));
    return this;
  }

  @Override
  public NewDateRule coverageId(@Nullable String coverageId) {
    this.next.coverageId(Optional.ofNullable(coverageId));
    return this;
  }

  @Override
  public NewDateRule partyId(@Nullable String partyId) {
    this.next.partyId(Optional.ofNullable(partyId));
    return this;
  }

  @Override
  public NewDateRule paymentPlanId(@Nullable String paymentPlanId) {
    this.next.paymentPlanId(Optional.ofNullable(paymentPlanId));
    return this;
  }

  @Override
  public NewDateRule dateRuleEntity(String dateRuleEntity) {
    this.next.dateRuleEntity(dateRuleEntity);
    return this;
  }

  @Override
  public NewDateRule dateRuleEntityField(String dateRuleEntityField) {
    this.next.dateRuleEntityField(dateRuleEntityField);
    return this;
  }

  @Override
  public NewDateRule dateRuleType(String dateRuleType) {
    this.next.dateRuleType(dateRuleType);
    return this;
  }

  @Override
  public NewDateRule dateRulePeriod(@Nullable Period dateRulePeriod) {
    this.next.dateRulePeriod(Optional.ofNullable(dateRulePeriod));
    return this;
  }

  @Override
  public NewDateRule dateRuleName(@Nullable String dateRuleName) {
    this.next.dateRuleName(Optional.ofNullable(dateRuleName));
    return this;
  }

  @Override
  public NewDateRule dateRuleDescription(@Nullable String dateRuleDescription) {
    this.next.dateRuleDescription(Optional.ofNullable(dateRuleDescription));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableDateRule close() {
    RepoAssert.isTrue(built, () -> "you must call NewDateRule.build() to finalize date rule CREATE!");
    
    final var dateRule = next.build();
    
    this.logger.add(dateRule);
    
    return dateRule;
  }
}