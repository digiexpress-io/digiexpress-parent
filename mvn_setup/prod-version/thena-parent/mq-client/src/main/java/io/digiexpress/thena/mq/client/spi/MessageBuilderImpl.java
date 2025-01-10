package io.digiexpress.thena.mq.client.spi;

import java.time.OffsetDateTime;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.MessageBuilder;
import io.digiexpress.thena.mq.client.api.entities.ImmutableQueueMessage;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage;
import io.digiexpress.thena.mq.client.api.entities.QueueMessage.QueueMessageStatus;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelTxScope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelBatch;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState.ChannelTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class MessageBuilderImpl implements MessageBuilder {
  private final ThenaMqChannelState state;
  private final OffsetDateTime now = OffsetDateTime.now();
  private final ImmutableChannelBatch.Builder batch = ImmutableChannelBatch.builder().batchStatus(OperationStatus.OK);
  private final StringBuilder batchLog = new StringBuilder();
  
  private String routingKey;
  
  private String comment;
  private String createdBy;

  private OffsetDateTime expiresAt;
  private OffsetDateTime startsAt;
  private OffsetDateTime createdAt;

  private String bodyId;
  private String bodyType;
  private JsonObject bodyValue;
  
  
  @Override
  public Uni<ThenaMqEnvelope<QueueMessage>> build() {
    return request().onItem().transform(resp -> response(resp));
  }
  
  private Uni<ChannelBatch> request() {
    final var msg = createMsg();
    final ChannelTxScope scope = ImmutableChannelTxScope.builder()
        .channelId(state.getDataSource().getChannel().getChannelName())
        .commitAuthor("MessageBuilderImpl")
        .commitMessage("publishing message via builder")
        .build();
    
    return state.withChannelTransaction(scope, tx -> tx.batchMany(batch
        .addNewPublishedMessages(msg)
        .log(batchLog.toString())
        .channelId(state.getDataSource().getChannel().getId())
        .build()));
  }
  
  private ThenaMqEnvelope<QueueMessage> response(ChannelBatch batch) {
    if(batch.getBatchStatus() == OperationStatus.ERROR) {
      return ImmutableThenaMqEnvelope.<QueueMessage>builder()
          .channelId(state.getDataSource().getChannel().getId())
          .channel(state.getDataSource().getChannel())
          .operationStatus(batch.getBatchStatus())
          .operationLogs(batch.getLogs())
          .build();
    }
    
    return ImmutableThenaMqEnvelope.<QueueMessage>builder()
        .channelId(state.getDataSource().getChannel().getId())
        .channel(state.getDataSource().getChannel())
        .operationStatus(batch.getBatchStatus())
        .operationLogs(batch.getLogs())
        .object(batch.getNewPublishedMessages().iterator().next())
        .build();
  }
  
  private QueueMessage createMsg() {
    final var createdAt = this.createdAt == null ? now : this.createdAt;
    final var startsAt = this.startsAt == null ? now : this.startsAt;
    final var expiresAt = this.expiresAt == null ? now : this.expiresAt;
    
    RepoAssert.isTrue(startsAt.isEqual(createdAt) || startsAt.isAfter(createdAt), () -> "message can't start before createdAt()");
    RepoAssert.isTrue(expiresAt.isEqual(now) || expiresAt.isAfter(startsAt), () -> "message can't expire before startsAt()");
    RepoAssert.notEmpty(routingKey, () -> "routingKey can't be empty!");
    
    
    
    return ImmutableQueueMessage.builder()
      .id(OidUtils.gen())
      .routingKey(routingKey)
      .status(QueueMessageStatus.RESOLVING_ROUTING)
  
      .comment(comment)
      .createdBy(createdBy)
  
      
      .createdAt(createdAt)
      .expiresAt(expiresAt)
      .startsAt(startsAt)
  
      .bodyId(bodyId)
      .bodyType(bodyType)
      .bodyValue(bodyValue)
      .build();
  }
  
}
