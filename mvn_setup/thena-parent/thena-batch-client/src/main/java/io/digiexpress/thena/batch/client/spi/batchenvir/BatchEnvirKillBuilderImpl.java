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
import java.util.concurrent.TimeUnit;

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
  private boolean graceful = true;
  
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
  @Override
  public BatchEnvirKillBuilder graceful(boolean graceful) {
    this.graceful = graceful;
    return this;
  }
  
  private Uni<RuntimeInstance> cancel(StartedRuntimeInstance instance) {    
    
    
    return Uni.createFrom().item(() -> {
      if(graceful) {
        instance.getContext().getThreadPool().shutdown();
      } else {
        instance.getContext().getThreadPool().shutdownNow();
      }
      return "";
    }).onItem().transformToUni(ignore -> new InstanceRunnerCancel(instance.getContext()).accept());
  }

}
