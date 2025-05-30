package io.digiexpress.thena.batch.client.spi.batchenvir;

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
