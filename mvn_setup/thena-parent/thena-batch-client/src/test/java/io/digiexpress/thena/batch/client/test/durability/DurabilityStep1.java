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

import java.util.concurrent.atomic.AtomicInteger;

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
import lombok.Setter;


@Setter
public class DurabilityStep1 implements Executor<DurabilityStep1.EntityForTesting, DurabilityStep1.ExecutorConfigForTesting> {


  public final AtomicInteger index = new AtomicInteger(0);
  
  private boolean blowUpInBeforeGetConfig;
  
  private boolean blowUpInBeforeFindAll;
  private boolean blowUpInInFindAll;
  private boolean blowUpInInFindAllEntity;
  
  private boolean blowUpOnSecondEntity;
  private boolean blowUpOnSecondEntityStream;
  
  private boolean blowUpInAfter;
  private boolean blowUpInAfterStream;
  
  
  public DurabilityStep1 reset() {
    blowUpInBeforeGetConfig = false;
    blowUpInBeforeFindAll = false;
    
    blowUpInInFindAll = false;
    blowUpInInFindAllEntity = false;
    
    blowUpOnSecondEntity = false;
    blowUpOnSecondEntityStream = false;
    
    blowUpInAfter = false;
    blowUpInAfterStream = false;
    
    index.set(0);
    return this;
  }
  
  
  @Override
  public ExecutorQuery<EntityForTesting, ExecutorConfigForTesting> before(ExecutorContext context) {
    blowUpIfTrue(blowUpInBeforeFindAll);
    return new ExecutorQuery<EntityForTesting, ExecutorConfigForTesting>() {
      @Override
      public ExecutorConfigForTesting getConfig() {
        blowUpIfTrue(blowUpInBeforeGetConfig);
        return new ExecutorConfigForTesting();
      }
      @Override
      public Multi<EntityForTesting> findAll() {
        blowUpIfTrue(blowUpInInFindAll);
        
        return Multi.createFrom()
            .range(1, 10)
            .onItem().transform(counter -> {
              blowUpIfTrue(blowUpInInFindAllEntity);
              final var nextId = index.incrementAndGet();
              return new EntityForTesting("entity-" + nextId, nextId);
            });
      }
      
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(EntityForTesting entity, ExecutorConfigForTesting config, ExecutorContext context) {
    blowUpIfTrue(blowUpOnSecondEntity && entity.index == 2);
    return Uni.createFrom().item(() -> {
      
      blowUpIfTrue(blowUpOnSecondEntityStream && entity.index == 2);
      
      return ImmutableExecutorEntity.builder()
        .status(ExecutorEntity.ExecutorEntityStatus.OK)
        .entityId(entity.id)
        .build();
      });
  }

  @Override
  public Uni<ExecutorResult> after(ExecutorConfigForTesting config, ExecutorContext context) {
    
    blowUpIfTrue(blowUpInAfter);
    
    return Uni.createFrom().item(() -> {
      blowUpIfTrue(blowUpInAfterStream);
        
      return ImmutableExecutorResult.builder()
      .status(ExecutorResult.ExecutorStatus.OK)
      .build();
    });
  }

  @RequiredArgsConstructor
  public static class EntityForTesting {
    private final String id;
    private final int index;
  }
  
  public static class ExecutorConfigForTesting {
    
  }
  
  public static class BlowUpTestException extends RuntimeException {
    private static final long serialVersionUID = -3372854888499246586L;
  }
  
  private void blowUpIfTrue(boolean trigger) {
    if(trigger) {
      throw new BlowUpTestException();
    }
  }
}
