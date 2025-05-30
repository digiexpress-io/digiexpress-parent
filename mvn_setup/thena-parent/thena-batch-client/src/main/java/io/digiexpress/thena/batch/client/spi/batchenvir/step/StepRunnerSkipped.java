package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class StepRunnerSkipped {

  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  
  /**
   * Start the step with executing status
   */
  public Uni<RuntimeStep> accept() {
    return context.getDb()
        .withTransaction(context.getScope(), db -> startStep(db))
        .onItem().invoke(step -> {
          BatchEnvirLogger.STEP_SKIP
            .withContext(this.context)
            .addProps(config)
            .append("Created and skipped runtime instance step, because one of the previous steps returned error");
        });
  }
  
  private Uni<RuntimeStep> startStep(BatchDb tx) {
    final RuntimeStep toBeSaved = ImmutableRuntimeStep.builder()
        .id(OidUtils.gen())
        .consumerId(this.config.getBatchConsumer().getId())
        .runtimeId(this.context.getInstance().getId())
        .status(RuntimeInstance.RuntimeStatus.SKIPPED)
        .executionStatus(RuntimeExecutionStatus.OK)
        .name(this.config.getBatchConsumer().getConsumerName())
        .createdAt(OffsetDateTime.now())
        .endedAt(Optional.empty())
        .comment("created by: " + StepRunnerSkipped.class.getSimpleName())
        .build();
    
    final var container = this.context.createPersistContainer()
        .addRuntimeStepInserts(toBeSaved)
        .build();
    
    return tx.builder().persist(container).onItem().transform(ignore -> toBeSaved);
  }
}
