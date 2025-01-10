package io.digiexpress.thena.mq.client.spi;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.DeliveryBuilder;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeliveryBuilderImpl implements DeliveryBuilder {
  private final ThenaMqChannelState state;
  
  @Override
  public Uni<ThenaMqEnvelope<Delivery>> build() {
    // TODO Auto-generated method stub
    return null;
  }

}
