package io.digiexpress.thena.batch.client.spi.createbatchconfig;

import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.ImmutableBatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchConfigImpl implements BatchConfig {

  private final int concurrency = 5;
  private final String appId;
  private final List<Batch> batches;
  private final List<BatchConsumer> batchConsumer;
  private final List<Executor<?, ?>> executors;

  
  @Override
  public String getAppId() {
    return appId;
  }
  @Override
  public List<Batch> getBatches() {
    return batches;
  }

  @Override
  public List<BatchConfigWithExecutor> findAllExecutors(String batchId) {

    return executors.stream().map(executor -> {
      final var batch = batches.stream()
        .filter(b -> 
            b.getBatchName().equals(batchId) || 
            b.getId().equals(batchId) || 
            b.getExternalId().map(e -> e.equals(batchId)).orElse(false))
        .findFirst();
      if(batch.isEmpty()) {
        return Optional.<BatchConfigWithExecutor>empty();
      }
      
      final var consumer = batchConsumer.stream()
        .filter(c -> c.getQualifiedJavaName().equals(executor.getClass().getCanonicalName()))
        .findFirst();

      return Optional.<BatchConfigWithExecutor>of(ImmutableBatchConfigWithExecutor.builder()
          .batch(batch.get())
          .executor(executor)
          .batchConsumer(consumer.get())
          .build());
    })
    .filter(e -> e.isPresent())
    .map(e -> e.get())
    .toList();
  }

  @Override
  public int getConcurrency() {
    return concurrency;
  }
  @Override
  public int getRetryAttempts() {
    return 1;
  }
  @Override
  public int getEventThreads() {
    return 4;
  }
}
