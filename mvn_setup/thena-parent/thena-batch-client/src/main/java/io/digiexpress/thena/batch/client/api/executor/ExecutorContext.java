package io.digiexpress.thena.batch.client.api.executor;

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
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.api.persistence.ImmutableBatchTransactionEntries;
import io.resys.thena.spi.TenantDataSource;

public interface ExecutorContext {
  ExecutorLogger getLog();
  Batch getBatch();
  
  BatchDb getDb();
  BatchConfig getConfig();
  RuntimeInstance getInstance();
  TenantDataSource.TxScope getScope();
  ScheduledExecutorService getThreadPool();
  
  
  ExecutorContext withRuntimeInstance(RuntimeInstance instance);
  
  default ImmutableBatchTransactionEntries.Builder createPersistContainer() {
    return ImmutableBatchTransactionEntries.builder()
        .tenantId(getDb().getDataSource().getTenant().getId())
        .status(OperationStatus.OK)
        .log("");
  }
}
