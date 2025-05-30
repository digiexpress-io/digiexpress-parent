package io.digiexpress.thena.batch.client.spi.persistence;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;
import io.resys.thena.api.entities.Tenant;



@Value.Immutable
public abstract class BatchTableNames {
  private static final BatchTableNames DEFAULTS = defaults();
    
  public abstract String getPrefix();
  public abstract String getBatches();
  public abstract String getBatchConsumers();
  
  public abstract String getRuntimeInstances();
  public abstract String getRuntimeInstancesRef();
  public abstract String getRuntimeLogs();
  public abstract String getRuntimeMetrics();
  public abstract String getRuntimeParams();
  public abstract String getRuntimeSteps();
  public abstract String getRuntimeStepRows();


  public BatchTableNames toRepo(Tenant repo) {
    final String prefix = repo.getPrefix();
    return toRepo(prefix);
  }
  
  public BatchTableNames toRepo(String prefix) {
    return ImmutableBatchTableNames.builder()
      .prefix(prefix)

      .batches((            prefix + DEFAULTS.getBatches()).toUpperCase())      
      .batchConsumers((     prefix + DEFAULTS.getBatchConsumers()).toUpperCase())
      
      .runtimeInstances((   prefix + DEFAULTS.getRuntimeInstances()).toUpperCase())
      .runtimeInstancesRef((prefix + DEFAULTS.getRuntimeInstancesRef()).toUpperCase())
      
      .runtimeLogs((        prefix + DEFAULTS.getRuntimeLogs()).toUpperCase())
      .runtimeMetrics((     prefix + DEFAULTS.getRuntimeMetrics()).toUpperCase())
      .runtimeParams((      prefix + DEFAULTS.getRuntimeParams()).toUpperCase())
      .runtimeSteps((       prefix + DEFAULTS.getRuntimeSteps()).toUpperCase())
      .runtimeStepRows((    prefix + DEFAULTS.getRuntimeStepRows()).toUpperCase())
      
      .build();
  }
  
  public static BatchTableNames defaults() {
    return ImmutableBatchTableNames.builder()
        .prefix("")
        .batches("BATCHES")
        .batchConsumers("BATCH_CONSUMERS")
        
        .runtimeInstancesRef("RUNTIME_INSTANCE_REF")
        .runtimeInstances("RUNTIME_INSTANCES")
        .runtimeLogs("RUNTIME_LOGS")
        .runtimeMetrics("RUNTIME_METRICS")
        .runtimeParams("RUNTIME_PARAMS")
        .runtimeSteps("RUNTIME_STEPS")
        .runtimeStepRows("RUNTIME_STEP_ROWS")
        .build();
  }
}
