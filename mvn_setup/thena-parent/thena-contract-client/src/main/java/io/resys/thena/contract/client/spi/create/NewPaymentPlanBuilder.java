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
import java.util.Map;

import io.resys.thena.contract.client.api.ThenaContractNewObject.NewPaymentPlan;
import io.resys.thena.contract.client.entities.ImmutablePaymentPlan;
import io.resys.thena.contract.client.entities.PaymentPlan;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
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
      Map<String, PaymentPlan> allPaymentPlans) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.allPaymentPlans = allPaymentPlans;
    this.next = ImmutablePaymentPlan.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId);
  }

  @Override
  public NewPaymentPlan partyId(@Nullable String partyId) {
    this.next.partyId(partyId);
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
  public NewPaymentPlan paymentPlanStartDateInterval(@Nullable Duration paymentPlanStartDateInterval) {
    this.next.paymentPlanStartDateInterval(paymentPlanStartDateInterval);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanStartDateType(@Nullable String paymentPlanStartDateType) {
    this.next.paymentPlanStartDateType(paymentPlanStartDateType);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanEndDate(@Nullable LocalDate paymentPlanEndDate) {
    this.next.paymentPlanEndDate(paymentPlanEndDate);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanEndDateInterval(@Nullable Duration paymentPlanEndDateInterval) {
    this.next.paymentPlanEndDateInterval(paymentPlanEndDateInterval);
    return this;
  }

  @Override
  public NewPaymentPlan paymentPlanEndDateType(@Nullable String paymentPlanEndDateType) {
    this.next.paymentPlanEndDateType(paymentPlanEndDateType);
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