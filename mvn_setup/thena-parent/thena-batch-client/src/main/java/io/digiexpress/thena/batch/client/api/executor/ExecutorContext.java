package io.digiexpress.thena.batch.client.api.executor;

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
