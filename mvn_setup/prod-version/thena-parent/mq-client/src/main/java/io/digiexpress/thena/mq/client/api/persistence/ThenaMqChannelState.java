package io.digiexpress.thena.mq.client.api.persistence;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.Log;
import io.digiexpress.thena.mq.client.api.entities.PublishedMessage;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqContainers;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
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
  
  
  InternalThenaMqContainersQuery queryContainers();

  
  interface InternalThenaMqContainersQuery {
    Uni<ThenaMqContainers> findAll();
  }
  
  interface InternalChannelQuery {
    Uni<Channel> getByNameOrId(String nameOrId); // channel is null if there is no entity for given criteria
    Multi<Channel> findAll();
    Uni<Void> delete();
    Uni<Channel> delete(Channel newRepo);
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
    List<Binding> getNewBinding();
    List<PublishedMessage> getNewPublishedMessages();
    List<Queue> getNewQueues();

    
    List<Delivery> getUpdateDeliveries();
    List<DeliveryAttempt> getUpdateDeliveryAttempts();
    
    OperationStatus getBatchStatus();
    String getChannelId();

    String getLog();
    List<Log> getLogs();
    
    @JsonIgnore
    default boolean isEmpty() {
      return 
        this.getNewDeliveries().isEmpty() &&
        this.getNewDeliveryAttempts().isEmpty() &&
        this.getNewBinding().isEmpty() &&
        this.getNewPublishedMessages().isEmpty() &&
        this.getNewQueues().isEmpty();
    }
  }
  
}
