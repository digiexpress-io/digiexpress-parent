package io.digiexpress.thena.batch.client.spi.batchenvir;

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
