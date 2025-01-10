package io.digiexpress.thena.mq.client.api;

import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;

public interface ThenaMqConsumerConfig {
  String getAppId();
  ThenaMqConsumer getConsumer(QueueConsumer consumer);
}
