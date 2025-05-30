package io.digiexpress.thena.batch.client.spi.batchenvir;

import java.util.List;

import io.digiexpress.thena.batch.client.api.BatchEnvir.BatchEnvirKillBuilder;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirImpl.StartedRuntimeInstance;
import io.digiexpress.thena.batch.client.spi.batchenvir.instance.InstanceRunnerCancel;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(chain = true, fluent = true)
public class BatchEnvirKillBuilderImpl implements BatchEnvirKillBuilder {
  private final List<StartedRuntimeInstance> executing;
  private final BatchConfig config;
  private final BatchDb db;
  
  private String commitMessage;
  private String commitAuthor;
  
  @Override
  public Uni<List<RuntimeInstance>> killAll() {
    RepoAssert.notBlank(commitAuthor, () -> "commitAuthor must be defined!");
    RepoAssert.notBlank(commitMessage, () -> "commitMessage must be defined!");
    return Uni.join().all(executing.stream().map(ex -> cancel(ex)).toList()).andCollectFailures();
  }
  @Override
  public Uni<RuntimeInstance> killInstance(String runtimeIdOrName) {
    RepoAssert.notBlank(commitAuthor, () -> "commitAuthor must be defined!");
    RepoAssert.notBlank(commitMessage, () -> "commitMessage must be defined!");
    
    // TODO Auto-generated method stub
    return null;
  }
  
  private Uni<RuntimeInstance> cancel(StartedRuntimeInstance instance) {    
    return Uni.createFrom().item(() -> {
      instance.getContext().getThreadPool().shutdownNow();
      return "";
    }).onItem().transformToUni(ignore -> new InstanceRunnerCancel(instance.getContext()).accept());
  }

}
