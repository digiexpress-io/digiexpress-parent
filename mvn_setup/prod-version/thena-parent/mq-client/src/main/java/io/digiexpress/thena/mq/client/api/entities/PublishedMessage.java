package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface PublishedMessage extends ThenaMqEntity {
  String getId();
  String getQueueId();
  String getRoutingKey();
  @Nullable JsonObject getRoutingProps();
  
  String getComment();
  String getCreatedBy();

  OffsetDateTime getCreatedAt();
  OffsetDateTime getExpiresAt();
  OffsetDateTime getStartsAt();

  String getBodyId();
  String getBodyType();
  JsonObject getBodyValue();
}