package io.resys.lp.batches.gen_contracts;


/*-
 * #%L
 * lp-batches
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

import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorConfig;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.resys.lp.batches.gen_contracts.BatchJob_Generate_Add_Payments.PaymentGenConfig;
import io.resys.lp.client.spi.LpClientImpl;
import io.resys.lp.client.spi.formula.feemi_savings.AddPaymentFactory;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class BatchJob_Generate_Add_Payments implements Executor<ContractContainer, PaymentGenConfig> {

  private final ContractClient contractClient;
  private final LedgerClient ledgerClient;

  @Override
  public ExecutorQuery<ContractContainer, PaymentGenConfig> before(ExecutorContext context) {
    return new ExecutorQuery<ContractContainer, PaymentGenConfig>() {
      @Override
      public PaymentGenConfig getConfig() {
        return new PaymentGenConfig();
      }
      @Override
      public Multi<ContractContainer> findAll() {
        return contractClient.withTenant().find().contractQuery().findAll()
            .onItem().transformToMulti(e -> Multi.createFrom().items(e.getObjects().stream()));
      }
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(
      ContractContainer contract, 
      PaymentGenConfig config, 
      ExecutorContext context) {
    
    final var minDate = contract.getContract().getContractIssueDate();
    final var firstPaymentPlan = contract.getPaymentPlans().iterator().next();
    final var firstPaymentDate = firstPaymentPlan.getPaymentPlanStartDate();
    final var lpClient = new LpClientImpl(contractClient, ledgerClient);
    
    return lpClient.actions().matchPayment()
      .addHint(contract.getContract().getId())
      .addPayment(newPayment -> {
        newPayment
          .amount(firstPaymentPlan.getPaymentPlanAmount())
          .date(firstPaymentDate)
          .externalId("001")
          .description("test payment")
          .type("NORMAL")
          .build();
      }).build()
      
      .onItem().transformToUni(ignore -> lpClient.actions().calculateAny()
          .ledgerId(firstPaymentPlan.getContractId())
          .formula(new AddPaymentFactory())
          .build())
      .onItem().transform(ignore ->  ImmutableExecutorEntity.builder()
          .status(ExecutorEntity.ExecutorEntityStatus.OK)
          .entityId("payment-" + minDate)
          .build());
  }

  @Override
  public Uni<ExecutorResult> after(PaymentGenConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor @Data
  public static class PaymentGenConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
  }
}
