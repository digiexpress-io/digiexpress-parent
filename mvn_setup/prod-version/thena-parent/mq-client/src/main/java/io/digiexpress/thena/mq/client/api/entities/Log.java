package io.digiexpress.thena.mq.client.api.entities;

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public interface Log {
  String getText();
  @Nullable Throwable getException();
}
