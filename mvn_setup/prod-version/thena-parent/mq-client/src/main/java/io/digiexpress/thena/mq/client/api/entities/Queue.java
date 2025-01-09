package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

@Value.Immutable
public interface Queue extends ThenaMqEntity {
  String getId();
  OffsetDateTime getCreatedAt();
  String getCreatedBy();
  String getComment();
  String getQueueName();
  


}
