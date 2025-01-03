package io.digiexpress.thena.mq.client.api.entities;

import java.time.OffsetDateTime;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;


@Value.Immutable
public interface Channel extends ThenaMqEntity {
  String getId();
  String getPrefix();
  @Nullable String getExternalId();
  OffsetDateTime getCreatedAt();
  String getCreatedBy();
  String getComment();
  String getChannelName();
}
