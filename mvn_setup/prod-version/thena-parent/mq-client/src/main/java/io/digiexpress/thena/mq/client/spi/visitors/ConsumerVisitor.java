package io.digiexpress.thena.mq.client.spi.visitors;

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
import java.util.Map;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.entities.ImmutableDelivery;
import io.digiexpress.thena.mq.client.api.entities.ImmutableDeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
public class ConsumerVisitor {
  private final Map<String, QueueConsumer> consumers; 
  private final Map<String, QueueMessage> messages;
  private final List<Delivery> deliveries;
  private final ThenaMqAppConfig config;
  private final Channel channel;
  private final ImmutableChannelBatch.Builder builder = ImmutableChannelBatch.builder();
  private final StringBuilder log = new StringBuilder();
  private final OffsetDateTime now = OffsetDateTime.now();
  
  public ChannelBatch accept() {
    deliveries.forEach(this::visitDelivery);
    
    
    return builder
        .channelId(channel.getId())
        .batchStatus(OperationStatus.OK)
        .log(log.toString())
        .build();
  }
  
  private void visitDelivery(Delivery delivery) {
    final var consumer = consumers.get(delivery.getConsumerId());
    final var consumerImpl = config.getConsumer(consumer);
    final var msg = messages.get(delivery.getMessageId());
    
    try {
      final var resp = consumerImpl.accept(msg);
      
      builder
        .addNewDeliveryAttempts(ImmutableDeliveryAttempt.builder()
            .id(OidUtils.gen())
            .createdAt(now)
            .deliveryId(delivery.getId())
            .consumerStatus(resp.getAck())
            .build())
        .addUpdateDeliveries(ImmutableDelivery.builder()
            .from(delivery)
            .completedAt(now)
            .status(DeliveryStatus.COMPLETED)
            .build());
    } catch(Exception e) {
      builder
        .addNewDeliveryAttempts(ImmutableDeliveryAttempt.builder()
          .id(OidUtils.gen())
          .createdAt(now)
          .deliveryId(delivery.getId())
          .consumerStatus(null)
          .consumerError(JsonObject
              .of(
                  "error", e.getMessage(),
                  "stack", e.getStackTrace()
              )
          )
          .build());
    }
  }
  
  public static ConsumerVisitorBuilder builder() {
    return new ConsumerVisitorBuilder();
  }
  
  @Setter @Accessors(fluent = true)
  public static class ConsumerVisitorBuilder {
    private List<QueueConsumer> consumers; 
    private List<QueueMessage> messages;
    private List<Delivery> deliveries;
    private ThenaMqAppConfig config;
    private Channel channel;
    
    public ConsumerVisitor build() {
      RepoAssert.notNull(consumers, () -> "consumers can't be null!");
      RepoAssert.notNull(messages, () -> "messages can't be null!");
      RepoAssert.notNull(deliveries, () -> "deliveries can't be null!");
      RepoAssert.notNull(channel, () -> "channel can't be null!");
      RepoAssert.notNull(config, () -> "config can't be null!");
      RepoAssert.notEmpty(config.getAppId(), () -> "appId can't be empty!");
      
      return new ConsumerVisitor(
          consumers.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)), 
          messages.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)), 
          deliveries, 
          config,
          channel);
    }
  }
}
