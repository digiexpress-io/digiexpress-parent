package io.digiexpress.thena.batch.client.spi.batchenvir.instance;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog.ExecutionLogLevel;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult.ExecutorStatus;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepRunner;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepRunnerAfter;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepRunnerBefore;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepRunnerCreated;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepRunnerFail;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepRunnerSkipped;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;




@RequiredArgsConstructor
public class InstanceRunnerConsumer {
  private final ExecutorContext context;
  private final RuntimeInstance runtime;

  
  // list is not going to be updated in parallel
  private final List<Tuple2<ExecutorResult, BatchConfigWithExecutor>> results = new ArrayList<>();
  
  // internal state
  private boolean skipAllSteps = false;
  private Uni<Tuple2<ExecutorResult, BatchConfigWithExecutor>> stream = null;
  

  /**
   * Run all the steps
   */
  public Uni<List<Tuple2<ExecutorResult, BatchConfigWithExecutor>>> accept() {
    
    // execute items in sequence, one after another(once previous is completed) and not in parallel
    for(final var config : this.context.getConfig().findAllExecutors(this.runtime.getBatchId())) {
      final Uni<Tuple2<ExecutorResult, BatchConfigWithExecutor>> next = visitNextConsumer(this.runtime, config);

      if(stream == null) {
        stream = next;
        continue;
      }
      
      stream = stream.onItem().transformToUni(previous -> {
        results.add(previous);
        
        if(previous.getItem1().getStatus() == ExecutorStatus.ERROR) {
          this.skipAllSteps = true; 
        }
        return next;
      });
      
    }

    return stream.onItem().transform(ignore -> results);
  }
  
  
  private Uni<Tuple2<ExecutorResult, BatchConfigWithExecutor>> visitNextConsumer(RuntimeInstance runtime, BatchConfigWithExecutor config) {
    
    
    return Uni.createFrom().item(() -> skipAllSteps)
      .onItem().transformToUni(skipStep -> {
        
        if(skipStep) {
          final var logentry = createConsumerSkipLog(config);
          final var skip = ImmutableExecutorResult.builder()
              .status(ExecutorResult.ExecutorStatus.SKIP)
              .addMessages(logentry)
              .build();
          
          
          return new StepRunnerSkipped(context, config).accept().onItem().transform(ignore -> Tuple2.of(skip, config));
        }
        
        return visitConsumer(runtime, config).onItem().transform(result -> Tuple2.of(result, config));
      });
  }
  
  
  private <Entity, EntityConfig> Uni<ExecutorResult> visitConsumer(RuntimeInstance runtime, BatchConfigWithExecutor config) {
    
    return new StepRunnerCreated(context, config).accept().onItem().transformToUni(step -> {
        
        final var stepRunner = new StepRunner<Entity, EntityConfig>(config, runtime, context, step);
        final var query = stepRunner.start(context);

        return query.findAll()
          .onSubscription().call(sub -> new StepRunnerBefore(context, config, step).accept())
          .onItem()
          .transformToUni((Entity entity) -> stepRunner.visitEntity(entity, query.getConfig(), context))
          
          // concurrent proc config
          .merge(
              context.getConfig().getConcurrency()
              // turn to one.... disable concurrent proc.
              //.merge(1)
          )

          .onItem().ignoreAsUni().onItem().transformToUni(ignore -> stepRunner.end(query, context))
          .onItem().call(result -> new StepRunnerAfter(context, config, step).accept())
          .onFailure().recoverWithUni((t) -> new StepRunnerFail(context, config, step, t).accept());     
      });
  }
  
  // TODO
  private ImmutableRuntimeLog createConsumerSkipLog(BatchConfigWithExecutor skipping) {
    return ImmutableRuntimeLog.builder()
      .id(OidUtils.gen())
      .runtimeId(context.getInstance().getId())
      .createdAt(OffsetDateTime.now())
      .level(ExecutionLogLevel.WARN)
      .formatType(BatchEnvirLogger.STEP_SKIP.getName())
      .format("Batch execution step skiping because one of the previous steps returned error")
      .extra(JsonObject.mapFrom(skipping.getBatchConsumer()))
      .build();
  }
}
