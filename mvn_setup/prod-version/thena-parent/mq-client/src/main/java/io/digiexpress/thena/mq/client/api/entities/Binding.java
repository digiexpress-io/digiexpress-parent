package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

@Value.Immutable
public interface Binding extends ThenaMqEntity {
  String getId();
  String getMessageId();
  String getQueueId();
  OffsetDateTime getCreatedAt();
  @Nullable OffsetDateTime getUpdatedAt();
  String getCreatedBy();
  String getComment();
  BindingStatus getStatus();
  
  enum BindingStatus {
    OPEN, COMPLETED 
  }
}