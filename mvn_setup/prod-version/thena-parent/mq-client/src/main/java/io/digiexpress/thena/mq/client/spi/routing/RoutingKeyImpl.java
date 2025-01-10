package io.digiexpress.thena.mq.client.spi.routing;



import java.util.List;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.Routing.ConsumerToQueueRouter;
import io.digiexpress.thena.mq.client.api.entities.Routing.MessageToQueueRouter;
import io.digiexpress.thena.mq.client.api.routing.Router;

public class RoutingKeyImpl implements MessageToQueueRouter, ConsumerToQueueRouter {

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
}
