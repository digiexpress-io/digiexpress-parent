package io.digiexpress.thena.mq.client.api.entities;

import java.util.List;


public interface Routing {  
  interface Router {}
  
  @FunctionalInterface
  interface MessageToQueueRouter extends Router {
    List<Queue> apply(
        Channel channel, 
        QueueMessage message, 
        List<Queue> queues);
  }

  @FunctionalInterface
  interface ConsumerToQueueRouter extends Router {
    List<Queue> apply(
        Channel channel, 
        QueueConsumer consumer, 
        List<Queue> queues);
  }
}
