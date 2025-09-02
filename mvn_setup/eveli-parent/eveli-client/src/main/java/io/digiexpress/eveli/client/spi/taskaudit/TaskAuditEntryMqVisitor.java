package io.digiexpress.eveli.client.spi.taskaudit;

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

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.client.api.ImmutableTaskAuditEntryMq;
import io.digiexpress.eveli.client.api.TaskAuditClient.TaskAuditEntryMq;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class TaskAuditEntryMqVisitor {
  private final ThenaMqClient mqClientInit;
  private final ThenaMqAppConfig mqConfig;
  private final String taskId;
  private final ImmutableTaskAuditEntryMq.Builder audit = ImmutableTaskAuditEntryMq.builder();
  
  public Uni<Optional<TaskAuditEntryMq>> accept() {
    
    
    final var mqClient = mqClientInit.withChannel(mqConfig.getChannel());

    
    // find all the messages addressed to the given task 
    return mqClient.messageQuery().findAllByBodyId(taskId).collect().asList()
    .onItem().transform(messages -> {
      final Map<String, QueueMessage> messagesMap = messages.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
      audit.putAllQueueMessages(messagesMap);
      return new ArrayList<>(messagesMap.keySet());
    })
    
    // find deliveries and and binding
    .onItem().transformToUni(messageId -> 
      Uni.combine().all().unis(
        mqClient.deliveryQuery().findAllByMessageId(messageId),
        mqClient.bindingQuery().findAllByMessageId(messageId)
      ).asTuple())
    .onItem().transformToUni(tuple -> {
      audit
        .putAllDeliveries(tuple.getItem1().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .putAllBindings(tuple.getItem2().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
      
      
      // find queues and consumers
      final var queueIds = new ArrayList<String>();
      final var consumerIds = new ArrayList<String>();
      
      tuple.getItem1().stream().forEach(e -> {
        queueIds.add(e.getQueueId());
        consumerIds.add(e.getConsumerId());
      });
      
      //
      return Uni.combine().all().unis(
          mqClient.consumerQuery().findAllById(consumerIds),
          mqClient.queueQuery().findAllById(queueIds)
      ).asTuple();
    })
    .onItem().transform(tuple -> {
      return Optional.of(audit
        .putAllQueueConsumers(tuple.getItem1().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .putAllQueues(tuple.getItem2().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .build());
    });
  }
}
