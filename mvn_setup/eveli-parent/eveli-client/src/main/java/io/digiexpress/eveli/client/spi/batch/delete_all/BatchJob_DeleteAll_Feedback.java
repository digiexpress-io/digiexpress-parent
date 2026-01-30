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

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.ImmutableDeleteReplyCommand;
import io.digiexpress.eveli.client.spi.batch.delete_all.BatchJob_DeleteAll_Feedback.FeedbackCleanupConfig;
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
public class BatchJob_DeleteAll_Feedback implements Executor<Feedback, FeedbackCleanupConfig> {

  private final FeedbackClient client;

  @Override
  public ExecutorQuery<Feedback, FeedbackCleanupConfig> before(ExecutorContext context) {
    return new ExecutorQuery<Feedback, FeedbackCleanupConfig>() {
      @Override
      public FeedbackCleanupConfig getConfig() {
        return new FeedbackCleanupConfig();
      }
      @Override
      public Multi<Feedback> findAll() {
        return client.queryFeedbacks().findAll();
      }
      
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(Feedback entity, FeedbackCleanupConfig config, ExecutorContext context) {
    
    final var command = ImmutableDeleteReplyCommand.builder()
        .addReplyIds(entity.getId())
        .build();
    return client.queryFeedbacks().deleteAll(command, BatchJob_DeleteAll_Feedback.class.getSimpleName())
      .collect().asList().map(e -> ImmutableExecutorEntity.builder()  
        .status(ExecutorEntity.ExecutorEntityStatus.OK)
        .entityId("feedback id: " + entity.getId())
        .inputBody(JsonObject.mapFrom(entity))
        .build());
    
  }

  @Override
  public Uni<ExecutorResult> after(FeedbackCleanupConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  public static class FeedbackCleanupConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
    
  }

}
