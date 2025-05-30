package io.digiexpress.thena.batch.client.spi.batchenvir;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import io.digiexpress.thena.batch.client.api.BatchEnvir.BatchEnvirExecuteBuilder;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirImpl.StartedRuntimeInstance;
import io.digiexpress.thena.batch.client.spi.batchenvir.instance.InstanceRunner;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(chain = true, fluent = true)
public class BatchEnvirExecuteBuilderImpl implements BatchEnvirExecuteBuilder {
  private final Supplier<ScheduledExecutorService> threadPoolFactory;
  private final List<StartedRuntimeInstance> executing;
  private final BatchConfig config;
  private final BatchDb db;
  
  private String commitMessage;
  private String commitAuthor;
  
  
  @Override
  public Uni<RuntimeInstance> execute(RuntimeInstance runtime) {
    RepoAssert.notBlank(commitAuthor, () -> "commitAuthor must be defined!");
    RepoAssert.notBlank(commitMessage, () -> "commitMessage must be defined!");
    RepoAssert.notNull(runtime, () -> "runtime must be defined!");
    
    final var foundBatch = config.getBatches().stream()
        .filter(batch -> batch.getId().equals(runtime.getBatchId()))
        .findFirst();    
      RepoAssert.isTrue(foundBatch.isPresent(), () -> "Can't find batch for runtime instance: " + runtime.getBatchId() + "!");
      
      return db.withTenant()
          .log("batch-envir-execute/instances/" + runtime.getId())
          .onItem().transformToUni(dbState -> {

        final var txScope = ImmutableTxScope.builder()
            .tenantId(dbState.getDataSource().getTenant().getId())
            .commitAuthor(commitAuthor)
            .commitMessage(commitMessage)
            .build();
        
        final var threadPool = threadPoolFactory.get();
        final var ctx = new ExecutorContextImpl(null, foundBatch.get(), dbState, config, runtime, txScope, threadPool);
        final var visitor = new InstanceRunner(ctx);
        
        final var pointer = new StartedRuntimeInstance(ctx);
        
        return visitor.start()
            .onSubscription().invoke(sub -> executing.add(pointer))
            .onItem().transformToUni(nextRuntimeState -> visitor.visitRuntime(nextRuntimeState))
            .onItem().transformToUni(results -> visitor.end(results))
            .onTermination().invoke(() -> executing.remove(pointer));
      });
  }


}
