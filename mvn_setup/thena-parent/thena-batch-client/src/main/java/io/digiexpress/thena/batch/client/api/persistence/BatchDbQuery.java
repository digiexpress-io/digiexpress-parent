package io.digiexpress.thena.batch.client.api.persistence;

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
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.BatchContainers.BatchTenantContainer;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeMetric;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface BatchDbQuery {
  Uni<BatchTenantContainer> findAll();
  
  BatchDbBatchQuery queryBatches();
  BatchDbBatchConsumerQuery queryBatchConsumers();
  
  BatchDbInstanceQuery queryInstances();
  
  BatchDbStepQuery querySteps();
  BatchDbStepRowQuery queryStepRows();
  BatchDbMetricQuery queryMetrics();
  
  interface BatchDbMetricQuery {
    Uni<List<RuntimeMetric>> findAllByInstanceStatus(List<RuntimeStatus> status);
    Uni<List<RuntimeMetric>> findForLastNInstancesByBatchName(int howMany, String batchIdOrName);
  }
  
  interface BatchDbBatchQuery {
    Multi<Batch> findAll();
    Uni<List<Batch>> findAllByAppId(String appId, boolean lockForUpdate);
    Uni<Optional<Batch>> findOneByAppIdAndName(String appId, String batchName);
    
    Uni<Optional<Batch>> findOneByName(String batchName);
  }
  
  interface BatchDbInstanceQuery {
    Uni<RuntimeInstance> getById(String id, boolean lockForUpdate);
    Uni<List<RuntimeInstance>> findAllByStatus(List<RuntimeStatus> status);
    
    Multi<RuntimeInstance> findLastN(int count); // find last N number of instances for EVERY batch
    Multi<RuntimeInstance> findLastNByBatchName(int count, String batchIdOoName); // find last N number of instances for EVERY batch
    Uni<Long> nextSequence();
  }
  
  interface BatchDbStepQuery {
    Uni<RuntimeStep> getById(String id, boolean lockForUpdate);
    Uni<List<RuntimeStep>> findAllByInstanceId(String instanceId);
    Uni<List<RuntimeStep>> findAllByInstanceStatus(List<RuntimeStatus> status);
    Multi<RuntimeStep> findForLastNInstancesByBatchName(int howMany, String batchIdOrName);
  }
  
  interface BatchDbStepRowQuery {
    Uni<List<RuntimeStepRow>> findAllByInstanceStatus(List<RuntimeStatus> status);
  }
  
  interface BatchDbBatchConsumerQuery {
    Uni<List<BatchConsumer>> findAllByAppId(String appId, boolean lockForUpdate);
  }
}
