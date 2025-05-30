package io.digiexpress.thena.batch.client.api.executor;

import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.thena.batch.client.api.entities.RuntimeLog;


@Value.Immutable
public interface ExecutorEntity {
  String getEntityId();
  List<RuntimeLog> getMessages();
  ExecutorEntityStatus getStatus();
  
  enum ExecutorEntityStatus {
    OK, ERROR, SKIP
  }
}