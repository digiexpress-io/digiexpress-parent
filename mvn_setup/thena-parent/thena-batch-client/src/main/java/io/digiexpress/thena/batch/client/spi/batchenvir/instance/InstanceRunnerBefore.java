package io.digiexpress.thena.batch.client.spi.batchenvir.instance;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog.ExecutionLogLevel;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;




@RequiredArgsConstructor
public class InstanceRunnerBefore {

  private final ExecutorContext context;
  
  /**
   * Start the instance, move status to executing and log the relevant parts of technical configuration
   */
  public Uni<RuntimeInstance> accept() {
    return context.getDb().withTransaction(context.getScope(), db -> doInTx(db));
  }
  
  private Uni<RuntimeInstance> doInTx(BatchDb tx) {
    return tx.query().queryInstances().getById(context.getInstance().getId(), true).onItem()
        .transformToUni(instance -> startInstance(instance, tx)); 
  }
  
  
  private Uni<RuntimeInstance> startInstance(RuntimeInstance instance, BatchDb tx) {
    final var config = context.getConfig();
    final var args = config.findAllExecutors(context.getInstance().getBatchId()).stream()
        .map((BatchConfigWithExecutor p) -> 
          JsonObject.of(
            "consumerName", p.getBatchConsumer().getConsumerName(),
            "consumerJavaName", p.getBatchConsumer().getQualifiedJavaName()
          )
        ).toList();

    // move the status to execution
    final var log = ImmutableRuntimeLog.builder()
        .id(OidUtils.gen())
        .runtimeId(instance.getId())
        .level(ExecutionLogLevel.INFO)
        .createdAt(OffsetDateTime.now())
        .format("Started runtime instance execution")
        .formatType("RUNTIME_START_EXECUTION")
        .extra(JsonObject.of(
            "commitMessage", context.getScope().getCommitMessage(),
            "commitAuthor", context.getScope().getCommitAuthor(),
            "concurrency", config.getConcurrency(),
            "retryAttempts", config.getRetryAttempts(),
            "consumers", args
          ))
        .build();
    
    final RuntimeInstance updatedInstance = ImmutableRuntimeInstance.builder().from(instance)
        .status(RuntimeInstance.RuntimeStatus.EXECUTING)
        .executionStatus(RuntimeExecutionStatus.OK)
        .endedAt(Optional.empty())
        .build();
    
    final var toBeSaved = context.createPersistContainer()
        .addRuntimeInstanceUpdates(updatedInstance)
        .addRuntimeLogInserts(log)
        .build();
    
    return tx.builder().persist(toBeSaved).onItem().transform(ignore -> updatedInstance);
  }
  
  

}
