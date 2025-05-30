package io.digiexpress.thena.batch.client.spi;

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
