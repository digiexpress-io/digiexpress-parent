package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import java.time.OffsetDateTime;

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
        .endedAt(OffsetDateTime.now())
        .build();
    
    final var container = this.context.createPersistContainer()
        .addRuntimeStepRowInserts(toBeSaved)
        .build();
    
    return tx.builder().persist(container).onItem().transform(ignore -> toBeSaved);
  }
}
