package io.digiexpress.thena.batch.client.test.success;

import java.util.Arrays;

import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

public class SuccessStep2 implements Executor<SuccessStep2.EntityForTesting, SuccessStep2.ExecutorConfigForTesting> {



  @Override
  public ExecutorQuery<EntityForTesting, ExecutorConfigForTesting> before(ExecutorContext context) {
    return new ExecutorQuery<EntityForTesting, ExecutorConfigForTesting>() {
      @Override
      public ExecutorConfigForTesting getConfig() {
        return new ExecutorConfigForTesting();
      }
      @Override
      public Multi<EntityForTesting> findAll() {
        return Multi.createFrom().items(Arrays.asList(
            new EntityForTesting("entity-1"),
            new EntityForTesting("entity-2"),
            new EntityForTesting("entity-3"),
            new EntityForTesting("entity-4"),
            new EntityForTesting("entity-5")
        ).stream());
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
  
  public static class ExecutorConfigForTesting {
    
  }
}

