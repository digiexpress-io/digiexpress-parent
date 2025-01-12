package io.digiexpress.thena.mq.client.spi;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.ChannelBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqClient.ConsumerBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqClient.QueueBuilder;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.ImmutableChannel;
import io.digiexpress.thena.mq.client.api.entities.ImmutableLog;
import io.digiexpress.thena.mq.client.api.entities.ImmutableQueue;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelTxScope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class ChannelBuilderImpl implements ChannelBuilder {
  private final ThenaMqChannelState state;
  private final ImmutableChannelBatch.Builder batch = ImmutableChannelBatch.builder().batchStatus(OperationStatus.NO_CHANGES);
  private final List<Consumer<QueueBuilder>> newQueues = new ArrayList<>();
  
  private String channelName;
  private String comment;
  private String appId;
  private String externalId;
  
  @Override
  public Uni<ThenaMqEnvelope<Channel>> build() {
    RepoAssert.notEmpty(comment, () -> "comment must be defined!");
    RepoAssert.notEmpty(appId, () -> "appId must be defined!");
    RepoAssert.notEmpty(channelName, () -> "channelName must be defined!");
    
    final var scope = ImmutableChannelTxScope.builder()
        .channelId(channelName)
        .commitAuthor(ChannelBuilderImpl.class.getCanonicalName())
        .commitMessage("Creating channels and queues")
        .build();
    
    return state.withChannelTransaction(scope, tx -> 
      Uni.combine().all().unis(
        tx.queryChannels().findAll(),
        tx.queryQueues().findAll(),
        tx.queryQueueConsumer().findAllByAppId(appId, true)
      )
      .asTuple()
      .onItem().transformToUni(input -> visitBatch(input, tx))
    );
  }

  @Override
  public ChannelBuilder addQueue(Consumer<QueueBuilder> queueBuilder) {
    newQueues.add(queueBuilder);
    return this;
  }
  

  @Override
  public ChannelBuilder addConsumer(Consumer<ConsumerBuilder> consumerBuilder) {
    // TODO Auto-generated method stub
    return null;
  }
  
  private Uni<ThenaMqEnvelope<Channel>> visitBatch(Tuple3<List<Channel>, List<Queue>, List<QueueConsumer>> input, ThenaMqChannelState tx) {
    final var channel = visitChannel(input.getItem1());
    final var queues = visitQueues(input.getItem2());
    final var queueNames = queues.stream().map(e -> e.getQueueName()).toList();
    final var request = batch.log("Batching channel: " + channel.getChannelName() + ", queues: " + String.join(",", queueNames)).build();
    if(request.getBatchStatus() == OperationStatus.NO_CHANGES) {
      final ThenaMqEnvelope<Channel> result = ImmutableThenaMqEnvelope.<Channel>builder()
          .channel(channel)
          .channelId(channel.getId())
          .object(channel)
          .operationStatus(request.getBatchStatus())
          .addAllOperationLogs(request.getLogs())
          .addAllOperationLogs(request.getLogs())
          .build();
      
      if(log.isDebugEnabled()) {
        final var allLogs = result.getOperationLogs().stream().map(e -> e.getText()).toList();
        log.debug("No changes for channels/queues:\r\n", String.join("\r\n", allLogs));
      }
      return Uni.createFrom().item(result);
    }
    
    
    return tx.batchMany(request).onItem()
        .transform(resp -> {
          final ThenaMqEnvelope<Channel> result = ImmutableThenaMqEnvelope.<Channel>builder()
              .channel(channel)
              .channelId(channel.getId())
              .object(channel)
              .operationStatus(resp.getBatchStatus())
              .addAllOperationLogs(request.getLogs())
              .addAllOperationLogs(resp.getLogs())
              .build();
          
          if(result.getOperationStatus() == OperationStatus.OK && log.isDebugEnabled()) {
            final var allLogs = result.getOperationLogs().stream().map(e -> e.getText()).toList();
            log.debug("Following channels/queues created:\r\n", String.join("\r\n", allLogs));
          }
          return result;
        })
        .onFailure().recoverWithItem(t -> {
          log.error("Failed to create create/query channel/queues because of error: {}", t.getMessage(), t);
          return ImmutableThenaMqEnvelope
              .<Channel>builder()
              .operationStatus(OperationStatus.ERROR)
              .channelId(channelName)
              .channel(null)
              .object(null)
              .addOperationLogs(ImmutableLog.builder().text(t.getMessage()).exception(t).build())
              .build();
        });
  }
  
  private Channel visitChannel(List<Channel> allChannels) {
    final Optional<Channel> existingChannel = allChannels.stream()
        .filter(channel -> channel.getChannelName().equals(channelName) || channel.getId().equals(channelName))
        .findFirst();
    
    if(existingChannel.isPresent()) {
      batch
        .addLogs(ImmutableLog.builder().text("Skipping channel creation, because it already exists.").build())
        .channelId(existingChannel.get().getId());
      return existingChannel.get();
    }
    
    final var codeName = channelName.toUpperCase();
    final var prefixStart = codeName.substring(0, Math.min(codeName.length(), 20));
    final var prefix = prefixStart.replace("-", "_") + "_" +(allChannels.size() + 10) + "_" ;
    
    final var channel = ImmutableChannel.builder()
      .id(OidUtils.gen())
      .channelName(channelName)
      .createdBy(appId)
      .externalId(externalId)
      .comment(comment)
      .createdAt(OffsetDateTime.now())
      .prefix(prefix.toUpperCase())
      .build();
    
    batch
      .batchStatus(OperationStatus.OK)
      .addLogs(ImmutableLog.builder().text("Created new channel.").build())
      .newChannel(channel)
      .channelId(channel.getId());
    
    return channel;
  }

  private List<Queue> visitQueues(List<Queue> allQueues) {
    return newQueues.stream().map(c -> {
      final var builder = new OneQueueBuilderImpl(allQueues, appId, batch);
      c.accept(builder);  
      RepoAssert.isTrue(builder.built, () -> "queue builder .build() method must be called!");
      return builder.result;
    }).toList();
  }

  @RequiredArgsConstructor
  @Setter @Accessors(fluent = true)
  private static class OneQueueBuilderImpl implements QueueBuilder {
    private final List<Queue> allQueues;
    private final String createdBy;
    private final ImmutableChannelBatch.Builder batch;
    private boolean built = false;
    private String queueName;
    private String comment;
    private Optional<Queue> existingQueue;
    private Queue result;

    @Override
    public Queue build() {
      RepoAssert.notEmpty(queueName, () -> "queueName must be defined!");
      RepoAssert.notEmpty(comment, () -> "queue comment must be defined!");
     
      this.built = true;
      this.existingQueue = allQueues.stream()
          .filter(queue -> queue.getQueueName().equals(queueName) || queue.getId().equals(queueName))
          .findFirst();
      
      if(existingQueue.isPresent()) {
        this.result = existingQueue.get();
        batch.addLogs(ImmutableLog.builder().text("Skipping queue: '" + queueName + "' creation, because it already exists.").build());
        return this.result;
      }
      
      final var newQueue = ImmutableQueue.builder()
          .id(OidUtils.gen())
          .createdAt(OffsetDateTime.now())
          .comment(comment)
          .createdBy(createdBy)
          .queueName(queueName)
          .build();
      
      this.result = newQueue;
      
      batch
        .batchStatus(OperationStatus.OK)
        .addLogs(ImmutableLog.builder().text("Created new queue: '" + queueName + "'.").build())
        .addNewQueues(newQueue);
      return newQueue;
    }
  }
}
