package io.digiexpress.thena.mq.client.spi;

import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.support.RepoAssert;
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
  public QueueBuilder queueBuilder() {
    RepoAssert.isTrue(state.getDataSource().isChannelLoaded(), () -> "channel must be created, call ThenaMqClient.withChannel first!");
    return new QueueBuilderImpl(state);
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
}
