package io.digiexpress.eveli.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;

/*-
 * #%L
 * eveli-client
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.config.EveliAutoConfigBatchesDev.BatchTenantCondition;
import io.digiexpress.eveli.client.spi.batch.delete_all.Batch_DeleteAll_Definition;
import io.digiexpress.eveli.client.spi.tenant.TenantConfigClientProps;
import io.digiexpress.thena.batch.client.api.BatchClient.BatchDefinition;
import io.resys.lp.batches.create_db.Batch_DB_Contract_Definition;
import io.resys.lp.batches.gen_contracts.Batch_Generate_Contract_Definition;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.ledger.client.api.LedgerClient;
import lombok.extern.slf4j.Slf4j;



@Configuration
@org.springframework.context.annotation.Conditional(BatchTenantCondition.class)
@Slf4j
public class EveliAutoConfigBatchesDev {
  
  
  static class BatchTenantCondition extends EveliTenantCondition {
    public BatchTenantCondition() {
      super(TenantConfigClientProps.BATCHES_DEV);
    }
  }

  @Bean
  public BatchDefinition tasksCleanUpJob(ProcessClient processClient, TaskClient taskClient, FeedbackClient feedback) {
    return Batch_DeleteAll_Definition.create(processClient, taskClient, feedback);
  }
  
  @Bean
  @ConditionalOnBooleanProperty(matchIfMissing = false, havingValue = true, prefix = EveliPropsContract.PREFIX, name = "enabled")
  public BatchDefinition generateContractJob(ContractClient contractClient, LedgerClient ledgerClient) {
    return Batch_Generate_Contract_Definition.create(contractClient, ledgerClient);
  } 
  
  @Bean
  @ConditionalOnBooleanProperty(matchIfMissing = false, havingValue = true, prefix = EveliPropsContract.PREFIX, name = "enabled")
  public BatchDefinition recreateContractDbJob(ContractClient contractClient, LedgerClient ledgerClient) {
    return Batch_DB_Contract_Definition.create(contractClient, ledgerClient);
  } 
  
  
}
