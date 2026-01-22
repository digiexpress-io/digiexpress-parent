package io.digiexpress.eveli.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.web.resources.worker.ContractApiController;
import io.digiexpress.eveli.client.web.resources.worker.LedgerApiController;
import io.resys.thena.api.ThenaAware;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.contract.client.spi.ContractClientImpl;
import io.resys.thena.ledger.client.api.LedgerClient;
import io.resys.thena.ledger.client.spi.LedgerClientImpl;
import io.resys.thena.storesql.PgErrors;
import io.vertx.mutiny.sqlclient.Pool;



@Configuration
@ConditionalOnBooleanProperty(matchIfMissing = false, havingValue = true, prefix = EveliPropsContract.PREFIX, name = "enabled")
public class EveliAutoConfigContract {
  
  @Bean
  public ContractClient contractClient(Pool pgPool, ThenaAware thenaAware) {
    final var contract = ContractClientImpl.create()
      .errorHandler(new PgErrors())
      .client(pgPool)
      .tenantName("INSURANCE")
      .build();
    
    thenaAware.register(contract.getClass(), contract
      .tenants().createOneTenant()
      .name(contract.getTenantName())
      .build());

    return contract;
  }
  
  @Bean
  public LedgerClient ledgerClient(Pool pgPool, ThenaAware thenaAware) {
    final var ledger = LedgerClientImpl.create()
      .errorHandler(new PgErrors())
      .client(pgPool)
      .tenantName("SAVINGS")
      .build();
    
    thenaAware.register(ledger.getClass(), ledger
        .tenants().createOneTenant()
        .name(ledger.getTenantName())
        .build());
    return ledger;
  }

  @Bean
  public ContractApiController contractApiController(ContractClient contractClient) {
    return new ContractApiController(contractClient);
  }
  
  @Bean
  public LedgerApiController ledgerApiController(LedgerClient ledgerClient, ContractClient contractClient) {
    return new LedgerApiController(ledgerClient, contractClient);
  }
}
