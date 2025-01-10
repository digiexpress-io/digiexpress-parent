package io.digiexpress.thena.mq.client.spi.consumer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.ThenaMqConsumerConfig;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
public class ConsumerVisitor {
  private final Map<String, QueueConsumer> consumers; 
  private final Map<String, QueueMessage> messages;
  private final List<Delivery> deliveries;
  private final ThenaMqConsumerConfig config;
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
  }
  
  public static ConsumerVisitorBuilder builder() {
    return new ConsumerVisitorBuilder();
  }
  
  @Setter @Accessors(fluent = true)
  public static class ConsumerVisitorBuilder {
    private List<QueueConsumer> consumers; 
    private List<QueueMessage> messages;
    private List<Delivery> deliveries;
    private ThenaMqConsumerConfig config;
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
