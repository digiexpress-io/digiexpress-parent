package io.digiexpress.thena.mq.client.api;

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
import java.util.function.Consumer;

import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.ChannelConfig;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface ThenaMqClient {

  ChannelBuilder channelBuilder();
  MessageBuilder messageBuilder();
  BindingBuilder bindingBuilder();
  DeliveryBuilder deliveryBuilder();
  
  ChannelConfigQuery channelConfigQuery();  
  MessageQuery messageQuery();
  DeliveryQuery deliveryQuery();
  
  Uni<ThenaMqClient> withChannel(String channelIdOrName);
  ThenaMqClient withChannel(Channel channel);

  
  interface MessageQuery {
    MessageQuery lastNEntries(long lastNEntriesToGet);
    Uni<List<QueueMessage>> findAll();
  }
  
  interface DeliveryQuery {
    DeliveryQuery lastNEntries(long lastNEntriesToGet);
    Uni<List<Delivery>> findAll();
  }
  
  
  interface ChannelConfigQuery {
    Uni<ThenaMqEnvelope<ChannelConfig>> getOne(String channelIdOrName);
  }
  
  interface DeliveryBuilder {
    DeliveryBuilder config(ThenaMqAppConfig config);
    Uni<ThenaMqEnvelope<Delivery>> build();
  }
  
  interface BindingBuilder {
    //BindingBuilder addRouters(List<Router> routers);
    Uni<ThenaMqEnvelope<Binding>> build();
  }

  interface ChannelBuilder {
    ChannelBuilder channelName(String channelName);
    ChannelBuilder comment(String comment);
    ChannelBuilder appId(String createdBy);// app-id, identify who or what is creating the channel and/or queues
    ChannelBuilder externalId(String externalId);
    ChannelBuilder addQueue(Consumer<QueueBuilder> queueBuilder);
    ChannelBuilder addConsumer(Consumer<ConsumerBuilder> consumerBuilder);
    Uni<ThenaMqEnvelope<ThenaMqAppConfig>> build();
  }

  interface QueueBuilder {
    QueueBuilder queueName(String queueName); // identify the queue
    QueueBuilder comment(String comment); // describe the queue
    Queue build();
  }
  
  interface ConsumerBuilder {
    ConsumerBuilder comment(String comment);
    ConsumerBuilder consumerName(String consumerName);
    ConsumerBuilder routingKey(String routingKey);
    QueueConsumer build(ThenaMqConsumer worker);
  }
  
  
  interface MessageBuilder {
    
    /** rabbitmq matching
        * (star) can substitute for exactly one word.
        # (hash) can substitute for zero or more words.
        ReceiveLogsTopic "kern.*" "*.critical"
        ReceiveLogsTopic "*.critical"
        EmitLogTopic "kern.critical" "A critical kernel error"
     */
    MessageBuilder routingKey(List<String> routingKey);
    MessageBuilder routingKey(String ...routingKey);

    MessageBuilder comment(String comment);
    MessageBuilder createdBy(String createdBy);

    MessageBuilder expiresAt(@Nullable OffsetDateTime expiresAt);
    MessageBuilder startsAt(@Nullable OffsetDateTime startsAt);
    MessageBuilder createdAt(@Nullable OffsetDateTime createdAt);

    MessageBuilder bodyId(String bodyId);
    MessageBuilder bodyType(String bodyType);
    MessageBuilder bodyValue(JsonObject bodyValue);
    
    Uni<ThenaMqEnvelope<QueueMessage>> build();
  }
  

}
