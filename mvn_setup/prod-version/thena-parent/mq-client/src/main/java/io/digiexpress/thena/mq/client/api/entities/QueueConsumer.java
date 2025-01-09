package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

@Value.Immutable
public interface QueueConsumer extends ThenaMqEntity {
  String getId();
  String getAppId();
  String getConsumerName();
  QueueConsumerStatus getConsumerStatus();

  OffsetDateTime getCreatedAt();
  @Nullable OffsetDateTime getUpdatedAt();
  
  String getQualifiedJavaName();
  String getComment();
  String getRoutingKey();
  
  enum QueueConsumerStatus {
    ENABLED, DISABLED
  }
}
