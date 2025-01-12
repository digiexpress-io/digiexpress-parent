package io.digiexpress.thena.mq.client.api.persistence;

public interface ThenaMqTableRegistry {
  ChannelRegistry channel();
  QueueRegistry queue();
  BindingRegistry binding();
  QueueConsumerRegistry queueConsumer();
  MessageRegistry message();
  DeliveryRegistry delivery();
  DeliveryAttemptRegistry deliveryAttempt();
  ThenaMqTableRegistry withChannel(ThenaMqTableNames options);
}
