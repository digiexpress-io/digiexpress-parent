package io.digiexpress.thena.mq.client.api.entities;


import java.util.Map;

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;

@Value.Immutable
public interface ThenaMqContainers {
  Map<String, Delivery> getDeliveries();    
  Map<String, DeliveryAttempt> getDeliveryAttempts();
  Map<String, Binding> getBindings();
  Map<String, PublishedMessage> getPublishedMessages();
  Map<String, Queue> getQueues();
  Map<String, Channel> getChannels();
}
