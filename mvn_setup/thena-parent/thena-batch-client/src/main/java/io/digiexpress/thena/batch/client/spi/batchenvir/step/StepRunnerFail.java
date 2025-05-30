package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import java.time.OffsetDateTime;

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
import io.quarkus.runtime.util.ExceptionUtil;
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
      .stack(ExceptionUtil.generateStackTrace(throwable))
      .extra(JsonObject.mapFrom(config.getBatchConsumer()))
      .build();
  }
  
}