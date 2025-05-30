package io.digiexpress.thena.batch.client.api.entities;

import java.util.List;

import org.immutables.value.Value;

import io.digiexpress.thena.batch.client.api.executor.Executor;

public interface BatchConfig {
  String getAppId();
  List<Batch> getBatches();
  
  int getThreads();
  int getConcurrency();
  int getRetryAttempts();
  long getThreadPoolPulse(); // check every n millis if threadpool is alive
  
  List<BatchConfigWithExecutor> findAllExecutors(String batchId);
  
  @Value.Immutable
  interface BatchConfigWithExecutor {
    Batch getBatch();
    BatchConsumer getBatchConsumer();
    Executor<?, ?> getExecutor();
  }
}
