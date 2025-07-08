package io.digiexpress.eveli.client.spi.batch.delete_all;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.spi.batch.delete_all.BatchJob_DeleteAll_ProcessStep.ProcessCleanupConfig;
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


@RequiredArgsConstructor
public class BatchJob_DeleteAll_ProcessStep implements Executor<ProcessInstance, ProcessCleanupConfig> {

  private final ProcessClient processClient;

  @Override
  public ExecutorQuery<ProcessInstance, ProcessCleanupConfig> before(ExecutorContext context) {
    return new ExecutorQuery<ProcessInstance, ProcessCleanupConfig>() {
      @Override
      public ProcessCleanupConfig getConfig() {
        return new ProcessCleanupConfig();
      }
      @Override
      public Multi<ProcessInstance> findAll() {
        return Multi.createFrom().items(processClient.queryInstances().findAll().stream());
      }
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(ProcessInstance entity, ProcessCleanupConfig config, ExecutorContext context) {
    
    // Delete process
    processClient.queryInstances().deleteOneById(entity.getId());
    
    return Uni.createFrom().item(ImmutableExecutorEntity.builder()
        .status(ExecutorEntity.ExecutorEntityStatus.OK)
        .entityId("processId: " + entity.getId().toString())
        .build());
  }

  @Override
  public Uni<ExecutorResult> after(ProcessCleanupConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  public static class ProcessCleanupConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
    
  }

}
