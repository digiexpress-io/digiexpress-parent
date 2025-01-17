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

import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThenaMqClientImpl implements ThenaMqClient {
  private final ThenaMqChannelState state;
  
  @Override
  public ChannelBuilder channelBuilder() {
    return new ChannelBuilderImpl(state);
  }
  @Override
  public Uni<ThenaMqClient> withChannel(String channelIdOrName) {
    // load the channel
    return state.withChannel(channelIdOrName).onItem().transform(nextState -> new ThenaMqClientImpl(nextState));
  }
  @Override
  public ThenaMqClient withChannel(Channel channel) {
    final var nextState = state.withChannel(channel);
    return new ThenaMqClientImpl(nextState);
  }
  @Override
  public MessageBuilder messageBuilder() {
    return new MessageBuilderImpl(state);
  }
  @Override
  public BindingBuilder bindingBuilder() {
    return new BindingBuilderImpl(state);
  }
  @Override
  public DeliveryBuilder deliveryBuilder() {
    return new DeliveryBuilderImpl(state);
  }
}
