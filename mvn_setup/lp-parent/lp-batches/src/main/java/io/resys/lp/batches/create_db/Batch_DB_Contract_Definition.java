package io.resys.lp.batches.create_db;

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

import io.digiexpress.thena.batch.client.api.BatchClient.BatchDefinition;
import io.digiexpress.thena.batch.client.api.ImmutableBatchDefinition;
import io.digiexpress.thena.batch.client.api.ImmutableBatchStepDefinition;
import io.resys.thena.contract.client.api.ContractClient;
import io.resys.thena.ledger.client.api.LedgerClient;

public class Batch_DB_Contract_Definition {

  public static BatchDefinition create(
      ContractClient contractClient,
      LedgerClient ledgerClient
      ) {
    return ImmutableBatchDefinition.builder()
        .batchName("recreate-contract-db")
        .comment("drops and creates contract db")
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("delete-contract-db")
            .comment("deletes all tables")
            .executor(new BatchJob_DROP_DB_Contract(contractClient))
            .build())
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("create-contract-db")
            .comment("create all tables")
            .executor(new BatchJob_CREATE_DB_Contract(contractClient))
            .build())
        
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("delete-ledger-db")
            .comment("create all tables")
            .executor(new BatchJob_DROP_DB_Ledger(ledgerClient))
            .build())
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("create-ledger-db")
            .comment("create all tables")
            .executor(new BatchJob_CREATE_DB_Ledger(ledgerClient))
            .build())
        .build();
  }
}
