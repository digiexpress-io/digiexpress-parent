package io.digiexpress.thena.mq.client.spi.persistence;

import io.digiexpress.thena.mq.client.api.persistence.ChannelRegistry;
import io.digiexpress.thena.mq.client.api.persistence.DeliveryAttemptRegistry;
import io.digiexpress.thena.mq.client.api.persistence.DeliveryRegistry;
import io.digiexpress.thena.mq.client.api.persistence.MessageRegistry;
import io.digiexpress.thena.mq.client.api.persistence.QueueConsumerRegistry;
import io.digiexpress.thena.mq.client.api.persistence.QueueRegistry;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableRegistry;
import io.digiexpress.thena.mq.client.sql.ChannelRegistrySqlImpl;
import io.digiexpress.thena.mq.client.sql.DeliveryAttemptRegistrySqlImpl;
import io.digiexpress.thena.mq.client.sql.DeliveryRegistrySqlImpl;
import io.digiexpress.thena.mq.client.sql.MessageRegistrySqlImpl;
import io.digiexpress.thena.mq.client.sql.QueueConsumerRegistrySqlImpl;
import io.digiexpress.thena.mq.client.sql.QueueRegistrySqlImpl;
import lombok.Getter;
import lombok.experimental.Accessors;


@Getter @Accessors(fluent = true)
public class ThenaMqTableRegistryImpl implements ThenaMqTableRegistry {
  private final ThenaMqTableNames options;
  private final ChannelRegistry channel;
  private final QueueRegistry queue;
  private final QueueConsumerRegistry queueConsumer;
  private final MessageRegistry message;
  private final DeliveryRegistry delivery;
  private final DeliveryAttemptRegistry deliveryAttempt;
  
  public ThenaMqTableRegistryImpl(ThenaMqTableNames options) {
    super();
    this.options = options;
    this.channel = new ChannelRegistrySqlImpl(options);
    this.queue = new QueueRegistrySqlImpl(options);
    this.queueConsumer = new QueueConsumerRegistrySqlImpl(options);
    this.message = new MessageRegistrySqlImpl(options);
    this.delivery = new DeliveryRegistrySqlImpl(options);
    this.deliveryAttempt = new DeliveryAttemptRegistrySqlImpl(options);
  }

  @Override
  public ThenaMqTableRegistry withChannel(ThenaMqTableNames options) {
    return new ThenaMqTableRegistryImpl(options);
  }
}
