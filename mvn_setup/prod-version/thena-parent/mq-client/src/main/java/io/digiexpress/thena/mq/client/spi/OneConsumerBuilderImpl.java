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
import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.ConsumerBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.digiexpress.thena.mq.client.api.entities.ImmutableLog;
import io.digiexpress.thena.mq.client.api.entities.ImmutableQueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer.QueueConsumerStatus;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Getter @Accessors(fluent = true)
public class OneConsumerBuilderImpl implements ConsumerBuilder {
  
  private final List<QueueConsumer> allConsumers;
  private final String appId;
  private final ImmutableChannelBatch.Builder batch;


  private String comment;
  private String consumerName;
  private String routingKey;
  private ThenaMqConsumer worker;
  private QueueConsumer result;
  private boolean built = false;
  
  @Override
  public QueueConsumer build(ThenaMqConsumer worker) {
    RepoAssert.notNull(worker, () -> "worker must be defined!");
    RepoAssert.notEmpty(consumerName, () -> "consumerName must be defined!");
    RepoAssert.notEmpty(comment, () -> "comment must be defined!");
    
    this.built = true;
    this.worker = worker;
    
    final var existingConsumer = allConsumers.stream()
        .filter(queue -> queue.getConsumerName().equals(consumerName) || queue.getId().equals(consumerName))
        .findFirst();


    this.result = existingConsumer.isPresent() ? 
        visitExistigQueueConsumers(existingConsumer.get()) :
        visitNewQueueConsumers();
    return this.result;
  }
  
  // enable and update existing consumer
  private QueueConsumer visitExistigQueueConsumers(QueueConsumer prev) {
    final var nextState = ImmutableQueueConsumer.builder().from(prev)
        .consumerStatus(QueueConsumerStatus.ENABLED)
        .qualifiedJavaName(worker.getClass().getPackageName() + "." + worker.getClass().getName())
        .routingKey(routingKey)
        .build();
    
    if(nextState.equals(ImmutableQueueConsumer.builder().from(prev).build())) {
      this.batch
        .addLogs(ImmutableLog.builder().text("Consumer: '" + consumerName + "' has no changes.").build());
      return prev;
    }
    
    final var result = nextState.withUpdatedAt(OffsetDateTime.now());
    this.batch
      .batchStatus(OperationStatus.OK)
      .addUpdateQueueConsumer(result)
      .addLogs(ImmutableLog.builder().text("Updating existing consumer: '" + consumerName + "'.").build());
    return result;
  }

  
  // create new queue
  private QueueConsumer visitNewQueueConsumers() {
    final var nextState = ImmutableQueueConsumer.builder()
        .id(OidUtils.gen())
        .comment(this.comment)
        .consumerStatus(QueueConsumerStatus.ENABLED)
        .qualifiedJavaName(worker.getClass().getPackageName() + "." + worker.getClass().getName())
        .routingKey(this.routingKey)
        .createdAt(OffsetDateTime.now())
        .consumerName(this.consumerName)
        .appId(appId)
        .build();
    this.batch
      .batchStatus(OperationStatus.OK)
      .addLogs(ImmutableLog.builder().text("Adding new consumer: '" + consumerName + "'.").build())
      .addNewQueueConsumer(nextState);
    return nextState;
  }
}
