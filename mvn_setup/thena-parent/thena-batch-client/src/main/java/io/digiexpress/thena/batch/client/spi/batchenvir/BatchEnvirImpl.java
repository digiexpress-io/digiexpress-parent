package io.digiexpress.thena.batch.client.spi.batchenvir;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import io.digiexpress.thena.batch.client.api.BatchEnvir;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.resys.thena.support.RepoAssert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;



public class BatchEnvirImpl implements BatchEnvir {
  
  private final BatchConfig config;
  private final BatchDb db;
  private final Supplier<ScheduledExecutorService> threadPoolFactory;
  
  private final List<StartedRuntimeInstance> executing = new CopyOnWriteArrayList<>();

  
  @RequiredArgsConstructor @Getter
  public static class StartedRuntimeInstance {
    private final ExecutorContext context;
  }
  
  public BatchEnvirImpl(BatchConfig config, BatchDb db, Supplier<ScheduledExecutorService> threadPoolFactory) {    
    super();
    
    this.config = config;
    this.db = db;
    this.threadPoolFactory = threadPoolFactory;
    
    RepoAssert.notNull(config, () -> "config must be defined!");
    RepoAssert.notNull(db, () -> "db must be defined!");
    RepoAssert.notNull(threadPoolFactory, () -> "threadPoolFactory must be defined!");
  }
  
  @Override
  public BatchEnvirExecuteBuilder executor() {
    return new BatchEnvirExecuteBuilderImpl(threadPoolFactory, executing, config, db);
  }

  @Override
  public BatchEnvirKillBuilder kill() {
    return new BatchEnvirKillBuilderImpl(executing, config, db);
  }
}
