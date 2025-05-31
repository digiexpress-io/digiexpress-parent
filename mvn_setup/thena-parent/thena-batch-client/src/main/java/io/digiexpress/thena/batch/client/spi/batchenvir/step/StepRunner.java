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
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class StepRunner<Entity, EntityConfig> {
  private final BroadcastProcessor<StepEvent> processor = BroadcastProcessor.create();
  private final ScheduledExecutorService executorService;


  private final BatchConfigWithExecutor config;
  private final RuntimeStep step;

  private final Executor<Entity, EntityConfig> executor;
  private final AtomicLong entityNumber = new AtomicLong();
  private final ScheduledExecutorService threadPool;

  @SuppressWarnings("unchecked")
  public StepRunner(BatchConfigWithExecutor config, ExecutorContext context, RuntimeStep step) {
    super();
    this.config = config;
    this.executor = (Executor<Entity, EntityConfig>) config.getExecutor();
    this.step = step;
    this.threadPool = context.getThreadPool();
    this.executorService = Executors.newScheduledThreadPool(context.getConfig().getEventThreads());
  }
  
  public ExecutorQuery<Entity, EntityConfig> start(ExecutorContext mainContext) {
    try {
      
      final var start = executor.before(mainContext);
      processor.subscribe().withSubscriber(new StepEventSubscriber(mainContext, config, step));
      return start;
    } catch(RuntimeException e) {
      BatchEnvirLogger.STEP_ERROR
        .withContext(mainContext).cause(e)
        .addProps(this.config)
        .append("Failed to start runtime instance step, error at #before stage: '{}'!", e.getMessage());
      throw e;
    }
  }
  
  public Uni<ExecutorEntity> visitEntity(Entity entity, EntityConfig config, ExecutorContext mainContext) {
    // something cancelled the processing, nobody listening down from here
    if(threadPool.isShutdown()) {
      // pull the plug on the events
      executorService.shutdownNow();
      
      // bye bye :)
      throw new ThreadPoolTerminatedException();
    }
    
    
    final var entityNumber = this.entityNumber.incrementAndGet();
    
    final var event = StepEvent.builder()
      .processed(Optional.empty())
      .throwable(Optional.empty())
      .entity(entity)
      .entityConfig(config)
      .entityNumber(entityNumber)
      .createdAt(OffsetDateTime.now());
    
    
    
    return executor.accept(entity, config, mainContext)          
      // enabled concurrent processing          
      .emitOn(threadPool)
  
      // just log of processed event
      .onItem().invoke(executed -> {
        BatchEnvirLogger.STEP_ENTITY
          .withContext(mainContext)
          .addProps(this.config)
          .addProps(step)
          .append(
              "Batch runtime step completed entity no: {} processing id: {}", 
                  entityNumber, executed.getEntityId());
          })
          
          .onItem().call(processed -> onSuccess(event.endedAt(OffsetDateTime.now()).processed(Optional.of(processed)).build(), mainContext))
 
          // Failsafe on the stream
      .onFailure().invoke(t -> {
        BatchEnvirLogger.STEP_ENTITY_ERROR
        .withContext(mainContext)
        .addProps(this.config)
        .addProps(step)
        .append("Step failed to process entity no: {}, message: {}", entityNumber, t.getMessage());
      })
      .onFailure().recoverWithUni(processed -> onFailureRecover(event.endedAt(OffsetDateTime.now()).throwable(Optional.of(processed)).build(), mainContext));
  }
  
  
  private Uni<Void> onSuccess(StepEvent event, ExecutorContext mainContext) {
    
    // send to to event management
    executorService.submit(() -> processor.onNext(event));
  
    // persist processed row
    return new StepRunnerEntityProcessed(mainContext, this.config, this.step).accept(event);
  }
  
  private Uni<ExecutorEntity> onFailureRecover(StepEvent event, ExecutorContext mainContext) {
    
    // send to to event management
    executorService.submit(() -> processor.onNext(event));
    
    // Recover from failure
    return new StepRunnerEntityRecovery(mainContext, this.config, this.step).accept(event);
  }
  
  
  public Uni<ExecutorResult> end(ExecutorQuery<Entity, EntityConfig> query, ExecutorContext mainContext) {
    Uni<ExecutorResult> start;
    try {
      
      return executor
          .after(query.getConfig(), mainContext)
          .onItem().invoke(e -> processor.onComplete());
    } catch(Throwable e) {
      start = Uni.createFrom().failure(e);
    }

    return start;
  }
  
  public void close(ExecutorContext context) {
    try {
      // don't do this... executorService.shutdown();
      // don't do this... threadPool.shutdownNow();
    } catch(Throwable t) {
      BatchEnvirLogger.STEP_ERROR
      .withContext(context)
      .addProps(config)
      .addProps(step)
      .append("Step failed to shutdown workers, message: {}", entityNumber, t.getMessage());
    }
  }
  
  public static class ThreadPoolTerminatedException extends RuntimeException {
    private static final long serialVersionUID = 6367466782179355177L;
    public ThreadPoolTerminatedException() {
      super("Thread pool has been terminated while processing items");
    }
  }

}
