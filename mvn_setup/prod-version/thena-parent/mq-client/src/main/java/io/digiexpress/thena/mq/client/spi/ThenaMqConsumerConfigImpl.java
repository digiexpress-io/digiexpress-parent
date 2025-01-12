package io.digiexpress.thena.mq.client.spi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ThenaMqConsumerConfigImpl implements ThenaMqAppConfig {
  private final String appId;
  private final Map<String, Tuple2<QueueConsumer, ThenaMqConsumer>> consumers;
  
  @Override
  public ThenaMqConsumer getConsumer(QueueConsumer consumer) {
    return consumers.get(consumer.getConsumerName()).getItem2();
  }
  
  public static ThenaMqConsumerConfigImpl from(String appId, List<Tuple2<QueueConsumer, ThenaMqConsumer>> input) {
    final var consumers = input.stream().collect(Collectors.toMap(e -> e.getItem1().getConsumerName(), e -> e));
    return new ThenaMqConsumerConfigImpl(appId, consumers);
  }

  @Override
  public String getAppId() {
    return appId;
  }
}
