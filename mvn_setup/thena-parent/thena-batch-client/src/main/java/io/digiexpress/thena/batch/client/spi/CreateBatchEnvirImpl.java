package io.digiexpress.thena.batch.client.spi;

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

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import io.digiexpress.thena.batch.client.api.BatchClient.CreateBatchEnvir;
import io.digiexpress.thena.batch.client.api.BatchEnvir;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirImpl;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(chain = true, fluent = true)
public class CreateBatchEnvirImpl implements CreateBatchEnvir {
  private final BatchDb batchDb;

  private BatchConfig config;
  private Supplier<ScheduledExecutorService> threadPool;
  
  @Override
  public BatchEnvir build() {
    RepoAssert.notNull(config, () -> "config must be provided!");
    return new BatchEnvirImpl(config, batchDb, Optional.ofNullable(threadPool).orElse(() -> createThreadPool()));
  }

  private ScheduledExecutorService createThreadPool() {
    //.emitOn(Infrastructure.getDefaultWorkerPool())
    //.emitOn(Executors.newScheduledThreadPool(2))
    // do not return Infrastructure.getDefaultWorkerPool();
    return Executors.newScheduledThreadPool(2);
  }
}
