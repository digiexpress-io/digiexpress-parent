package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface QueueMessage extends ThenaMqEntity {
  String getId();
  String getQueueId();
  
  String getRoutingKey();
  JsonObject getRoutingProps();
  List<String> getRoutingTopics();
  RoutingStatus getRoutingStatus();
  JsonObject getRoutingLog();
  

  String getComment();
  String getCreatedBy();

  OffsetDateTime getCreatedAt();
  OffsetDateTime getExpiresAt();
  OffsetDateTime getStartsAt();

  String getBodyId();
  String getBodyType();
  JsonObject getBodyValue();
  
  
  enum RoutingStatus {
    RESOLVING_ROUTING, ROUTING_COMPLETED
  }
}