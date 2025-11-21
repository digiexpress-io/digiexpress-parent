package io.resys.lp.client.spi.realcalculation;

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

import io.resys.lp.client.api.LpClient.PaymentCalculation;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Envelope.EnvelopeStatus;
import io.resys.lp.client.api.entities.ImmutableEnvelope;
import io.resys.lp.client.api.entities.ImmutableLog;
import io.resys.lp.client.spi.calc.CalculationFactory;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.LedgerCommitActions.OneLedgerEnvelope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AddPaymentCalculation implements PaymentCalculation {
  private final ContractClient contracts;
  private final LedgerClient ledgers;
  private final CalculationFactory factory;
  
  private String contractIdOrRefOrEtc;
  private LocalDate startDate;
  
  @Override
  public PaymentCalculation ledgerId(String contractIdOrRefOrEtc) {
    this.contractIdOrRefOrEtc = contractIdOrRefOrEtc;
    return this;
  }

  @Override
  public PaymentCalculation startDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  @Override
  public Uni<Envelope<AnyCalculation>> build() {
    RepoAssert.notEmpty(contractIdOrRefOrEtc, () -> "contractIdOrRefOrEtc can't be empty!");
    
    
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
              return factory.builder()
                  .contract(contract.getObjects())
                  .ledger(ledger.getObjects())
                  .startDate(startDate)
                  .incomingPayments()
                  .accept()
                  .onItem().transform(this::ledgerCalculated);
            });
      });
  }
  
  private Envelope<AnyCalculation> ledgerCalculated(OneLedgerEnvelope env) {
    return ImmutableEnvelope.<AnyCalculation>builder()
        .status(env.getStatus() == CommitResultStatus.OK ? EnvelopeStatus.OK : EnvelopeStatus.ERROR)
        .addAllLogs(env.getMessages().stream().map(e -> ImmutableLog.builder()
            .exception(e.getException())
            .text(e.getText())
            .build()).toList())
        .addLogs(ImmutableLog.builder()
            .targetId(String.join(",", contractIdOrRefOrEtc))
            .text("Ledger calculated")
            .build())
        .object(null)
        .build();
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
}