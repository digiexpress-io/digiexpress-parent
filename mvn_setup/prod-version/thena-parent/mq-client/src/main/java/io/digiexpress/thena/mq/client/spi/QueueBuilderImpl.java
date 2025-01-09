package io.digiexpress.thena.mq.client.spi;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.ConsumerBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqClient.QueueBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqConsumer;
import io.digiexpress.thena.mq.client.api.entities.ImmutableQueue;
import io.digiexpress.thena.mq.client.api.entities.ImmutableQueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer.QueueConsumerStatus;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelTxScope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class QueueBuilderImpl implements QueueBuilder {
  private final ThenaMqChannelState state;
  private final ImmutableChannelBatch.Builder batch = ImmutableChannelBatch.builder().batchStatus(OperationStatus.OK);
  private final StringBuilder batchLog = new StringBuilder();
  
  private String appId;
  private String queueName;
  private String comment;
  private String createdBy;
  private final Map<String, ConsumerBuilderImpl> newConsumers = new HashMap<>();
  
  @Override
  public QueueBuilder addConsumer(Consumer<ConsumerBuilder> worker) {
    final var consumerBuilder = new ConsumerBuilderImpl();
    worker.accept(consumerBuilder);
    RepoAssert.isTrue(consumerBuilder.completed().isTrue(), () -> "consumer added but #build method not called!");
    newConsumers.put(consumerBuilder.consumerName(), consumerBuilder);
    return this;
  }

  @Override
  public Uni<ThenaMqEnvelope<Queue>> build() {
    RepoAssert.notEmpty(comment, () -> "comment must be defined!");
    RepoAssert.notEmpty(createdBy, () -> "createdBy must be defined!");
    RepoAssert.notEmpty(queueName, () -> "queueName must be defined!");
    RepoAssert.notEmpty(appId, () -> "appId must be defined!");
    
    final var scope = ImmutableChannelTxScope.builder()
        .channelId(state.getDataSource().getChannel().getId())
        .commitAuthor(createdBy)
        .commitMessage(comment)
        .build();
    
    return state.withChannelTransaction(scope, state -> 
        Uni.combine().all().unis(
          state.queryQueues().findByQueueName(queueName),
          state.queryQueueConsumer().findAllByAppId(appId, true)
        )
        .asTuple()
        .onItem().transformToUni(input -> visitBatch(state, input))
    );
  
  }

  
  private Uni<ThenaMqEnvelope<Queue>> visitBatch(ThenaMqChannelState state, Tuple2<Optional<Queue>, List<QueueConsumer>> input) {
    final var queue = visitQueue(input.getItem1());
    final var consumers = visitQueueConsumers(queue, input.getItem2());
    
    final var request = batch
      .log(batchLog.toString())
      .channelId(state.getDataSource().getChannel().getId())
      .build();
    
    return state.batchMany(request).onItem().transform(resp -> {
          
      if(resp.getBatchStatus() == OperationStatus.ERROR) {
        return ImmutableThenaMqEnvelope
            .<Queue>builder()
            .operationStatus(resp.getBatchStatus())
            .channel(state.getDataSource().getChannel())
            .channelId(state.getDataSource().getChannel().getId())
            .object(queue)
            .operationLogs(resp.getLogs())
            .build();
      }
         
      return ImmutableThenaMqEnvelope
        .<Queue>builder()
        .operationStatus(OperationStatus.OK)
        .channel(state.getDataSource().getChannel())
        .channelId(state.getDataSource().getChannel().getId())
        .object(queue)
        .build();
      
    });
  }

  private Queue visitQueue(Optional<Queue> foundQueue) {
    final var queue = foundQueue.orElseGet(() -> {
      
      final var newQueue = ImmutableQueue.builder()
          .id(OidUtils.gen())
          .createdAt(OffsetDateTime.now())
          .comment(comment)
          .createdBy(createdBy)
          .queueName(queueName)
          .build();
      batch.addNewQueues(newQueue);
      return newQueue;
    });
    return queue;
  }


  // sync existing queue
  private List<QueueConsumer> visitQueueConsumers(Queue queue, List<QueueConsumer> foundAppConsumers) {

    final var activeConsumers = new ArrayList<QueueConsumer>();
    
    // sync existing state
    for(final var prev : foundAppConsumers) {
      final var consumerName = prev.getConsumerName();
      
      // new VS existing in DB
      if(newConsumers.containsKey(consumerName)) {
        final var currentState = newConsumers.get(consumerName);
        final var nextState = visitExistigQueueConsumers(prev, currentState, queue);
        newConsumers.remove(consumerName);
        activeConsumers.add(nextState);
        continue;
      }
      
      // does'nt exist anymore
      visitMissingQueueConsumers(prev);
    }
    
    // add all the new ones
    newConsumers.forEach((name, current) -> {
      final var nextState = visitNewQueueConsumers(current, queue);
      activeConsumers.add(nextState);
    });

    return activeConsumers;
  }


  // enable and update existing consumer
  private QueueConsumer visitExistigQueueConsumers(QueueConsumer prev, ConsumerBuilderImpl current, Queue queue) {
    final var nextState = ImmutableQueueConsumer.builder().from(prev)
        .consumerStatus(QueueConsumerStatus.ENABLED)
        .qualifiedJavaName(current.getClass().getPackageName() + "." + current.getClass().getName())
        .routingKey(Optional.ofNullable(current.routingKey()).orElse(queue.getQueueName()))
        .build();
    
    if(nextState.equals(ImmutableQueueConsumer.builder().from(prev).build())) {
      return prev;
    }
    
    final var result = nextState.withUpdatedAt(OffsetDateTime.now());
    this.batch.addUpdateQueueConsumer(result);
    return result;
  }

  
  // create new queue
  private QueueConsumer visitNewQueueConsumers(ConsumerBuilderImpl current, Queue queue) {
    final var nextState = ImmutableQueueConsumer.builder()
        .id(OidUtils.gen())
        .comment(current.comment())
        .consumerStatus(QueueConsumerStatus.ENABLED)
        .qualifiedJavaName(current.getClass().getPackageName() + "." + current.getClass().getName())
        .routingKey(Optional.ofNullable(current.routingKey()).orElse(queue.getQueueName()))
        .createdAt(OffsetDateTime.now())
        .consumerName(current.consumerName())
        .appId(appId)
        .build();
    this.batch.addNewQueueConsumer(nextState);
    return nextState;
  }


  // disable queue
  private QueueConsumer visitMissingQueueConsumers(QueueConsumer prev) {
    if(prev.getConsumerStatus() == QueueConsumerStatus.DISABLED) {
      // already disabled nothing to do
      return prev;
    }
    
    // disable consumer
    final var nextState = ImmutableQueueConsumer.builder().from(prev)
        .consumerStatus(QueueConsumerStatus.DISABLED)
        .updatedAt(OffsetDateTime.now())
        .build();
  
    final var result = nextState.withUpdatedAt(OffsetDateTime.now());
    this.batch.addUpdateQueueConsumer(result);
    return result;
  }
  
  @Getter @Setter @Accessors(fluent = true)
  private static class ConsumerBuilderImpl implements ConsumerBuilder {
    private String routingKey;
    private String consumerName;
    private String comment;
    private ThenaMqConsumer thenaMqConsumer;
    private final MutableBoolean completed = new MutableBoolean(false);
    
    @Override
    public void build(ThenaMqConsumer worker) {
      RepoAssert.notNull(worker, () -> "worker must be defined!");
      RepoAssert.notEmpty(consumerName, () -> "consumerName must be defined!");
      RepoAssert.notEmpty(comment, () -> "comment must be defined!");
      this.completed.setTrue();
      this.thenaMqConsumer = worker;
    }
  }
}
