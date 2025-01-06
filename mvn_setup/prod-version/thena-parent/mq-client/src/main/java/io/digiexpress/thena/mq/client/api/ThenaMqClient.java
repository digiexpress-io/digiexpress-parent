package io.digiexpress.thena.mq.client.api;

import java.util.List;
import java.util.function.Consumer;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.smallrye.mutiny.Uni;

public interface ThenaMqClient {

  QueueBuilder queueBuilder();
  ChannelBuilder channelBuilder();
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
    QueueBuilder queueName(String queueName);
    QueueBuilder comment(String comment);
    QueueBuilder createdBy(String createdBy);
    QueueBuilder appId(String appId);
    QueueBuilder addConsumer(Consumer<ConsumerBuilder> worker);
    Uni<ThenaMqEnvelope<Queue>> build();
  }
  
  interface ProducerBuilder {
    ProducerBuilder producerName(String producerName);  
  }
  interface ConsumerBuilder {
    ConsumerBuilder consumerName(String consumerName);
    ConsumerBuilder routingTopics(List<String> topics);
    ConsumerBuilder routingKey(String routingKey);
    void build(ThenaMqConsumer worker);
  }
}
