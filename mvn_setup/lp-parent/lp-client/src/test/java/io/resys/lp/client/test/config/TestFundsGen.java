package io.resys.lp.client.test.config;

/*-
 * #%L
 * lp-client
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoField;

import io.resys.lp.product.spi.providers.Fund_Provider;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class TestFundsGen {

  private final ContractClient contractClient;
  private final LedgerClient ledgerClient;
  
  
  public Uni<Void> genFunds() {
    return contractClient.withTenant().find().contractQuery().findAll()
        .onItem().transformToUni(this::accept);
  }
  
  public Uni<Void> accept(QueryEnvelopeList<ContractContainer> contracts) {
    
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
        .commitAuthor(TestFundsGen.class.getSimpleName())
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
               .type("UNIT_PRICE")
               .value(calculateFundValue(fund.getFundId(), valueDate))
               .build();
           })
         );
    }
    
    
    return builder.build().onItem().transformToUni(ignore -> Uni.createFrom().voidItem());  
  }


  
  @Value
  private static class GenFundValue {
    String fundId;
    String fundName;
  }
  
  private BigDecimal calculateFundValue(String fundId, LocalDate date) {
    // Find fund info from Fund_Provider
    Fund_Provider.FundInfo fundInfo = null;
    for (Fund_Provider.FundInfo info : Fund_Provider.FUND_INFO) {
      if (info.getCode().equals(fundId)) {
        fundInfo = info;
        break;
      }
    }
    
    if (fundInfo == null) {
      return new BigDecimal("100.00"); // Default fallback
    }
    
    // Use same calculation logic as FundQueryImpl
    final var baseValue = fundInfo.getBaseValue();
    final var volatility = fundInfo.getVolatility();
    final var seed = date.toEpochDay() + fundId.hashCode();
    
    // Realistic price movement using sine waves
    final var random = (Math.sin(seed * 0.1) + Math.sin(seed * 0.023) + Math.sin(seed * 0.007)) / 3.0;
    final var dailyChange = volatility.multiply(BigDecimal.valueOf(random));
    final var newValue = baseValue.add(baseValue.multiply(dailyChange));
    final var minValue = baseValue.multiply(new BigDecimal("0.2"));
    
    return newValue.max(minValue).setScale(2, RoundingMode.HALF_UP);
  }

  
}
