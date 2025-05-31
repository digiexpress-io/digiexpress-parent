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

import java.util.concurrent.ScheduledExecutorService;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorLogger;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.resys.thena.spi.TenantDataSource;
import io.resys.thena.spi.TenantDataSource.TxScope;
import lombok.Data;


@Data
public class ExecutorContextImpl implements ExecutorContext {

  private final ExecutorLogger log;
  private final Batch batch;
  private final BatchDb db;
  private final BatchConfig config;
  private final RuntimeInstance instance;
  private final TenantDataSource.TxScope scope;
  private final ScheduledExecutorService threadPool;

  
  public ExecutorContextImpl(
      ExecutorLogger log, 
      Batch batch,
      BatchDb db, 
      BatchConfig config,
      RuntimeInstance instance, 
      TxScope scope,
      ScheduledExecutorService threadPool) {
    
    super();
    this.log = log;
    this.batch = batch;
    this.db = db;
    this.config = config;
    this.instance = instance;
    this.scope = scope;
    this.threadPool = threadPool;
  }


  @Override
  public ExecutorContext withRuntimeInstance(RuntimeInstance instance) {
    return new ExecutorContextImpl(log, batch, db, config, instance, scope, threadPool);
  }
}
