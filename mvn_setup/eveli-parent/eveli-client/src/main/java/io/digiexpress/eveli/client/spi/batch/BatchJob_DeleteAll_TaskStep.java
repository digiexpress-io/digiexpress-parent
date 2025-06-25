package io.digiexpress.eveli.client.spi.batch;

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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.spi.batch.BatchJob_DeleteAll_TaskStep.TaskCleanupConfig;
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
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchJob_DeleteAll_TaskStep implements Executor<Task, TaskCleanupConfig> {

  private final TaskClient taskClient;

  @Override
  public ExecutorQuery<Task, TaskCleanupConfig> before(ExecutorContext context) {
    return new ExecutorQuery<Task, TaskCleanupConfig>() {
      @Override
      public TaskCleanupConfig getConfig() {
        return new TaskCleanupConfig();
      }
      @Override
      public Multi<Task> findAll() {
        return taskClient.queryTasks().findAll().onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
      }
      
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(Task entity, TaskCleanupConfig config, ExecutorContext context) {
    
    return taskClient.deleteTasks()
      .commitAuthor(BatchJob_DeleteAll_TaskStep.class.getSimpleName())
      .commitMessage("delete all ...")
      .deleteOne(entity.getId())
      .onItem().transform(pointer -> {
        
        return ImmutableExecutorEntity.builder()
          .status(ExecutorEntity.ExecutorEntityStatus.OK)
          .entityId("task ref: " + entity.getTaskRef())
          .inputBody(JsonObject.mapFrom(entity))
          .outputBody(JsonObject.mapFrom(pointer))
          .build();
      });
    
  }

  @Override
  public Uni<ExecutorResult> after(TaskCleanupConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  public static class TaskCleanupConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
    
  }

}
