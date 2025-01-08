package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface QueueMessage extends ThenaMqEntity {
  String getId();
  String getRoutingKey();
  
  RoutingStatus getRoutingStatus();
  JsonObject getRoutingLog();
  

  String getComment();
  String getCreatedBy();

  OffsetDateTime getCreatedAt();
  OffsetDateTime getExpiresAt();
  OffsetDateTime getStartsAt();
  
  @Nullable OffsetDateTime getUpdatedAt();

  String getBodyId();
  String getBodyType();
  JsonObject getBodyValue();
  
  
  enum RoutingStatus {
    RESOLVING_ROUTING, ROUTING_COMPLETED
  }
}