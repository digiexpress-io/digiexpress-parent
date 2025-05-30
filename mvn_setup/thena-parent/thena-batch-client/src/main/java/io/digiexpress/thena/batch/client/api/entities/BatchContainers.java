package io.digiexpress.thena.batch.client.api.entities;

/*-
 * #%L
 * thena-mq-client
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


import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;




public interface BatchContainers {

  @Value.Immutable
  interface BatchTenantContainer extends BatchContainers {
    Map<String, Batch> getBatches();  
    Map<String, BatchConsumer> getBatchConsumers();
    
    Map<String, RuntimeInstance> getRuntimeInstances();
    Map<String, RuntimeLog> getRuntimeLogs();
    Map<String, RuntimeMetric> getRuntimeMetrics();
    Map<String, RuntimeParams> getRuntimeParams();
    Map<String, RuntimeStep> getRuntimeSteps();
    Map<String, RuntimeStepRow> getRuntimeStepRows();

    Tenant getTenant(); 
  } 
}