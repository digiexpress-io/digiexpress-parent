package io.digiexpress.thena.batch.client.api;

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
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.immutables.value.Value;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.Envelope;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.resys.thena.api.actions.TenantActions;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface BatchClient {
  BatchClient withTenant(String tenantId);
  TenantActions manageTenants();
  
  BatchQuery queryBatches();
  RuntimeInstanceQuery queryRuntimeInstances();
  CreateOneRuntimeInstance createOneRuntimeInstance();  
  
  CreateBatchConfig createBatchConfig();
  CreateBatchEnvir createBatchEnvir();
  
  
  interface BatchQuery {
    Multi<Batch> findAll();
  }
  
  interface CreateBatchEnvir {
    CreateBatchEnvir config(BatchConfig config);
    /**
     * optional thread pool for step-row execution, 
     * will default internally to
     * return Executors.newScheduledThreadPool(2);
     */
    CreateBatchEnvir threadPool(Supplier<ScheduledExecutorService> threadPool);
    
    BatchEnvir build();
  }
  
  interface RuntimeInstanceQuery {
    RuntimeInstanceQuery status(RuntimeStatus ... status);
    Uni<Envelope<List<RuntimeInstance>>> findAll();
  }
  
  interface CreateOneRuntimeInstance {
    CreateOneRuntimeInstance appId(String appId);
    CreateOneRuntimeInstance batchName(String batchName);

    CreateOneRuntimeInstance instanceName(String instanceName);
    CreateOneRuntimeInstance instanceSeq(boolean instanceSeq); //append sequence number, default false

    CreateOneRuntimeInstance commitMessage(String commitMessage);
    CreateOneRuntimeInstance commitAuthor(String commitAuthor);
    
    CreateOneRuntimeInstance params(JsonObject params);
    
    Uni<Envelope<RuntimeInstance>> build();
  }
  
  
  interface CreateBatchConfig {
    CreateBatchConfig appId(String appId);// app-id, identify who or what is creating the consumers
    CreateBatchConfig addBatch(Consumer<BatchBuilder> batchBuilder);
    CreateBatchConfig addConsumer(Consumer<BatchConsumerBuilder> consumerBuilder);
    CreateBatchConfig commitMessage(String commitMessage);
    CreateBatchConfig commitAuthor(String commitAuthor);
    CreateBatchConfig addAll(List<BatchDefinition> def);
    
    Uni<Envelope<BatchConfig>> build();

  }
  
  interface BatchBuilder {
    BatchBuilder batchName(String batchName);
    BatchBuilder comment(String comment);
    BatchBuilder externalId(String externalId);
    Batch build();
  }
  
  interface BatchConsumerBuilder {
    BatchConsumerBuilder batchName(String batchName);
    BatchConsumerBuilder comment(String comment);
    BatchConsumerBuilder consumerName(String consumerName);
    BatchConsumer build(Executor<?, ?> worker);
  }
  
  
  // Marker interface
  @Value.Immutable
  interface BatchDefinition {
    String getBatchName();
    String getComment(); // user comment, what should this batch do
    @Nullable String getExternalId();
    
    List<BatchStepDefinition> getSteps();
  }
  
  @Value.Immutable
  interface BatchStepDefinition {
    String getName();
    String getComment(); // user comment, what should this step do
    Executor<?, ?> getExecutor();
  }
}
