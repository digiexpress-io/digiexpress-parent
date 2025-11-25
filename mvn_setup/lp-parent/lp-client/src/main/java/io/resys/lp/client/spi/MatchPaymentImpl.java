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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.resys.lp.client.api.LpClient.MatchPayment;
import io.resys.lp.client.api.entities.AllocatedPayments;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Envelope.EnvelopeStatus;
import io.resys.lp.client.api.entities.ImmutableAllocatedPayments;
import io.resys.lp.client.api.entities.ImmutableEnvelope;
import io.resys.lp.client.api.entities.ImmutableLog;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.ThenaLedgerMergeObject.MergeLedger;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MatchPaymentImpl implements MatchPayment {
  private final ContractClient contracts;
  private final LedgerClient ledgers;
  
  private final List<Consumer<NewPayment>> payments = new ArrayList<>();
  private final List<String> hints = new ArrayList<String>();
  
  private final ImmutableAllocatedPayments.Builder allocation = ImmutableAllocatedPayments.builder();
  
  @Override
  public MatchPayment addHint(String contractIdOrSomeContractIdentifier) {
    this.hints.add(contractIdOrSomeContractIdentifier);
    return this;
  }
  @Override
  public MatchPayment addPayment(Consumer<NewPayment> payment) {
    this.payments.add(payment);
    return this;
  }
  @Override
  public Uni<Envelope<AllocatedPayments>> build() {
    return contracts.withTenant().find().contractQuery()
        .addAllContractId(hints)
        .findOne().onItem().transformToUni(env -> {
          if(env.getObjects() == null) {
            return contractNotFound();
          }
          return allocateToLedger(env.getObjects());
        });
  }
  
  
  private Uni<Envelope<AllocatedPayments>> contractNotFound() {
    return Uni.createFrom().item(ImmutableEnvelope.<AllocatedPayments>builder()
        .status(EnvelopeStatus.ERROR)
        .addLogs(ImmutableLog.builder()
            .targetId(String.join(",", hints))
            .text("No contract found!")
            .build())
        .object(allocation.build())
        .build());
  }
  
  private Uni<Envelope<AllocatedPayments>> allocateToLedger(ContractContainer contract) {
    return ledgers.withTenant().commit().modifyOneLedger()
        .commitAuthor(MatchPaymentImpl.class.getSimpleName())
        .commitMessage("Payment matching")
        .ledgerId(contract.getContract().getId())
        .modifyLedger(ledger -> {
          payments.forEach(newPayment -> appendPayment(ledger, newPayment));
          ledger.build();
        })
        .build()
        .onItem().transform(appended -> {
          return ImmutableEnvelope.<AllocatedPayments>builder()
            .status(EnvelopeStatus.OK)
            .object(allocation.build())
            .build();
        });
  }
  
  private void appendPayment(MergeLedger ledger, Consumer<NewPayment> newPayment) {
    ledger.addPayment(builder -> {
      builder.onNewState(allocated -> {
        
        // record the payment as allocated
        allocation.addMatches(allocated);
        
        // push the payment to processing as + money to account
        ledger.addMoneyRequest(request -> request
            .amount(allocated.getPaymentAmount())
            .paymentId(allocated.getId())
            .status(MoneyRequestStatus.OPEN)
            .targetDate(allocated.getPaymentDate())
            .type(MoneyRequestType.ADD_PAYMENT)
            .build()
        );
      });
      newPayment.accept(builder);
    
    });
  }
}
