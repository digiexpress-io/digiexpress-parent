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
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewPaymentPlan;
import io.resys.thena.contract.client.entities.ImmutablePaymentPlan;
import io.resys.thena.contract.client.entities.PaymentPlan;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewPaymentPlanBuilder implements NewPaymentPlan {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, PaymentPlan> allPaymentPlans;
  private final ImmutablePaymentPlan.Builder next;
  private boolean built;
  
  public NewPaymentPlanBuilder(
      ContractCommitBuilder logger, 
      String contractId, 
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.next = ImmutablePaymentPlan.builder()
        .id(OidUtils.genUUID())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .partyId(Optional.empty())
        .paymentPlanEndDate(Optional.empty());
    
  
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
      )
      .flatMap(e -> e)
      .filter(saved -> !deletes.contains(saved.getId()))
      .filter(saved -> !updates.contains(saved.getId()))
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewPaymentPlan partyId(@Nullable String partyId) {
    this.next.partyId(Optional.ofNullable(partyId));
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanStatus(String paymentPlanStatus) {
    this.next.paymentPlanStatus(paymentPlanStatus);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanFrequency(String paymentPlanFrequency) {
    this.next.paymentPlanFrequency(paymentPlanFrequency);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanAmount(BigDecimal paymentPlanAmount) {
    this.next.paymentPlanAmount(paymentPlanAmount);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanStartDate(LocalDate paymentPlanStartDate) {
    this.next.paymentPlanStartDate(paymentPlanStartDate);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanEndDate(@Nullable LocalDate paymentPlanEndDate) {
    this.next.paymentPlanEndDate(Optional.ofNullable(paymentPlanEndDate));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePaymentPlan close() {
    RepoAssert.isTrue(built, () -> "you must call NewPaymentPlan.build() to finalize payment plan CREATE!");
    
    final var built = next.build();
    
    this.logger.add(built);
    return built;
  }
}