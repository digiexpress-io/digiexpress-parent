package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import java.time.OffsetDateTime;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStepRow;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity.ExecutorEntityStatus;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorEntity;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.quarkus.runtime.util.ExceptionUtil;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class StepRunnerEntityRecovery {

  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  private final RuntimeStep step;
  
  
  
  public Uni<ExecutorEntity>  accept(StepEvent event) {
    return context.getDb().withTransaction(context.getScope(), db -> persistRecovery(db, event));
  }
  
  private Uni<ExecutorEntity> persistRecovery(BatchDb tx, StepEvent event) {
    final RuntimeStepRow toBeSaved = ImmutableRuntimeStepRow.builder()
        .id(OidUtils.gen())
        .runtimeId(this.step.getRuntimeId())
        .stepId(this.step.getId())
        .executionStatus(RuntimeExecutionStatus.ERROR)
        .rowNumber(event.getEntityNumber())
        .externalId(event.getProcessed().get().getEntityId())
        .createdAt(event.getCreatedAt())
        .endedAt(OffsetDateTime.now())
        .output(JsonObject.of(
          "errorMsg", event.getThrowable().get().getMessage(),
          "errorCause", ExceptionUtil.generateStackTrace(event.getThrowable().get())
        ))
        .build();
    
    final var container = this.context.createPersistContainer()
        .addRuntimeStepRowInserts(toBeSaved)
        .build();
    
    return tx.builder().persist(container).onItem().transform(ignore -> toBeSaved)
        .onItem().transform(row -> {
          return ImmutableExecutorEntity.builder()
            .entityId("error-" + event.getEntityNumber())
            .status(ExecutorEntityStatus.ERROR)
            .build();
        });
  }
}
