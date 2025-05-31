package io.digiexpress.thena.batch.client.test.durability;

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

public class DurabilityStep2 implements Executor<DurabilityStep2.EntityForTesting, DurabilityStep2.ExecutorConfigForTesting> {



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

