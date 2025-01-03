package io.digiexpress.thena.mq.client.api.entities;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public interface ThenaMqEnvelope<T> {
  String getChannelId();
  OperationStatus getOperationStatus();
  List<Log> getOperationLogs();
  
  @Nullable Channel getChannel(); // only when available
  @Nullable T getObject(); // Operation result
  
  enum OperationStatus { OK, ERROR, CONFLICT, NO_CHANGES }
}
