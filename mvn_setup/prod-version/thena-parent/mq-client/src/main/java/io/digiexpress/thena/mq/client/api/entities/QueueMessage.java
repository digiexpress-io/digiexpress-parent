package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface QueueMessage extends ThenaMqEntity {
  String getId();
  String getRoutingKey();
  
  String getComment();
  String getCreatedBy();

  OffsetDateTime getCreatedAt();
  OffsetDateTime getExpiresAt();
  OffsetDateTime getStartsAt();

  
  String getBodyId();
  String getBodyType();
  JsonObject getBodyValue();
  
  @Nullable OffsetDateTime getUpdatedAt();
  QueueMessageStatus getStatus();

  enum QueueMessageStatus {
    RESOLVING_ROUTING, ROUTING_COMPLETED
  }
}