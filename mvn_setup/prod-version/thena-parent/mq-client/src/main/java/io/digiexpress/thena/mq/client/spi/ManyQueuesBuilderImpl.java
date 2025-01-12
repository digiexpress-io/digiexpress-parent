package io.digiexpress.thena.mq.client.spi;

import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.ManyQueuesBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqClient.OneQueueBuilder;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ManyQueuesBuilderImpl implements ManyQueuesBuilder {
  private final ThenaMqChannelState state;
  
  @Override
  public ManyQueuesBuilder createdBy(String createdBy) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ManyQueuesBuilder addQueue(OneQueueBuilder queueBuilder) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ThenaMqEnvelope<List<Queue>>> build() {
    // TODO Auto-generated method stub
    return null;
  }

}
