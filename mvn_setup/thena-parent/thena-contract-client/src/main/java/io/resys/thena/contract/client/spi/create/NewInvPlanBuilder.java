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

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlanAlloc;
import io.resys.thena.contract.client.entities.ImmutableInvPlan;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewInvPlanBuilder implements NewInvPlan {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final String invPlanId;
  private final Map<String, InvPlan> allPalns;
  private final ImmutableInvPlan.Builder next;
  private final ContractContainer savedState;
  private ImmutablePersistenceUnit.Builder batch;
  
  private boolean built;
  
  public NewInvPlanBuilder(
      ContractCommitBuilder logger, 
      String contractId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.invPlanId = OidUtils.genUUID();
    this.savedState = savedState;
    this.next = ImmutableInvPlan.builder()
        .id(invPlanId)
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .invPlanEndDate(Optional.empty())
        .invPlanEndDateInterval(Optional.empty())
        .invPlanEndDateType(Optional.empty());
    
    this.batch = ImmutablePersistenceUnit.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    
    
    final var updates = currentTx.getInvPlanUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getInvPlanDeletes().stream().map(e -> e.getId()).toList();
    
    this.allPalns = Stream.of(
        // from current TX
        currentTx.getInvPlanInserts().stream(),
        currentTx.getInvPlanUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getInvPlans())
          .orElse(Collections.emptyList())
          .stream()
      )
      .flatMap(e -> e)
      .filter(saved -> !deletes.contains(saved.getId()))
      .filter(saved -> !updates.contains(saved.getId()))
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewInvPlan externalId(String externalId) {
    this.next.externalId(externalId);
    return this;
  }

  @Override
  public NewInvPlan invPlanCode(String invPlanCode) {
    this.next.invPlanCode(invPlanCode);
    return this;
  }

  @Override
  public NewInvPlan invPlanName(String invPlanName) {
    this.next.invPlanName(invPlanName);
    return this;
  }

  @Override
  public NewInvPlan invPlanStatus(String invPlanStatus) {
    this.next.invPlanStatus(invPlanStatus);
    return this;
  }

  @Override
  public NewInvPlan invPlanStartDate(LocalDate invPlanStartDate) {
    this.next.invPlanStartDate(invPlanStartDate);
    return this;
  }

  @Override
  public NewInvPlan invPlanStartDateInterval(@Nullable Period invPlanStartDateInterval) {
    this.next.invPlanStartDateInterval(invPlanStartDateInterval);
    return this;
  }

  @Override
  public NewInvPlan invPlanStartDateType(@Nullable String invPlanStartDateType) {
    this.next.invPlanStartDateType(invPlanStartDateType);
    return this;
  }

  @Override
  public NewInvPlan invPlanEndDate(@Nullable LocalDate invPlanEndDate) {
    this.next.invPlanEndDate(Optional.ofNullable(invPlanEndDate));
    return this;
  }

  @Override
  public NewInvPlan invPlanEndDateInterval(@Nullable Period invPlanEndDateInterval) {
    this.next.invPlanEndDateInterval(Optional.ofNullable(invPlanEndDateInterval));
    return this;
  }

  @Override
  public NewInvPlan invPlanEndDateType(@Nullable String invPlanEndDateType) {
    this.next.invPlanEndDateType(Optional.ofNullable(invPlanEndDateType));
    return this;
  }

  @Override
  public NewInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation) {
    final var allAllocations = this.batch.build();
    final var builder = new NewInvPlanAllocBuilder(logger, invPlanId, allAllocations, savedState);
    allocation.accept(builder);
    final var built = builder.close();
    this.batch.addInvPlanAllocInserts(built);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call NewInvPlan.build() to finalize investment plan CREATE!");
    
    final var invPlan = next.build();
    
    this.logger.add(invPlan);
    
    return batch.addInvPlanInserts(invPlan).build();
  }
}
