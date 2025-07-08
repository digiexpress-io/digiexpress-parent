package io.digiexpress.eveli.client.spi.batch.delete_all;

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

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.thena.batch.client.api.BatchClient.BatchDefinition;
import io.digiexpress.thena.batch.client.api.ImmutableBatchDefinition;
import io.digiexpress.thena.batch.client.api.ImmutableBatchStepDefinition;

public class Batch_DeleteAll_Definition {

  public static BatchDefinition create(ProcessClient processClient, TaskClient taskClient, FeedbackClient feedback) {
    return ImmutableBatchDefinition.builder()
        .batchName("tasks-delete-all")
        .comment("clean up batch for wiping most of the 'task managment' data")
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("delete-proc")
            .comment("deletes all processes")
            .executor(new BatchJob_DeleteAll_ProcessStep(processClient))
            .build())
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("delete-tasks")
            .comment("deletes all tasks and makes and audit entry into commit logs")
            .executor(new BatchJob_DeleteAll_TaskStep(taskClient))
            .build())
        .addSteps(ImmutableBatchStepDefinition.builder()
            .name("delete-feedback")
            .comment("deletes all feedback")
            .executor(new BatchJob_DeleteAll_Feedback(feedback))
            .build())
        .build();
  }
}
