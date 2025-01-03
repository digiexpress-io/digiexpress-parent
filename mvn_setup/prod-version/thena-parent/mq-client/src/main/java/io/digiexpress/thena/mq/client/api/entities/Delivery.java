package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface Delivery extends ThenaMqEntity {
  String getId();
  String getMessageId();
  String getQueueId();
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
    DeliveryStatus getDeliveryStatus();
    
    OffsetDateTime getCreatedAt();
    @Nullable OffsetDateTime getUpdatedAt();
    
    @Nullable String getAckComment();
    @Nullable JsonObject getAckError();
    @Nullable DeliveryAckValue getAckValue();
    
  }
  
  enum DeliveryStatus {
    STARTED, ERROR, COMPLETED
  }
  enum DeliveryAckValue {
    OK, ERROR 
  }
}