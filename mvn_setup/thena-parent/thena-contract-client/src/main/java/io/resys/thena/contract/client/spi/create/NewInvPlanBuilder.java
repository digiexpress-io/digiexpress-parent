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

import java.time.Duration;
import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlan;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewInvPlanAlloc;
import io.resys.thena.contract.client.entities.ImmutableInvPlan;
import io.resys.thena.contract.client.spi.commitlog.ContractBatchOperations;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewInvPlanBuilder implements NewInvPlan {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final String invPlanId;
  private final ImmutableInvPlan.Builder next;
  private ContractBatchOperations.Builder batch;
  private boolean built;
  
  public NewInvPlanBuilder(ContractCommitBuilder logger, String contractId) {
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.invPlanId = OidUtils.gen();
    this.next = ImmutableInvPlan.builder()
        .id(invPlanId)
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId);
    
    this.batch = ContractBatchOperations.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
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
  public NewInvPlan invPlanStartDateInterval(@Nullable Duration invPlanStartDateInterval) {
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
    this.next.invPlanEndDate(invPlanEndDate);
    return this;
  }

  @Override
  public NewInvPlan invPlanEndDateInterval(@Nullable Duration invPlanEndDateInterval) {
    this.next.invPlanEndDateInterval(invPlanEndDateInterval);
    return this;
  }

  @Override
  public NewInvPlan invPlanEndDateType(@Nullable String invPlanEndDateType) {
    this.next.invPlanEndDateType(invPlanEndDateType);
    return this;
  }

  @Override
  public NewInvPlan addAllocation(Consumer<NewInvPlanAlloc> allocation) {
    final var allAllocations = this.batch.build().getInvPlanAllocations().stream()
        .collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewInvPlanAllocBuilder(logger, invPlanId, allAllocations);
    allocation.accept(builder);
    final var built = builder.close();
    this.batch.addInvPlanAllocations(built);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ContractBatchOperations close() {
    RepoAssert.isTrue(built, () -> "you must call NewInvPlan.build() to finalize investment plan CREATE!");
    
    final var invPlan = next.build();
    
    this.logger.add(invPlan);
    
    return batch.addInvPlans(invPlan).build();
  }
}