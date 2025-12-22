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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import io.resys.thena.contract.client.spi.ContractClientImpl;
import io.resys.thena.ledger.client.spi.LedgerClientImpl;
import io.resys.thena.storesql.PgErrors;
import io.vertx.mutiny.sqlclient.Pool;

public class TestState {
  private static AtomicInteger INDEX = new AtomicInteger(1);
  private final io.vertx.mutiny.sqlclient.Pool pgPool;
  private final Duration atMost = Duration.ofMinutes(1);
  
  private ContractClientImpl client_contract;
  private LedgerClientImpl ledger_contract;
  
  
  public TestState(Pool pgPool) {
    super();
    this.pgPool = pgPool;
    
    allocate();
    create();
  }
  
  private void allocate() {
    this.client_contract = ContractClientImpl.create()
      .tenantName("contract_" + INDEX.incrementAndGet())
      .client(pgPool)
      .errorHandler(new PgErrors())
      .build();

    this.ledger_contract = LedgerClientImpl.create()
      .tenantName("ledger_" + INDEX.incrementAndGet())
      .client(pgPool)
      .errorHandler(new PgErrors())
      .build();
  }
  
  private void create() {
    this.client_contract
      .tenants().createOneTenant()
      .build()
      .await().atMost(atMost);
    this.ledger_contract
      .tenants().createOneTenant()
      .build()
      .await().atMost(atMost);
  }

  public ContractClientImpl getClientContract() {
    return client_contract;
  }

  public LedgerClientImpl getLedgerContract() {
    return ledger_contract;
  }
}
