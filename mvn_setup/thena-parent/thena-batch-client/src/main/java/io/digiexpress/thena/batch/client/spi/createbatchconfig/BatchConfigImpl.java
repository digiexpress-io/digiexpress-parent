package io.digiexpress.thena.batch.client.spi.createbatchconfig;

/*-
 * #%L
 * thena-batch-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
