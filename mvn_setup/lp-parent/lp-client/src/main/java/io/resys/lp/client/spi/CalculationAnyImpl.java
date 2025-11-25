package io.resys.lp.client.spi;

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

import java.time.LocalDate;

import io.resys.lp.client.api.LpClient.CalculateAny;
import io.resys.lp.client.api.LpClient.CalculationFormula;
import io.resys.lp.client.api.LpClient.FormulaContainer;
import io.resys.lp.client.api.LpClient.FundQuery;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Envelope.EnvelopeStatus;
import io.resys.lp.client.api.entities.ImmutableEnvelope;
import io.resys.lp.client.api.entities.ImmutableLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CalculationAnyImpl implements CalculateAny {
  private final ContractClient contracts;
  private final LedgerClient ledgers;
  private final FundQuery fundQuery;
  
  private String contractIdOrRefOrEtc;
  private LocalDate targetDate;
  private CalculationFormula formula;
  
  @Override
  public CalculateAny ledgerId(String ledgerId) {
    this.contractIdOrRefOrEtc = ledgerId;
    return this;
  }
  @Override
  public CalculateAny targetDate(LocalDate targetDate) {
    this.targetDate = targetDate;
    return this;
  }
  @Override
  public CalculateAny formula(CalculationFormula formula) {
    this.formula = formula;
    return this;
  } 
  @Override
  public Uni<Envelope<AnyCalculation>> build() {
    RepoAssert.notEmpty(contractIdOrRefOrEtc, () -> "ledgerId can't be empty!");
    RepoAssert.notNull(formula, () -> "formula can't be null!");
    
    
    return contracts.withTenant().find().contractQuery()
      .addContractId(contractIdOrRefOrEtc)
      .findOne()
      .onItem().transformToUni(contract -> {
        if(contract.getObjects() == null) {
          return notFoundError(contract, "Contract not found!");
        }
        
        final var contractId = contract.getObjects().getContract().getId();
        
        return ledgers.withTenant().find()
            .ledgerQuery()
            .addLedgerId(contractId)
            .findOne()
            .onItem().transformToUni(ledger -> {
              if(ledger.getObjects() == null) {
                return notFoundError(contract, "Ledger not found!");
              }
              
              final var targetDate = this.targetDate == null ? LocalDate.now() : this.targetDate;
              
              final var container = FormulaContainerImpl.builder()
                .startDate(targetDate)
                .today(targetDate)
                .contract(contract.getObjects())
                .contractClient(contracts)
                .ledger(ledger.getObjects())
                .ledgerClient(ledgers)
                .fundQuery(fundQuery)
                .build();
              
              return formula.accept(container);
            });
      });
  }
  
  private Uni<Envelope<AnyCalculation>> notFoundError(QueryEnvelope<?> env, String message) {
    return Uni.createFrom().item(ImmutableEnvelope.<AnyCalculation>builder()
        .status(EnvelopeStatus.ERROR)
        .addAllLogs(env.getMessages().stream().map(e -> ImmutableLog.builder()
            .exception(e.getException())
            .text(e.getText())
            .build()).toList())
        .addLogs(ImmutableLog.builder()
            .targetId(String.join(",", contractIdOrRefOrEtc))
            .text(message)
            .build())
        .object(null)
        .build());
  }


  @Data @Builder
  private static class FormulaContainerImpl implements FormulaContainer {
    private final ContractClient contractClient;
    private final LedgerClient ledgerClient;
    private final FundQuery fundQuery;
    
    private final ContractContainer contract;
    private final LedgerContainer ledger;
    private final LocalDate startDate;
    private final LocalDate today;

  }
}