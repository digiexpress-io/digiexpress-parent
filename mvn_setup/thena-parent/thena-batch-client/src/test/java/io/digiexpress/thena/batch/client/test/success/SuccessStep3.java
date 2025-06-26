package io.digiexpress.thena.batch.client.test.success;

import java.util.Collections;

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

public class SuccessStep3 implements Executor<SuccessStep3.EntityForTesting, SuccessStep3.ExecutorConfigForTesting> {



  @Override
  public ExecutorQuery<EntityForTesting, ExecutorConfigForTesting> before(ExecutorContext context) {
    return new ExecutorQuery<EntityForTesting, ExecutorConfigForTesting>() {
      @Override
      public ExecutorConfigForTesting getConfig() {
        return new ExecutorConfigForTesting();
      }
      @Override
      public Multi<EntityForTesting> findAll() {
        return Multi.createFrom().items(Collections.<EntityForTesting>emptyList().stream());
      }
      
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(EntityForTesting entity, ExecutorConfigForTesting config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorEntity.builder()
        .status(ExecutorEntity.ExecutorEntityStatus.OK)
        .entityId(entity.id)
        .build());
  }

  @Override
  public Uni<ExecutorResult> after(ExecutorConfigForTesting config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }
  
  

  
  @RequiredArgsConstructor
  public static class EntityForTesting {
    private final String id;
  }
  
  public static class ExecutorConfigForTesting implements ExecutorConfig {

    private static final long serialVersionUID = -6531976819651956797L;
    
  }
}

