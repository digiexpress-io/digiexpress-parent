package io.digiexpress.thena.mq.client.api.persistence;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.thena.mq.client.api.entities.Binding;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryAttempt;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.entities.ImmutableLog;
import io.digiexpress.thena.mq.client.api.entities.Log;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.QueueMessageStatus;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqContainers;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;

public interface ThenaMqChannelState {
  ThenaMqDataSource getDataSource();
  
  // change channel
  Uni<ThenaMqChannelState> withDefaultChannel();
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
  InternalDeliveryQuery queryDeliveries();

  interface InternalDeliveryQuery {
    Uni<List<Delivery>> findAllByAppIdAndStatus(String appId, DeliveryStatus status, boolean lockForUpdate);
    Uni<List<DeliveryAttempt>> findLastNAttemptEntries(long entries);
    Uni<List<Delivery>> findLastNEntries(long entries);
  }
  
  interface InternalQueueConsumerQuery {
    Uni<List<QueueConsumer>> findAllByAppId(String appId, boolean lockForUpdate);
    Uni<List<QueueConsumer>> findAllEnabled();
    Uni<List<QueueConsumer>> findAllEnabled(String appId);
    Uni<List<QueueConsumer>> findAll();
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
    Uni<List<Channel>> findAll();
    Uni<Void> delete();
    Uni<Channel> delete(Channel newRepo);
  }
  
  interface InternalMessageQuery {
    Uni<List<QueueMessage>> findLastNEntries(long entries);
    Uni<List<QueueMessage>> findAllByStatus(QueueMessageStatus status, boolean lockForUpdate);
    Uni<List<QueueMessage>> findAllByAppIdAndDeliveryStatus(String appId, DeliveryStatus status);
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
    List<Binding> getNewBindings();
    Optional<Channel> getNewChannel();
    
    
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
    
    default ChannelBatch merge(List<ChannelBatch> current) {
      final var start = this;
      final var builder = ImmutableChannelBatch.builder().from(start);
      final var log = new StringBuilder(start.getLog());
      var status = start.getBatchStatus();
      for(final var value : current) {
        if(value == null) {
          continue;
        }
        
        if(status != OperationStatus.ERROR) {
          status = value.getBatchStatus();
        }
        log.append("\r\n\r\n").append(value.getLog());
        builder.addAllLogs(value.getLogs());
      }
      
      return builder.batchStatus(status).build();
    }
  }
  
  public static class ChannelBatchException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final ChannelBatch batch;
    
    public ChannelBatchException(ChannelBatch current, String msg, Throwable t) {
      this.batch = ImmutableChannelBatch.builder()
          .from(current)
          .batchStatus(OperationStatus.ERROR)
          .addLogs(ImmutableLog.builder().text(msg).exception(t).build())
          .addLogs(ImmutableLog.builder().text(t.getMessage()).build())
          .build(); 
    }
    
    public ChannelBatchException(ChannelBatch batch) {
      this.batch = batch;
    }
    public ChannelBatch getBatch() {
      return batch;
    }
  }
  
}
