package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.ThenaMqConsumer.MessageResponseStatus;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface Delivery extends ThenaMqEntity {
  String getId();
  String getMessageId();
  String getQueueId();
  String getConsumerId();
  DeliveryStatus getStatus();
  
  OffsetDateTime getCreatedAt();
  OffsetDateTime getStartsAt();
  OffsetDateTime getExpiresAt();
  @Nullable OffsetDateTime getCompletedAt();
  List<DeliveryAttempt> getAttempts();
  
  @Value.Immutable
  interface DeliveryAttempt extends ThenaMqEntity {
    String getId();
    String getDeliveryId();
    DeliveryStatus getStatus();
    
    OffsetDateTime getCreatedAt();
    @Nullable OffsetDateTime getUpdatedAt();
    
    @Nullable String getConsumerComment();
    @Nullable JsonObject getConsumerError();
    @Nullable MessageResponseStatus getConsumerStatus();
  }
  
  enum DeliveryStatus {
    OPEN, COMPLETED
  }
}