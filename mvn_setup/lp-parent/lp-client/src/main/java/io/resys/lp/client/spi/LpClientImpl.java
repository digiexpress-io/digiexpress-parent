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

import io.resys.lp.client.api.LpClient;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.ledger.client.api.LedgerClient;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class LpClientImpl implements LpClient {

  private final ContractClient contracts;
  private final LedgerClient ledgers;
  
  @Override
  public Actions actions() {
    return new Actions() {
      @Override
      public CalculatePayment calculatePayment() {
        return new AddPaymentCalculation(contracts, ledgers);
      }
      @Override
      public MatchPayment matchPayment() {
        return new MatchPaymentImpl(contracts, ledgers);
      }
    };
  }

  @Override
  public LpClient with(ContractClient contracts, LedgerClient ledgers) {
    return new LpClientImpl(contracts, ledgers);
  }
}
