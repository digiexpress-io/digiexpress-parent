package io.digiexpress.thena.mq.client.spi;

import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.DeliveryBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelTxScope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelTxScope;
import io.digiexpress.thena.mq.client.spi.visitors.ConsumerVisitor;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class DeliveryBuilderImpl implements DeliveryBuilder {
  private final ThenaMqChannelState state;
  
  private ThenaMqAppConfig config;
  
  @Override
  public Uni<ThenaMqEnvelope<Delivery>> build() {
    RepoAssert.notNull(config, () -> "config can't be null!");
    RepoAssert.notEmpty(config.getAppId(), () -> "appId can't be empty!");
    
    final ChannelTxScope scope = ImmutableChannelTxScope.builder()
        .channelId(state.getDataSource().getChannel().getChannelName())
        .commitAuthor("DeliveryBuilderImpl")
        .commitMessage("creating deliveries and attempts")
        .build();
    
    return state.withChannelTransaction(scope, tx -> Uni.combine().all().unis(
          tx.queryQueueConsumer().findAllEnabled(config.getAppId()),
          tx.queryMessages().findAllByAppIdAndDeliveryStatus(config.getAppId(), DeliveryStatus.OPEN),
          tx.queryDeliveries().findAllByAppIdAndStatus(config.getAppId(), DeliveryStatus.OPEN, true)
      )
      .asTuple()
      .onItem().transformToUni(tuple -> request(tx, tuple))
      .onItem().transform(req -> response(req))
    );
  }

  private Uni<ChannelBatch> request(ThenaMqChannelState state, Tuple3<List<QueueConsumer>, List<QueueMessage>, List<Delivery>> input) {

    final var batch = ConsumerVisitor.builder()
      .config(config)
      .channel(state.getDataSource().getChannel())
      .deliveries(input.getItem3())
      .consumers(input.getItem1())
      .messages(input.getItem2())
      .build()
      .accept();
    return state.batchMany(batch);
  }
  
  private ThenaMqEnvelope<Delivery> response(ChannelBatch batch) {
    if(batch.getBatchStatus() == OperationStatus.ERROR) {
      return ImmutableThenaMqEnvelope.<Delivery>builder()
          .channelId(state.getDataSource().getChannel().getId())
          .channel(state.getDataSource().getChannel())
          .operationStatus(batch.getBatchStatus())
          .operationLogs(batch.getLogs())
          .build();
    }
    
    return ImmutableThenaMqEnvelope.<Delivery>builder()
        .channelId(state.getDataSource().getChannel().getId())
        .channel(state.getDataSource().getChannel())
        .operationStatus(batch.getBatchStatus())
        .operationLogs(batch.getLogs())
        .object(null)
        .build();
  }
  
}
