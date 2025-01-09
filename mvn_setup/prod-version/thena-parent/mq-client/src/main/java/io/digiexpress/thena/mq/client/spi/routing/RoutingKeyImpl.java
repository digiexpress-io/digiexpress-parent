package io.digiexpress.thena.mq.client.spi.routing;



import java.util.List;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.Routing.ConsumerToQueueRouter;
import io.digiexpress.thena.mq.client.api.entities.Routing.MessageToQueueRouter;

public class RoutingKeyImpl implements MessageToQueueRouter, ConsumerToQueueRouter {

  /*
  @Override
  public List<Queue> apply(Channel channel, QueueMessage message, List<Queue> queues, List<QueueConsumer> consumers) {
    final var msg_to_queue = queues.stream().filter(q -> isQueueMatch(q, message)).toList();
    final var queue_to_consumer = consumers.stream().filter(consumer -> isQueueMatch(message, consumer)).toList();
    
    
    final var queue = queues.stream().filter(e -> e.getId().equals(message.getQueueId())).findFirst()
        .orElseThrow(() -> {
          final var knownQueues = String.join(",", queues.stream().map(q -> q.getQueueName() + "/" + q.getId()).toList());
          return RepoAssert.fail("Can't find queue with id: '" + message.getQueueId() + "', known queues: " + knownQueues + "!");
        });
    
    return ImmutableRouting.builder()
        .addAllQueueConsumers(matches)
        .queue(queue)
        .build();
  }
*/



  @Override
  public List<Queue> apply(Channel channel, QueueMessage message, List<Queue> queues) {
    final var routingKey = message.getRoutingKey();
    
    return null;
  }



  @Override
  public List<Queue> apply(Channel channel, QueueConsumer consumer, List<Queue> queues) {
    // TODO Auto-generated method stub
    return null;
  }
  
  
}
