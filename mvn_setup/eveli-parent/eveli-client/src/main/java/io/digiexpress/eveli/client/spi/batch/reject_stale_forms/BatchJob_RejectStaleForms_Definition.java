package io.digiexpress.eveli.client.spi.batch.reject_stale_forms;

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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.thena.batch.client.api.BatchClient.BatchDefinition;
import io.digiexpress.thena.batch.client.api.ImmutableBatchDefinition;
import io.digiexpress.thena.batch.client.api.ImmutableBatchStepDefinition;

public class BatchJob_RejectStaleForms_Definition {

  public static BatchDefinition create(TaskClient taskClient, DialobClient dialobClient) {
    return ImmutableBatchDefinition.builder()
        .batchName("stale-data-clean-up")
        .comment("Cleanes up data oleder then 6 months")
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("reject stale questionnaires")
            .comment("Mark all stale proc-s to rejected state")
            .executor(new BatchJob_RejectStaleForms_ProcessInstance(taskClient, dialobClient))
            .build())
        .build();
  }
}
