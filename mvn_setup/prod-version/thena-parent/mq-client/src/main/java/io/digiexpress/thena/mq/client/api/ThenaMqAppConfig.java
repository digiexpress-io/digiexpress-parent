package io.digiexpress.thena.mq.client.api;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;

public interface ThenaMqAppConfig {
  String getAppId();
  Channel getChannel();
  ThenaMqConsumer getConsumer(QueueConsumer consumer);
}
