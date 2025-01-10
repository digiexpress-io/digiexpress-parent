package io.digiexpress.thena.mq.client.spi;

import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.BindingBuilder;
import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.QueueMessageStatus;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelTxScope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelTxScope;
import io.digiexpress.thena.mq.client.spi.routing.ImmutableRoutingRequest;
import io.digiexpress.thena.mq.client.spi.routing.RoutingVisitor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BindingBuilderImpl implements BindingBuilder {
  
  private final ThenaMqChannelState state;

  @Override
  public Uni<ThenaMqEnvelope<Binding>> build() {
    final ChannelTxScope scope = ImmutableChannelTxScope.builder()
        .channelId(state.getDataSource().getChannel().getChannelName())
        .commitAuthor("BindingBuilderImpl")
        .commitMessage("bind open messages to queues based on routing impl.")
        .build();
    
    return state.withChannelTransaction(scope, this::doInTx);
  }

  private Uni<ThenaMqEnvelope<Binding>> doInTx(ThenaMqChannelState tx) {
    return Uni.combine().all().unis(
        tx.queryMessages().findAllByStatus(QueueMessageStatus.RESOLVING_ROUTING, true),
        tx.queryQueueConsumer().findAllEnabled(),
        tx.queryQueues().findAll()    
    ).asTuple()
    .onItem().transform(input -> performRouting(input, tx))
    .onItem().transformToUni(input -> {
      return tx.batchMany(input).onItem().transform(rsp -> {
        if(rsp.getBatchStatus() == OperationStatus.ERROR) {
          return bindingFail(rsp, tx);
        }
        return bindingOk(rsp, tx);
      });
    });
  }
  
  
  private ChannelBatch performRouting(Tuple3<List<QueueMessage>, List<QueueConsumer>, List<Queue>> input, ThenaMqChannelState tx) {
    final var request = ImmutableRoutingRequest.builder()
        .channel(tx.getDataSource().getChannel())
        .message(input.getItem1())
        .addAllConsumers(input.getItem2())
        .addAllQueues(input.getItem3())
        .build();
    return new RoutingVisitor().accept(request);
  }
  
  private ThenaMqEnvelope<Binding> bindingOk(ChannelBatch rsp, ThenaMqChannelState tx) {
    return ImmutableThenaMqEnvelope
      .<Binding>builder()
      .operationStatus(rsp.getBatchStatus())
      .channel(tx.getDataSource().getChannel())
      .channelId(tx.getDataSource().getChannel().getId())
      .object(null)
      .operationLogs(rsp.getLogs())
      .build();
  }
  
  private ThenaMqEnvelope<Binding> bindingFail(ChannelBatch rsp, ThenaMqChannelState tx) {
    return ImmutableThenaMqEnvelope
      .<Binding>builder()
      .operationStatus(rsp.getBatchStatus())
      .channel(tx.getDataSource().getChannel())
      .channelId(tx.getDataSource().getChannel().getId())
      .object(null)
      .operationLogs(rsp.getLogs())
      .build();
  }
}
