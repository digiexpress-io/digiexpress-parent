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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.resys.lp.client.api.LpClient;
import io.resys.lp.client.api.LpClient.FundQuery;
import io.resys.lp.client.spi.FundQueryImpl;
import io.resys.lp.client.spi.LpClientImpl;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.test.ThenaTest;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest
public class DbTestTemplate {
  
  private TestState client;
  protected io.vertx.mutiny.sqlclient.Pool pgPool;
  protected static Duration atMost = Duration.ofMinutes(1);
  

  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) {
    this.pgPool = pgPool;
    this.client = new TestState(pgPool);
  }

  @AfterEach
  public void tearDown() {
  }
  public LpClient getLpClient() {
    return new LpClientImpl(getContractClient(), getLedgerClient(), getFundQuery());
  }
  public ContractClient getContractClient() {
    return client.getClientContract();
  }
  public LedgerClient getLedgerClient() {
    return client.getLedgerContract();    
  }
  public FundQuery getFundQuery() {
    return new FundQueryImpl();
  }
}
