package io.digiexpress.thena.mq.client.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface ThenaMqClient {

  QueueBuilder queueBuilder();
  ChannelBuilder channelBuilder();
  MessageBuilder messageBuilder();
  
  Uni<ThenaMqClient> withChannel(String channelIdOrName);
  ThenaMqClient withChannel(Channel channel);
  
  
  interface ChannelBuilder {
    ChannelBuilder channelName(String channelName);
    ChannelBuilder comment(String comment);
    ChannelBuilder createdBy(String createdBy);
    ChannelBuilder externalId(String externalId);
    Uni<ThenaMqEnvelope<Channel>> build();
  }
  
  interface QueueBuilder {
    QueueBuilder queueName(String queueName); // identify the queue
    QueueBuilder comment(String comment); // describe the queue
    QueueBuilder createdBy(String createdBy); // identify who or what is creating the queue
    QueueBuilder appId(String appId); // user provided app id(how do you identify your app?)
    QueueBuilder addConsumer(Consumer<ConsumerBuilder> worker);
    Uni<ThenaMqEnvelope<Queue>> build();
  }
  
  interface MessageBuilder {
    MessageBuilder appId(String appId); // user provided app id(how do you identify your app?)
    MessageBuilder queueIdOrName(String queueIdOrName);
    
    MessageBuilder routingKey(@Nullable String routingKey);
    MessageBuilder routingProps(@Nullable JsonObject routingProps);
    MessageBuilder routingTopics(@Nullable List<String> routingTopics);
    
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
  
  interface ConsumerBuilder {
    ConsumerBuilder consumerName(String consumerName);
    ConsumerBuilder routingTopics(List<String> topics);
    ConsumerBuilder routingKey(String routingKey);
    void build(ThenaMqConsumer worker);
  }
}
