package io.digiexpress.eveli.client.spi.batch.delete_all;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.spi.batch.delete_all.BatchJob_DeleteAll_ProcessStep.ProcessCleanupConfig;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorConfig;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchJob_DeleteAll_ProcessStep implements Executor<ProcessInstance, ProcessCleanupConfig> {

  private final TaskClient taskClient;

  @Override
  public ExecutorQuery<ProcessInstance, ProcessCleanupConfig> before(ExecutorContext context) {
    return new ExecutorQuery<ProcessInstance, ProcessCleanupConfig>() {
      @Override
      public ProcessCleanupConfig getConfig() {
        return new ProcessCleanupConfig();
      }
      @Override
      public Multi<ProcessInstance> findAll() {
        return taskClient.queryTaskProcesess().findAll();
      }
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(ProcessInstance entity, ProcessCleanupConfig config, ExecutorContext context) {
    
    return taskClient.deleteProcesses()
        .commitAuthor(BatchJob_DeleteAll_ProcessStep.class.getSimpleName())
        .commitMessage("Batch clean up")
        .deleteOne(entity.getId().toString())
      .onItem().transform(ignore -> ImmutableExecutorEntity.builder()
          .status(ExecutorEntity.ExecutorEntityStatus.OK)
          .entityId("processId: " + entity.getId().toString())
          .build());    
  }

  @Override
  public Uni<ExecutorResult> after(ProcessCleanupConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  public static class ProcessCleanupConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
    
  }

}
