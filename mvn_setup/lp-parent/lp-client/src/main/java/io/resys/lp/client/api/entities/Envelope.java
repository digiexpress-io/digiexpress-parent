package io.resys.lp.client.api.entities;

import java.util.List;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;


@Value.Immutable
public interface Envelope<T> {
  EnvelopeStatus getStatus();
  List<Log> getLogs();
  @Nullable T getObject(); // Operation result
  
  enum EnvelopeStatus { OK, ERROR, WARNING }
  
  @Value.Immutable
  interface Log {
    @Nullable String getTargetId();
    String getText();
    @Nullable Throwable getException();
  }
}
