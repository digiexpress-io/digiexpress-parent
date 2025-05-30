package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;

import java.time.OffsetDateTime;

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class StepRunnerAfter {

  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  private final RuntimeStep step;
  
  /**
   * Mark step completed
   */
  public Uni<RuntimeStep> accept() {
    return context.getDb()
        .withTransaction(context.getScope(), db -> doInTx(db))
        .onItem().invoke(step -> {
          BatchEnvirLogger.STEP_COMPLETED
            .withContext(this.context)
            .addProps(config)
            .addProps(step)
            .append("Completed step");
        });
  }
  
  private Uni<RuntimeStep> doInTx(BatchDb tx) {
    return tx.query().querySteps().getById(step.getId(), true).onItem()
        .transformToUni(instance -> startStep(instance, tx)); 
  }
  
  private Uni<RuntimeStep> startStep(RuntimeStep step, BatchDb tx) {
    final RuntimeStep toBeSaved = ImmutableRuntimeStep.builder().from(step)
        .status(RuntimeInstance.RuntimeStatus.COMPLETED)
        .endedAt(OffsetDateTime.now())
        .build();
    
    final var container = this.context.createPersistContainer()
        .addRuntimeStepUpdates(toBeSaved)
        .build();
    
    return tx.builder().persist(container).onItem().transform(ignore -> toBeSaved);
  }
}
