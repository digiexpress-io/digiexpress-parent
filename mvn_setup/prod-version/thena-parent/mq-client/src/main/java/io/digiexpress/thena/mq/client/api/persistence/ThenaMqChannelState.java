package io.digiexpress.thena.mq.client.api.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.Log;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.RoutingStatus;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqContainers;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface ThenaMqChannelState {
  ThenaMqDataSource getDataSource();
  
  // change channel
  Uni<ThenaMqChannelState> withChannel(String channelId);
  ThenaMqChannelState withChannel(Channel channel);
  
  // do in channel
  <R> Uni<R> withChannelTransaction(ChannelTxScope channelId, ChannelTransaction<R> callback);
  

  // Patch everything to DB
  Uni<ChannelBatch> batchMany(ChannelBatch output);
  Uni<Channel> insertOne(Channel newRepo);
  
  // find/delete channel
  InternalChannelQuery queryChannels();
  
  InternalQueueQuery queryQueues();
  InternalQueueConsumerQuery queryQueueConsumer();
  InternalThenaMqContainersQuery queryContainers();
  InternalMessageQuery queryMessages();
  

  interface InternalQueueConsumerQuery {
    Uni<List<QueueConsumer>> findAllByAppId(String appId, boolean lockForUpdate);
    
    Uni<List<QueueConsumer>> findAllEnabled();
  }
  
  interface InternalQueueQuery {
    Uni<Optional<Queue>> findByQueueName(String queueName);
    Uni<List<Queue>> findAll();
  }

  interface InternalThenaMqContainersQuery {
    Uni<ThenaMqContainers> findAll();
  }
  
  interface InternalChannelQuery {
    Uni<Optional<Channel>> getByNameOrId(String nameOrId); // channel is null if there is no entity for given criteria
    Multi<Channel> findAll();
    Uni<Void> delete();
    Uni<Channel> delete(Channel newRepo);
  }
  
  interface InternalMessageQuery {
    Uni<List<QueueMessage>> findAllByRoutingStatus(RoutingStatus status, boolean lockForUpdate);
  }
  
  
  
  @FunctionalInterface
  interface ChannelTransaction<R> {
    Uni<R> apply(ThenaMqChannelState currentState);
  }
  @Value.Immutable
  interface ChannelTxScope {
    String getChannelId();
    String getCommitAuthor();
    String getCommitMessage();
  }
  
  
  
  @Value.Immutable
  interface ChannelBatch {
    List<Delivery> getNewDeliveries();
    List<DeliveryAttempt> getNewDeliveryAttempts();
    List<QueueMessage> getNewPublishedMessages();
    List<Queue> getNewQueues();
    List<QueueConsumer> getNewQueueConsumer();

    
    List<Delivery> getUpdateDeliveries();
    List<DeliveryAttempt> getUpdateDeliveryAttempts();
    List<QueueConsumer> getUpdateQueueConsumer();
    List<QueueMessage> getUpdatePublishedMessages();
    
    OperationStatus getBatchStatus();
    String getChannelId();

    String getLog();
    List<Log> getLogs();
    
    @JsonIgnore
    default boolean isEmpty() {
      return
        this.getUpdateDeliveries().isEmpty()  &&
        this.getUpdateDeliveryAttempts().isEmpty()  &&
        this.getUpdateQueueConsumer().isEmpty()  &&
        this.getUpdatePublishedMessages().isEmpty()  &&
          
        this.getNewQueueConsumer().isEmpty() &&
        this.getNewDeliveries().isEmpty() &&
        this.getNewDeliveryAttempts().isEmpty() &&
        this.getNewPublishedMessages().isEmpty() &&
        this.getNewQueues().isEmpty();
    }
    
    default ChannelBatch merge(ChannelBatch next) {
      final var start = this;
      if(next == null) {
        return start;
      }
      
      RepoAssert.isTrue(next.getChannelId().equals(start.getChannelId()), () -> "batch channelId-s must match for merging operations!");
      
      final var status = Arrays.asList(start.getBatchStatus(), next.getBatchStatus());
      final Optional<OperationStatus> isError = status.contains(OperationStatus.ERROR) ? Optional.of(OperationStatus.ERROR) : Optional.empty();
      final Optional<OperationStatus> isConflict = status.contains(OperationStatus.CONFLICT) ? Optional.of(OperationStatus.CONFLICT) : Optional.empty();
      final Optional<OperationStatus> isNoChanges = start.getBatchStatus().equals(next.getBatchStatus()) && next.getBatchStatus().equals(OperationStatus.NO_CHANGES)
          ? Optional.of(OperationStatus.NO_CHANGES) : Optional.empty();
      
      final var nextStatus = isError.or(() -> isConflict).or(() -> isNoChanges).orElse(OperationStatus.OK);
      
      return ImmutableChannelBatch.builder()
          .from(start)
          .from(next)
          .log(String.join(System.lineSeparator(), start.getLog(), next.getLog()))
          .batchStatus(nextStatus)
          .build();
      
    }
    
    
  }
  
}
