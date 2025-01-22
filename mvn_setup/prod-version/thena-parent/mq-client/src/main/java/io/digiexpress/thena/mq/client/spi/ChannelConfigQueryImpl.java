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

import io.digiexpress.thena.mq.client.api.ThenaMqClient.ChannelConfigQuery;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.ChannelConfig;
import io.digiexpress.thena.mq.client.api.entities.ImmutableChannelConfig;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ChannelConfigQueryImpl implements ChannelConfigQuery {
  private final ThenaMqChannelState state;
  
  @Override
  public Uni<ThenaMqEnvelope<ChannelConfig>> getOne(String channelIdOrName) {
    return state.queryChannels()
        .getByNameOrId(channelIdOrName)
        .onItem().transformToUni(found -> found.isEmpty() ? visitChannel(found.get()) : Uni.createFrom().item(visitNotFound(channelIdOrName)));
  }

  private Uni<ThenaMqEnvelope<ChannelConfig>> visitChannel(Channel channel) {
    return Uni.combine().all().unis(
        state.withChannel(channel).queryQueueConsumer().findAll(),
        state.withChannel(channel).queryQueues().findAll())
        .asTuple()
        .onItem()
        .transform(tuple -> visitResponse(channel, tuple.getItem2(), tuple.getItem1()));
  }
  
  
  private ThenaMqEnvelope<ChannelConfig> visitResponse(Channel channel, List<Queue> queues, List<QueueConsumer> consumers) {
    return ImmutableThenaMqEnvelope.<ChannelConfig>builder()
        .channelId(channel.getId())
        .channel(channel)
        .operationStatus(ThenaMqEnvelope.OperationStatus.OK)
        .object(ImmutableChannelConfig.builder()
            .channel(channel)
            .queues(queues)
            .queueConsumers(consumers)
            .build())
        .build();
  }
  
  private ThenaMqEnvelope<ChannelConfig> visitNotFound(String channelIdOrName) {
    return ImmutableThenaMqEnvelope.<ChannelConfig>builder()
        .channelId(channelIdOrName)
        .channel(null)
        .operationStatus(ThenaMqEnvelope.OperationStatus.OK)
        .build();
  }
}
