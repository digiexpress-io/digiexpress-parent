package io.digiexpress.thena.batch.client.spi.batchenvir.instance;

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
