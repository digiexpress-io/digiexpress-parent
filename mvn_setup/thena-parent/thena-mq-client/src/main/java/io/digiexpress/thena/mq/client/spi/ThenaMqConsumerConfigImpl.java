package io.digiexpress.thena.mq.client.spi;

/*-
 * #%L
 * thena-mq-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ThenaMqConsumerConfigImpl implements ThenaMqAppConfig {
  private final String appId;
  private final Channel channel;
  private final Map<String, Tuple2<QueueConsumer, ThenaMqConsumer>> consumers;
  
  @Override
  public ThenaMqConsumer getConsumer(QueueConsumer consumer) {
    return consumers.get(consumer.getConsumerName()).getItem2();
  }
  
  public static ThenaMqConsumerConfigImpl from(String appId, Channel channel, List<Tuple2<QueueConsumer, ThenaMqConsumer>> input) {
    final var consumers = input.stream().collect(Collectors.toMap(e -> e.getItem1().getConsumerName(), e -> e));
    return new ThenaMqConsumerConfigImpl(appId, channel, consumers);
  }

  @Override
  public String getAppId() {
    return appId;
  }

  public Channel getChannel() {
    return channel;
  }
}
