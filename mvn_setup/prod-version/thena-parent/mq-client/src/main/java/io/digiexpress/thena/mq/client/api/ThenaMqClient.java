package io.digiexpress.thena.mq.client.api;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface ThenaMqClient {

  ChannelBuilder channelBuilder();
  MessageBuilder messageBuilder();
  BindingBuilder bindingBuilder();
  ConsumerConfigBuilder consumerConfigBuilder();
  DeliveryBuilder deliveryBuilder();
  
  
  Uni<ThenaMqClient> withChannel(String channelIdOrName);
  ThenaMqClient withChannel(Channel channel);

  
  interface DeliveryBuilder {
    DeliveryBuilder config(ThenaMqConsumerConfig config);
    Uni<ThenaMqEnvelope<Delivery>> build();
  }
  
  interface BindingBuilder {
    //BindingBuilder addRouters(List<Router> routers);
    Uni<ThenaMqEnvelope<Binding>> build();
  }

  interface ChannelBuilder {
    ChannelBuilder channelName(String channelName);
    ChannelBuilder comment(String comment);
    ChannelBuilder createdBy(String createdBy);// identify who or what is creating the channel and/or queues
    ChannelBuilder externalId(String externalId);
    ChannelBuilder addQueue(Consumer<QueueBuilder> queueBuilder);
    Uni<ThenaMqEnvelope<Channel>> build();
  }

  interface QueueBuilder {
    QueueBuilder queueName(String queueName); // identify the queue
    QueueBuilder comment(String comment); // describe the queue
    Queue build();
  }
  
  interface ConsumerConfigBuilder {
    ConsumerConfigBuilder createdBy(String createdBy); // identify who or what is creating the queue
    ConsumerConfigBuilder appId(String appId); // user provided app id(how do you identify your app?)
    ConsumerConfigBuilder addConsumer(Consumer<ConsumerBuilder> worker);
    Uni<ThenaMqEnvelope<ThenaMqConsumerConfig>> build();
  }
  
  interface ConsumerBuilder {
    ConsumerBuilder comment(String comment);
    ConsumerBuilder consumerName(String consumerName);
    ConsumerBuilder routingKey(String routingKey);
    void build(ThenaMqConsumer worker);
  }
  
  
  interface MessageBuilder {
    
    /** rabbitmq matching
        * (star) can substitute for exactly one word.
        # (hash) can substitute for zero or more words.
        ReceiveLogsTopic "kern.*" "*.critical"
        ReceiveLogsTopic "*.critical"
        EmitLogTopic "kern.critical" "A critical kernel error"
     */
    MessageBuilder routingKey(String routingKey);

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
