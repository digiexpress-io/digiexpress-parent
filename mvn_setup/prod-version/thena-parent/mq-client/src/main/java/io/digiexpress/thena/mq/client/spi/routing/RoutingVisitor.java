package io.digiexpress.thena.mq.client.spi.routing;

import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.Routing.Router;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoutingVisitor {
  private final ImmutableChannelBatch.Builder batch = ImmutableChannelBatch.builder()
      .batchStatus(OperationStatus.OK)
      .log("Building channel changes with RoutingVisitor");

  
  public ChannelBatch accept(RoutingRequest request) {
    
    final var key = new RoutingKeyImpl();
    final var channel = request.getChannel();
    final var queues = request.getQueues();
    
    
    // process messages
    request.getMessage().stream().map(msg -> key.apply(channel, msg, queues));
    
    // process consumers
    request.getConsumers().stream().map(msg -> key.apply(channel, msg, queues));    

    
    return batch
        .channelId(request.getChannel().getId())
        .build();
  }
  
  
  @Value.Immutable
  public interface RoutingRequest {
    List<Router> getRouters();
    List<QueueMessage> getMessage();
    Channel getChannel();
    List<QueueConsumer> getConsumers();
    List<Queue> getQueues();
  }
  
  /*
  ChannelBatch merged = ImmutableChannelBatch.builder()
      .log("Performing binding with: " + RoutingVisitor.class + ", for: " + input.getItem1().size() + " messages")
      .batchStatus(OperationStatus.OK)
      .channelId(tx.getDataSource().getChannel().getId())
      .build();
  
  for(final var message : input.getItem1()) {
    final var request = ImmutableRoutingRequest.builder()
      .channel(tx.getDataSource().getChannel())
      .message(message)
      .addAllConsumers(input.getItem2())
      .addAllQueues(input.getItem3())
      .addAllRouters(routers)
      .build();
    
    merged = merged.merge(new RoutingVisitor().accept(request));
  }*/
  
}
