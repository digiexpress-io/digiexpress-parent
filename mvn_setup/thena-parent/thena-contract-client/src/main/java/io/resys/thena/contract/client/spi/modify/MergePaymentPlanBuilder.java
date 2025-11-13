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
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergePaymentPlan;
import io.resys.thena.contract.client.entities.ImmutablePaymentPlan;
import io.resys.thena.contract.client.entities.PaymentPlan;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class MergePaymentPlanBuilder implements MergePaymentPlan {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final PaymentPlan currentPaymentPlan; 
  private final ImmutablePaymentPlan.Builder nextPaymentPlan;
  private final Map<String, PaymentPlan> allPaymentPlans;
  private boolean built;

  public MergePaymentPlanBuilder(ContractContainer container, ContractCommitBuilder logger, String contractId, String paymentPlanId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentPaymentPlan = container.getPaymentPlans().stream()
        .filter(p -> p.getId().equals(paymentPlanId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentPaymentPlan, () -> "Can't find payment plan with id: '" + paymentPlanId + "' for contract: '" + contractId + "'!");
    this.nextPaymentPlan = ImmutablePaymentPlan.builder().from(currentPaymentPlan);
    
    final var updates = currentTx.getPaymentPlanUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getPaymentPlanDeletes().stream().map(e -> e.getId()).toList();
    
    this.allPaymentPlans = Stream.of(
        // from current TX
        currentTx.getPaymentPlanInserts().stream(),
        currentTx.getPaymentPlanUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getPaymentPlans())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public MergePaymentPlan partyId(String partyId) {
    this.nextPaymentPlan.partyId(Optional.ofNullable(partyId));
    return this;
  }

  @Override
  public MergePaymentPlan paymentPlanStatus(String paymentPlanStatus) {
    this.nextPaymentPlan.paymentPlanStatus(paymentPlanStatus);
    return this;
  }

  @Override
  public MergePaymentPlan paymentPlanFrequency(String paymentPlanFrequency) {
    this.nextPaymentPlan.paymentPlanFrequency(paymentPlanFrequency);
    return this;
  }

  @Override
  public MergePaymentPlan paymentPlanAmount(BigDecimal paymentPlanAmount) {
    this.nextPaymentPlan.paymentPlanAmount(paymentPlanAmount);
    return this;
  }

  @Override
  public MergePaymentPlan paymentPlanStartDate(LocalDate paymentPlanStartDate) {
    this.nextPaymentPlan.paymentPlanStartDate(paymentPlanStartDate);
    return this;
  }


  @Override
  public MergePaymentPlan paymentPlanEndDate(LocalDate paymentPlanEndDate) {
    this.nextPaymentPlan.paymentPlanEndDate(Optional.ofNullable(paymentPlanEndDate));
    return this;
  }


  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergePaymentPlan.build() to finalize payment plan MERGE!");
    
    var nextPaymentPlan = this.nextPaymentPlan.build();
    final var isModified = !nextPaymentPlan.equals(currentPaymentPlan);
    if(isModified) {
      nextPaymentPlan = ImmutablePaymentPlan.builder()
          .from(nextPaymentPlan)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentPaymentPlan, nextPaymentPlan);
      batch.addPaymentPlanUpdates(nextPaymentPlan);
    }
    return batch.build();
  }
}