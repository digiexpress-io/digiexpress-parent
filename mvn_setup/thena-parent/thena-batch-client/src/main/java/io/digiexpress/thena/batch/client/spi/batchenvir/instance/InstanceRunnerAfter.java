package io.digiexpress.thena.batch.client.spi.batchenvir.instance;

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

import java.time.OffsetDateTime;
import java.util.List;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog.ExecutionLogLevel;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;




@RequiredArgsConstructor
public class InstanceRunnerAfter {
  private final ExecutorContext context;
  private final List<Tuple2<ExecutorResult, BatchConfigWithExecutor>> results;
  
  /**
   * Start the instance, move status to executing and log the relevant parts of technical configuration
   */
  public Uni<RuntimeInstance> accept() {
    return context.getDb().withTransaction(context.getScope(), db -> doInTx(db));
  }
  
  private Uni<RuntimeInstance> doInTx(BatchDb db) {
    

    return Uni.combine().all().unis(
      db.query().querySteps().findAllByInstanceId(context.getInstance().getId()), 
      db.query().queryInstances().getById(context.getInstance().getId(), true)
    ).asTuple()
    .onItem().transformToUni(tuple -> completeInstance(tuple.getItem2(), tuple.getItem1(), db)); 
  }
  
  private Uni<RuntimeInstance> completeInstance(RuntimeInstance instance, List<RuntimeStep> steps, BatchDb tx) {
    final var now = OffsetDateTime.now();
    // move the status to execution
    final var log = ImmutableRuntimeLog.builder()
      .id(OidUtils.gen())
      .runtimeId(instance.getId())
      .level(ExecutionLogLevel.INFO)
      .createdAt(now)
      .format("Completed runtime instance execution")
      .formatType("RUNTIME_COMPLETED_EXECUTION")
      .build();
    
    final var isCancelled = steps.stream().filter(t -> t.getStatus() == RuntimeStatus.CANCELLED).findFirst().isPresent();
    final var isError = steps.stream().filter(t -> t.getExecutionStatus() == RuntimeExecutionStatus.ERROR).findFirst().isPresent();
    

    
    
    final RuntimeInstance updatedInstance = ImmutableRuntimeInstance.builder().from(instance)
      .status(isCancelled ? RuntimeInstance.RuntimeStatus.CANCELLED : RuntimeInstance.RuntimeStatus.COMPLETED)
      .executionStatus((!isCancelled && isError) ? RuntimeExecutionStatus.ERROR : RuntimeExecutionStatus.OK)
      .endedAt(now)
      .build();
    
    final var toBeSaved = context.createPersistContainer()
      .addRuntimeInstanceUpdates(updatedInstance)
      .addRuntimeLogInserts(log)
      .build();
    
    return tx.builder().persist(toBeSaved).onItem().transform(ignore -> updatedInstance)
        .onItem().invoke(ignore -> {
          
          BatchEnvirLogger.INSTANCE_COMPLETED.withContext(context.withRuntimeInstance(updatedInstance))
            .append("Batch instance completed all step - {}", results.stream()
                .map(e -> e.getItem2().getBatchConsumer().getConsumerName() + "/" + e.getItem1().getStatus()).toArray());
        });
  }
}
