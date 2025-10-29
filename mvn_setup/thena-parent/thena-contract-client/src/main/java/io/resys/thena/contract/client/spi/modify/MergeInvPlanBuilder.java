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

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeInvPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlanAlloc;
import io.resys.thena.contract.client.entities.ImmutableInvPlan;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.create.NewInvPlanAllocBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;

public class MergeInvPlanBuilder implements MergeInvPlan {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final InvPlan currentInvPlan;
  private final ImmutableInvPlan.Builder nextInvPlan;
  private final Map<String, InvPlan> allInvPlans;
  private final String contractId;
  private boolean built;

  public MergeInvPlanBuilder(ContractContainer container, ContractCommitBuilder logger, String contractId, String invPlanId,
      Map<String, InvPlan> allInvPlans) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentInvPlan = container.getInvPlans().stream()
        .filter(i -> i.getId().equals(invPlanId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentInvPlan, () -> "Can't find investment plan with id: '" + invPlanId + "' for contract: '" + contractId + "'!");
    this.nextInvPlan = ImmutableInvPlan.builder().from(currentInvPlan);
    this.allInvPlans = allInvPlans;
    this.contractId = contractId;
  }

  @Override
  public MergeInvPlan externalId(String externalId) {
    this.nextInvPlan.externalId(externalId);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStatus(String invPlanStatus) {
    this.nextInvPlan.invPlanStatus(invPlanStatus);
    return this;
  }
  @Override
  public MergeInvPlan invPlanCode(String invPlanCode) {
    this.nextInvPlan.invPlanCode(invPlanCode);
    return this;
  }
  @Override
  public MergeInvPlan invPlanName(String invPlanName) {
    this.nextInvPlan.invPlanName(invPlanName);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStartDate(LocalDate invPlanStartDate) {
    this.nextInvPlan.invPlanStartDate(invPlanStartDate);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStartDateInterval(Duration invPlanStartDateInterval) {
    this.nextInvPlan.invPlanStartDateInterval(invPlanStartDateInterval);
    return this;
  }
  @Override
  public MergeInvPlan invPlanStartDateType(String invPlanStartDateType) {
    this.nextInvPlan.invPlanStartDateType(invPlanStartDateType);
    return this;
  }
  @Override
  public MergeInvPlan invPlanEndDate(LocalDate invPlanEndDate) {
    this.nextInvPlan.invPlanEndDate(invPlanEndDate);
    return this;
  }
  @Override
  public MergeInvPlan invPlanEndDateInterval(Duration invPlanEndDateInterval) {
    this.nextInvPlan.invPlanEndDateInterval(invPlanEndDateInterval);
    return this;
  }
  @Override
  public MergeInvPlan invPlanEndDateType(String invPlanEndDateType) {
    this.nextInvPlan.invPlanEndDateType(invPlanEndDateType);
    return this;
  }
  @Override
  public <T> MergeInvPlan setAllAllocations(List<T> replacements, Function<T, Consumer<NewInvPlanAlloc>> callbacks) {
    // clear old
    this.batch.invPlanAllocs(Collections.emptyList());
    final var allAllocations = new HashMap<String, InvPlanAlloc>();
    
    // delete old - find all existing allocations for this investment plan
    final var existingAllocs = this.batch.build().getInvPlanAllocs().stream()
        .filter(a -> a.getInvPlanId().equals(currentInvPlan.getId()))
        .collect(Collectors.toList());
    this.batch.addAllDeleteInvPlanAllocs(existingAllocs);
    
    // add new
    for(final var replacement : replacements) {
      final var allocation = callbacks.apply(replacement);
      
      final var builder = new NewInvPlanAllocBuilder(logger, currentInvPlan.getId(), Collections.unmodifiableMap(allAllocations));
      allocation.accept(builder);
      final var built = builder.close();
      allAllocations.put(built.getId(), built);
      this.batch.addInvPlanAllocs(built);
    }
    return this;
  }
  @Override
  public MergeInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation) {
    final var allAllocations = this.batch.build().getInvPlanAllocs().stream()
        .collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewInvPlanAllocBuilder(logger, currentInvPlan.getId(), allAllocations);
    allocation.accept(builder);
    final var built = builder.close();
    this.batch.addInvPlanAllocs(built);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeInvPlan.build() to finalize investment plan MERGE!");
    
    var nextInvPlan = this.nextInvPlan.build();
    final var isModified = !nextInvPlan.equals(currentInvPlan);
    if(isModified) {
      nextInvPlan = ImmutableInvPlan.builder()
          .from(nextInvPlan)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentInvPlan, nextInvPlan);
      batch.addInvPlanUpdates(nextInvPlan);
    }
    return batch.build();
  }
}