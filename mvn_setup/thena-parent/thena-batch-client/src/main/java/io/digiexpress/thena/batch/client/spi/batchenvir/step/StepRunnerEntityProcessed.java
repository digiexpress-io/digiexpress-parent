package io.digiexpress.thena.batch.client.spi.batchenvir.step;

/*-
 * #%L
 * thena-batch-client
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

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStepRow;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class StepRunnerEntityProcessed {

  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  private final RuntimeStep step;
  
  public Uni<Void> accept(StepEvent event) {
    return context.getDb().withTransaction(context.getScope(), db -> persistSuccess(db, event))
        .onItem().transformToUni((ignore) -> Uni.createFrom().voidItem());
  }
  
  private Uni<RuntimeStepRow> persistSuccess(BatchDb tx, StepEvent event) {
    final RuntimeStepRow toBeSaved = ImmutableRuntimeStepRow.builder()
        .id(OidUtils.gen())
        .runtimeId(this.step.getRuntimeId())
        .stepId(this.step.getId())
        .executionStatus(RuntimeExecutionStatus.OK)
        .rowNumber(event.getEntityNumber())
        .externalId(event.getProcessed().get().getEntityId())
        .createdAt(event.getCreatedAt())
        .endedAt(event.getEndedAt())
        .comment(event.getComment())
        .build();
    
    final var container = this.context.createPersistContainer()
        .addRuntimeStepRowInserts(toBeSaved)
        .build();
    
    return tx.builder().persist(container).onItem().transform(ignore -> toBeSaved);
  }
}
