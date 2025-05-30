package io.digiexpress.thena.batch.client.spi.batchenvir.instance;

import java.util.List;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.AllArgsConstructor;




@AllArgsConstructor
public class InstanceRunner {
  private ExecutorContext context;
  
  
  private ExecutorContext updateContext(RuntimeInstance next) {
    this.context = context.withRuntimeInstance(next);
    return this.context;
  }
  
  /**
   * Start the instance, move status to executing and log the relevant parts of technical configuration
   */
  public Uni<RuntimeInstance> start() {
    BatchEnvirLogger.INSTANCE_STARTED.withContext(context).append("Starting batch runtime");
    
    Uni<RuntimeInstance> stream;
    try { 
      stream = new InstanceRunnerBefore(context).accept()
          .onItem().invoke(instance -> updateContext(instance));
      
    } catch(Throwable t) {
      // failsafe, batch must complete, individual steps failures are irrelevant
      stream = Uni.createFrom().failure(t);
    }

    return stream
      .onFailure().retry().atMost(context.getConfig().getRetryAttempts())
        
      // Failsafe 
      .onFailure()
          .call(throwable -> new InstanceRunnerFail(context, throwable).accept()
          .onItem().invoke(instance -> updateContext(instance)))

      // start error
      .onFailure().invoke((ex) -> BatchEnvirLogger.INSTANCE_ERROR.withContext(context).cause(ex).append("Failed to start runtime instance!"));
  }

  /**
   * Run all the steps
   */
  public Uni<List<Tuple2<ExecutorResult, BatchConfigWithExecutor>>> visitRuntime(RuntimeInstance runtime) {
    Uni<List<Tuple2<ExecutorResult, BatchConfigWithExecutor>>> stream;
  
    try { 
      stream = new InstanceRunnerConsumer(context, runtime).accept();
    } catch(Throwable t) {
      // failsafe, batch must complete, individual steps failures are irrelevant
      stream = Uni.createFrom().failure(t);
    }
    
    return stream
        
      // Failsafe 
      .onFailure().call(throwable -> {
        BatchEnvirLogger.INSTANCE_ERROR
          .withContext(context)
          .cause(throwable)
          .append("Failed to start runtime instance steps!");
        return new InstanceRunnerFail(context, throwable).accept();
      });
  }
  
  /*
   * End everything
   */
  public Uni<RuntimeInstance> end(List<Tuple2<ExecutorResult, BatchConfigWithExecutor>> results) {
    Uni<RuntimeInstance> stream;
    try { 
      stream = new InstanceRunnerAfter(context, results).accept().onItem().invoke(instance -> updateContext(instance));
    } catch(Throwable t) {
      // failsafe, batch must complete, individual steps failures are irrelevant
      stream = Uni.createFrom().failure(t);
    }

    return stream.onFailure().retry().atMost(context.getConfig().getRetryAttempts())
        
      // Failsafe 
      .onFailure().call(throwable -> {
        
        return new InstanceRunnerFail(context, throwable).accept()
            .onItem().invoke(instance -> updateContext(instance))
            .onItem().invoke(ignore -> BatchEnvirLogger.INSTANCE_ERROR
                .withContext(context)
                .cause(throwable)
                .append("Failed to end runtime instance!"));
      });
  }
}
