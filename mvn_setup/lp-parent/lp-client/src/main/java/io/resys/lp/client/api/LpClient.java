package io.resys.lp.client.api;

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
import java.util.function.Consumer;

import io.resys.lp.client.api.entities.AllocatedPayments;
import io.resys.lp.client.api.entities.AnyCalculation;
import io.resys.lp.client.api.entities.Envelope;
import io.resys.lp.client.api.entities.Fund;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.api.ThenaLedgerContainers.LedgerContainer;
import io.resys.thena.ledger.client.api.ThenaLedgerNewObject.NewPayment;
import io.smallrye.mutiny.Uni;

public interface LpClient {
  Actions actions();
  
  // add clients with given tenants
  LpClient with(ContractClient contracts, LedgerClient ledgers);

  
  interface Actions {
    CalculateAny calculateAny();
    MatchPayment matchPayment();
  }
  
  // Calculate any open matched payments
  interface CalculateAny {
    CalculateAny ledgerId(String ledgerId);
    CalculateAny targetDate(LocalDate targetDate);
    CalculateAny formula(CalculationFormula formula);
    
    // make sure to check for failures...
    Uni<Envelope<AnyCalculation>> build();
  }
  
  interface CalculationFormula {
    Uni<Envelope<AnyCalculation>> accept(FormulaContainer container);
  }
    
  interface FormulaContainer {
    ContractClient getContractClient();
    LedgerClient getLedgerClient();
    
    FundQuery getFundQuery();
    
    ContractContainer getContract();
    LedgerContainer getLedger();
    LocalDate getStartDate();
    LocalDate getToday();
  }
  
  
  interface MatchPayment {
    MatchPayment addHint(String contractIdOrRefOrEtc);
    
    // auto-match figure out if this can be matched by whatever rules....
    MatchPayment addPayment(Consumer<NewPayment> payment);
    
    // make sure to check for failures...
    Uni<Envelope<AllocatedPayments>> build();
  }
  
  interface FundQuery {
    Fund getOne(String fundIdNameOrCode, LocalDate targetDate);
  }

}
