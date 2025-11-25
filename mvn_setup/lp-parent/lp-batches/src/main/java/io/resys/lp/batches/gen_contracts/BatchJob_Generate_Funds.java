package io.resys.lp.batches.gen_contracts;


import java.time.LocalDate;
import java.time.temporal.ChronoField;

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
import io.resys.lp.batches.gen_contracts.BatchJob_Generate_Funds.FundGeneratorConfig;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;



@RequiredArgsConstructor
public class BatchJob_Generate_Funds implements Executor<QueryEnvelopeList<ContractContainer>, FundGeneratorConfig> {

  private final ContractClient contractClient;
  private final LedgerClient ledgerClient;

  @Override
  public ExecutorQuery<QueryEnvelopeList<ContractContainer>, FundGeneratorConfig> before(ExecutorContext context) {
    return new ExecutorQuery<QueryEnvelopeList<ContractContainer>, FundGeneratorConfig>() {
      @Override
      public FundGeneratorConfig getConfig() {
        return new FundGeneratorConfig();
      }
      @Override
      public Multi<QueryEnvelopeList<ContractContainer>> findAll() {
        return contractClient.withTenant().find().contractQuery().findAll()
            .toMulti();
      }
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(QueryEnvelopeList<ContractContainer> contracts, FundGeneratorConfig config, ExecutorContext context) {
    
    final var minDate = contracts.getObjects().stream()
      .map(e -> e.getContract().getContractIssueDate())
      .min(LocalDate::compareTo)
      .get();
    
    final var funds = contracts.getObjects().stream()
      .flatMap(e -> e.getInvPlanAllocations().values().stream())
      .flatMap(e -> e.stream())
      .map(e -> new GenFundValue(e.getInvPlanAllocCode(), e.getInvPlanAllocName()))
      .distinct()
      .toList();
    
    final var builder = ledgerClient.withTenant().commit().createManyUnitPrices()
        .commitAuthor(BatchJob_Generate_Funds.class.getSimpleName())
        .commitMessage("Generating test funds for all contracts");
    
    final var endDate = LocalDate.now().plusYears(1);
    for(final var fund : funds) {

       minDate.datesUntil(endDate.plusDays(1)) // plusDays(1) to include endDate
         .filter(date -> date.get(ChronoField.DAY_OF_WEEK) < 6)
         .forEach(valueDate -> builder.addUnitPrice(newUnitPrice -> {
             
             newUnitPrice
               .fundId(fund.getFundId())
               .externalId(fund.getFundName())
               .date(valueDate)
               .type(null)
               .value(null)
               .build();
           })
         );
    }
    
    
    return builder.build().onItem().transform(env -> ImmutableExecutorEntity.builder()
        .status(ExecutorEntity.ExecutorEntityStatus.OK)
        .entityId("fund-(" + env.getUnitPrices().size() + ")")
        .build());    
  }

  @Override
  public Uni<ExecutorResult> after(FundGeneratorConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  @Data
  public static class FundGeneratorConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
  }
  
  
  @Value
  private static class GenFundValue {
    String fundId;
    String fundName;
  }
}
