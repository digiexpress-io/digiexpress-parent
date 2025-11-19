package io.resys.lp.batches.gen_contracts;

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

public class Batch_Generate_Contract_Definition {

  public static BatchDefinition create(ContractClient contractClient) {
    return ImmutableBatchDefinition.builder()
        .batchName("generate-test-contracts")
        .comment("generates test data for contracts")
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("delete-proc")
            .comment("deletes all processes")
            .executor(new BatchJob_Generate_Savings_1(contractClient))
            .build())
        .build();
  }
}
