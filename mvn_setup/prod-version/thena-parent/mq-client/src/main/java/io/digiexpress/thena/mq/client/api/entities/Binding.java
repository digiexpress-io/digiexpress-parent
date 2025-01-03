package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

@Value.Immutable
public interface Binding extends ThenaMqEntity {
  String getId();
  String getSourceId();
  OffsetDateTime getCreatedAt();
  String getMessageId();
  
  String getQueueName();
  String getRoutingKey();
  String getAddressName();
}