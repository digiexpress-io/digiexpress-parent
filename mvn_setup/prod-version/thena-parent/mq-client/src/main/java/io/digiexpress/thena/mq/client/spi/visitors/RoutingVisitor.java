package io.digiexpress.thena.mq.client.spi.visitors;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.entities.Binding.BindingStatus;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.entities.ImmutableBinding;
import io.digiexpress.thena.mq.client.api.entities.ImmutableDelivery;
import io.digiexpress.thena.mq.client.api.entities.ImmutableQueueMessage;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.QueueMessageStatus;
import io.digiexpress.thena.mq.client.api.entities.Routing.ConsumerToQueueRouter;
import io.digiexpress.thena.mq.client.api.entities.Routing.MessageToQueueRouter;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import io.digiexpress.thena.mq.client.api.routing.Router;
import io.resys.thena.support.OidUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoutingVisitor implements  MessageToQueueRouter, ConsumerToQueueRouter {
  
  private final ImmutableChannelBatch.Builder batch = ImmutableChannelBatch.builder()
      .batchStatus(OperationStatus.OK)
      .log("Building channel changes with RoutingVisitor");
  
  private final OffsetDateTime now = OffsetDateTime.now();
  private Map<String, Queue> queueById;
  private Map<String, QueueMessage> queueMessagesById; 
  
  
  public ChannelBatch accept(RoutingRequest request) {
    batch.channelId(request.getChannel().getId());
    queueById = request.getQueues().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    queueMessagesById = request.getMessage().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));;
    
    // process messages
    request.getMessage().stream().forEach(msg -> visitMessage(request, msg));
    
    // process consumers
    request.getConsumers().stream().forEach(msg -> visitConsumers(request, msg));    

    return batch.build();
  }
  
  private void visitMessage(RoutingRequest request, QueueMessage msg) {
    final var channel = request.getChannel();
    final var matchedQueues = apply(channel, msg, request.getQueues());
    final var bindings = matchedQueues.stream().map(queue -> ImmutableBinding.builder()
        .status(BindingStatus.OPEN)
        .createdAt(now)
        .id(OidUtils.gen())
        .messageId(msg.getId())
        .queueId(queue.getId())
        .createdBy(RoutingVisitor.class.getCanonicalName())
        .comment("Created with routing RoutingVisitor")
        .build()).toList();
    
    batch
      .addUpdatePublishedMessages(ImmutableQueueMessage.builder().from(msg)
        .status(QueueMessageStatus.ROUTING_COMPLETED)
        .updatedAt(OffsetDateTime.now())
        .build())
      .addAllNewBindings(bindings);
  }
  
  private void visitConsumers(RoutingRequest request, QueueConsumer consumer) {
    final var channel = request.getChannel();

    final var currentState = this.batch.build();

    final var deliveries = currentState.getNewBindings().stream()
      .filter(b -> !apply(channel, consumer, Arrays.asList(queueById.get(b.getQueueId()))).isEmpty())
      .map(b -> {
        final var msgId = queueMessagesById.get(b.getMessageId());
        return ImmutableDelivery.builder()
          .id(OidUtils.gen())
          .messageId(b.getMessageId())
          .queueId(b.getQueueId())
          .consumerId(consumer.getId())
          .status(DeliveryStatus.OPEN)
          .createdAt(now)
          .startsAt(msgId.getStartsAt())
          .expiresAt(msgId.getExpiresAt())
          .build();
      })
      .toList();
    
    this.batch.addAllNewDeliveries(deliveries);
  }
  
  
  @Override
  public List<Queue> apply(Channel channel, QueueMessage message, List<Queue> queues) {
    return queues.stream().filter(queue -> Router.builder()
        .routingKey(message.getRoutingKey())
        .queueName(queue.getQueueName())
        .isMatch()).toList();
  }
  @Override
  public List<Queue> apply(Channel channel, QueueConsumer consumer, List<Queue> queues) {
    return queues.stream().filter(queue -> Router.builder()
        .routingKey(consumer.getRoutingKey())
        .queueName(queue.getQueueName())
        .isMatch()).toList();
  }
  
  @Value.Immutable
  public interface RoutingRequest {
    Channel getChannel();
    List<QueueMessage> getMessage();
    List<QueueConsumer> getConsumers();
    List<Queue> getQueues();
  }
}
