package io.digiexpress.thena.mq.client.api.persistence;

public interface ThenaMqTableRegistry {
  ChannelRegistry channel();
  QueueRegistry queue();
  MessageRegistry message();
  BindingRegistry binding();
  DeliveryRegistry delivery();
  DeliveryAttemptRegistry deliveryAttempt();
  ThenaMqTableRegistry withChannel(ThenaMqTableNames options);
}
