package io.digiexpress.thena.mq.client.spi;

/*-
 * #%L
 * thena-mq-client
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

import java.time.OffsetDateTime;
import java.util.Optional;

import io.digiexpress.thena.mq.client.api.entities.ImmutableQueue;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelTxScope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class QueueBuilderImpl {
  private final ThenaMqChannelState state;
  private final ImmutableChannelBatch.Builder batch = ImmutableChannelBatch.builder().batchStatus(OperationStatus.OK);
  private final StringBuilder batchLog = new StringBuilder();
  
  private String queueName;
  private String comment;
  private String createdBy;
  
  public Uni<ThenaMqEnvelope<Queue>> build() {
    RepoAssert.notEmpty(comment, () -> "comment must be defined!");
    RepoAssert.notEmpty(createdBy, () -> "createdBy must be defined!");
    RepoAssert.notEmpty(queueName, () -> "queueName must be defined!");

    final var scope = ImmutableChannelTxScope.builder()
        .channelId(state.getDataSource().getChannel().getId())
        .commitAuthor(createdBy)
        .commitMessage(comment)
        .build();
    
    return state.withChannelTransaction(scope, state -> 
        state.queryQueues().findByQueueName(queueName)
        .onItem().transformToUni(input -> visitBatch(state, input))
    );
  
  }
  
  private Uni<ThenaMqEnvelope<Queue>> visitBatch(ThenaMqChannelState state, Optional<Queue> inputQueue) {
    final var queue = visitQueue(inputQueue);
    final var request = batch
      .log(batchLog.toString())
      .channelId(state.getDataSource().getChannel().getId())
      .build();
    
    return state.batchMany(request).onItem().transform(resp -> {
          
      if(resp.getBatchStatus() == OperationStatus.ERROR) {
        return ImmutableThenaMqEnvelope
            .<Queue>builder()
            .operationStatus(resp.getBatchStatus())
            .channel(state.getDataSource().getChannel())
            .channelId(state.getDataSource().getChannel().getId())
            .object(queue)
            .operationLogs(resp.getLogs())
            .build();
      }
         
      return ImmutableThenaMqEnvelope
        .<Queue>builder()
        .operationStatus(OperationStatus.OK)
        .channel(state.getDataSource().getChannel())
        .channelId(state.getDataSource().getChannel().getId())
        .object(queue)
        .build();
      
    });
  }

  private Queue visitQueue(Optional<Queue> foundQueue) {
    final var queue = foundQueue.orElseGet(() -> {
      final var newQueue = ImmutableQueue.builder()
          .id(OidUtils.gen())
          .createdAt(OffsetDateTime.now())
          .comment(comment)
          .createdBy(createdBy)
          .queueName(queueName)
          .build();
      batch.addNewQueues(newQueue);
      return newQueue;
    });
    return queue;
  }

}
