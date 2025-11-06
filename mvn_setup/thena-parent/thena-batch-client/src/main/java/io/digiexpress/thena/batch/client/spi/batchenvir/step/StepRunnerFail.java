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

import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog.ExecutionLogLevel;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepRunner.ThreadPoolTerminatedException;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class StepRunnerFail {

  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  private final RuntimeStep step;
  private final Throwable t;

  public Uni<ExecutorResult> accept() {
    
    BatchEnvirLogger.STEP_ERROR.withContext(this.context)
      .addProps(this.config)
      .addProps(this.step)
      .addProps(this.config)
      .cause(t)
      .append("Failed to complete batch instance step: {}!", t.getMessage());
    
    return context.getDb().withTransaction(context.getScope(), db -> doInTx(db));
  }
  
  private Uni<ExecutorResult> doInTx(BatchDb tx) {
    return tx.query().querySteps().getById(step.getId(), true).onItem()
        .transformToUni(instance -> endStep(instance, tx)); 
  }
  
  public Uni<ExecutorResult> endStep(RuntimeStep step, BatchDb db) {
    final var isCancelled = t instanceof ThreadPoolTerminatedException;
    
    
    final RuntimeStep toBeSaved = ImmutableRuntimeStep.builder().from(step)
        .status(isCancelled ? RuntimeInstance.RuntimeStatus.CANCELLED : RuntimeInstance.RuntimeStatus.COMPLETED)
        .executionStatus(RuntimeExecutionStatus.ERROR)
        .endedAt(OffsetDateTime.now())
        .build();
    
    final var logentry = createFailLog(t);
    
    final var container = this.context.createPersistContainer()
        .addRuntimeStepUpdates(toBeSaved)
        .addRuntimeLogInserts(logentry)
        .build();
    
    return db.builder().persist(container).onItem().transform(ignore -> {
      
      // delegate the error to the rest of batch steps
      final ExecutorResult recovery = ImmutableExecutorResult.builder()
          .status(ExecutorResult.ExecutorStatus.ERROR)
          .addMessages(logentry)
          .build();
        
      return recovery;
    })
    .onItem().invoke(e -> {
      BatchEnvirLogger.STEP_COMPLETED
        .withContext(this.context)
        .addProps(this.config)
        .addProps(step)
        .append("Completed batch runtime step with errors");
    })
    .onFailure().invoke(whoErrorHandlingFailedWithThrowable -> {
      BatchEnvirLogger.STEP_ERROR
      .withContext(this.context)
      .addProps(this.config)
      .cause(this.t)
      .addProps(step)
      .cause(whoErrorHandlingFailedWithThrowable)
      .append("Failed to completed batch runtime step with errors: {}, also error state save failed!", t.getMessage());
    });
  }
  
  
  private ImmutableRuntimeLog createFailLog(Throwable throwable) {
    return ImmutableRuntimeLog.builder()
      .id(OidUtils.gen())
      .runtimeId(context.getInstance().getId())
      .stepId(step.getId())
      .createdAt(OffsetDateTime.now())
      .level(ExecutionLogLevel.ERROR)
      .formatType(BatchEnvirLogger.STEP_ERROR.getName())
      .format("Batch execution step crashed")
      .stack(ExceptionUtils.getStackTrace(throwable))
      .extra(JsonObject.mapFrom(config.getBatchConsumer()))
      .build();
  }
  
}
