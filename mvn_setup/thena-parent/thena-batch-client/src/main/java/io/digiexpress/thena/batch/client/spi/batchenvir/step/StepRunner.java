package io.digiexpress.thena.batch.client.spi.batchenvir.step;

import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class StepRunner<Entity, EntityConfig> {
  BroadcastProcessor<String> processor = BroadcastProcessor.create();
  ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  private final RuntimeInstance runtime;
  private final RuntimeStep step;

  private final Executor<Entity, EntityConfig> executor;
  private final AtomicLong entityNumber = new AtomicLong();
  private final ScheduledExecutorService threadPool;

  @SuppressWarnings("unchecked")
  public StepRunner(BatchConfigWithExecutor config, RuntimeInstance instance, ExecutorContext context, RuntimeStep step) {
    super();
    this.config = config;
    this.runtime = instance; 
    this.executor = (Executor<Entity, EntityConfig>) config.getExecutor();
    this.context = context;
    this.step = step;
    this.threadPool = context.getThreadPool();
  }
  
  public ExecutorQuery<Entity, EntityConfig> start(ExecutorContext mainContext) {
    try {
      
      final var start = executor.before(mainContext);
      
      processor.subscribe().withSubscriber(new MultiSubscriber<String>() {
        
        Subscription subscription;
        
        @Override
        public void onSubscribe(Subscription subscription) {
          // TODO Auto-generated method stub
          System.out.println("FFFF");
          this.subscription = subscription;
          subscription.request(Long.MAX_VALUE); // Request the first item
        }

        @Override
        public void onItem(String item) {
          // TODO Auto-generated method stub
          

          System.out.println("XXXXXXX ======================== " + item);
          //subscription.request(1);
        }

        @Override
        public void onFailure(Throwable failure) {
          // TODO Auto-generated method stub
          System.out.println("FFFF");failure.printStackTrace();
        }

        @Override
        public void onCompletion() {
          // TODO Auto-generated method stub
          System.out.println("FFFF");
        }
      });
      
      return start;
    } catch(RuntimeException e) {
      BatchEnvirLogger.STEP_ERROR
        .withContext(this.context).cause(e)
        .addProps(this.config)
        .append("Failed to start runtime instance step, error at #before stage: '{}'!", e.getMessage());
      throw e;
    }
  }
  
  public Uni<ExecutorEntity> visitEntity(Entity entity, EntityConfig config, ExecutorContext mainContext) {
    // something cancelled the processing, nobody listening down from here
    if(threadPool.isShutdown()) {
      // bye bye :)
      throw new ThreadPoolTerminatedException();
    }
    
    Uni<ExecutorEntity> start;
    try {

      start = executor.accept(entity, config, mainContext)          
          // enabled concurrent processing          
          .emitOn(threadPool)
          
          .onItem().invoke(executed -> {
            BatchEnvirLogger.STEP_ENTITY
              .withContext(mainContext)
              .addProps(this.config)
              .addProps(step)
              .append("Batch runtime step completed entity processing: {}", executed.getEntityId());
          })
          .onItem().invoke(executed -> {
            executorService.submit(() -> {
              processor.onNext(executed.getEntityId());  
            });
            
          })
 
          
          
          .onFailure().invoke(t -> {
            BatchEnvirLogger.STEP_ENTITY_ERROR
            .withContext(mainContext)
            .addProps(this.config)
            .addProps(step)
            .append("Fail to execute entity processing: {}", t.getMessage());
          });
    } catch(Throwable e) {
      start = Uni.createFrom().failure(e);
    }
      
    return start;
  }
  
  
  
  public Uni<ExecutorResult> end(ExecutorQuery<Entity, EntityConfig> query, ExecutorContext mainContext) {
    
    
    Uni<ExecutorResult> start;
    try {
      return executor.after(query.getConfig(), mainContext)
          
      .onItem().invoke(e -> {
        
        processor.onComplete();
      })
      ;
      
      
    } catch(Throwable e) {
      start = Uni.createFrom().failure(e);
    }

    return start;
  }
  
  public static class ThreadPoolTerminatedException extends RuntimeException {
    private static final long serialVersionUID = 6367466782179355177L;
    public ThreadPoolTerminatedException() {
      super("Thread pool has been terminated while processing items");
    }
    
  }

}