package io.digiexpress.thena.batch.client.api.executor;

import java.util.List;

import org.immutables.value.Value.Immutable;

import io.digiexpress.thena.batch.client.api.entities.RuntimeLog;

@Immutable
public interface ExecutorResult {

  List<RuntimeLog> getMessages();
  ExecutorStatus getStatus();

  
  enum ExecutorStatus {
    OK, ERROR, SKIP
  }

}
