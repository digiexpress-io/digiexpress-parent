package io.digiexpress.thena.mq.client.api.entities;

import java.util.List;


public interface Routing {  
  
  @FunctionalInterface
  interface MessageToQueueRouter {
    List<Queue> apply(
        Channel channel, 
        QueueMessage message, 
        List<Queue> queues);
  }

  @FunctionalInterface
  interface ConsumerToQueueRouter {
    List<Queue> apply(
        Channel channel, 
        QueueConsumer consumer, 
        List<Queue> queues);
  }
  
  
  interface Route {
    boolean isMatch(String routingKey);
  }
}
