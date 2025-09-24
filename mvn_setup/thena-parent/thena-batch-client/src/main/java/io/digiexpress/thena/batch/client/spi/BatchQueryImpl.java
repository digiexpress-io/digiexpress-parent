package io.digiexpress.thena.batch.client.spi;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.thena.batch.client.api.BatchClient.BatchQuery;
import io.digiexpress.thena.batch.client.api.BatchException;
import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.Envelope;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableBatch;
import io.digiexpress.thena.batch.client.api.entities.ImmutableBatchTransitives;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelope;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstanceTransitives;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeMetric;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BatchQueryImpl implements BatchQuery {
  private final BatchDb batchDb;
  

  
  private <T> Envelope<T> onError(BatchDb batchDb, Throwable throwable) {
    return ImmutableEnvelope.<T>builder()
        .tenantId(batchDb.getDataSource().getTenant().getId())
        .addOperationLogs(ImmutableEnvelopeLog.builder()
            .text(new StringBuilder()
              .append("Batch query to: '").append(batchDb.getDataSource().getTenant().getId()).append("'").append(" is rejected.")
              .append(System.lineSeparator())
              .append("Message: ").append(throwable.getMessage())
              .toString())
            .exception(throwable)
            .build())
        .operationStatus(OperationStatus.ERROR)
      .build();
  }
  
  
  private List<Batch> onMap(
      BatchDb batchDb, 
      List<Batch> batches,
      List<RuntimeInstance> instances,
      List<RuntimeStep> steps,
      List<RuntimeMetric> metrics) {


    final var metricByRuntime = metrics.stream()
        .collect(Collectors.groupingBy(instance -> instance.getRuntimeId()));
    
    final var stepByRuntime = steps.stream()
        .collect(Collectors.groupingBy(instance -> instance.getRuntimeId()));
    
    final var instanceByBatch = instances.stream()
        .map(instance -> {
          return ImmutableRuntimeInstance.builder().from(instance)
              .transitives(ImmutableRuntimeInstanceTransitives.builder()
                  .addAllSteps(stepByRuntime.getOrDefault(instance.getId(), Collections.emptyList()))
                  .addAllMetrics(metricByRuntime.getOrDefault(instance.getId(), Collections.emptyList()))
                  .build())
              .build();
        })
        .collect(Collectors.groupingBy(instance -> instance.getBatchId()));
    

    
    final List<Batch> items = batches.stream().map(batch -> {
      final Batch next = ImmutableBatch.builder()
        .from(batch)
        .transitives(ImmutableBatchTransitives.builder()
            .addAllInstances(instanceByBatch.getOrDefault(batch.getId(), Collections.emptyList()))
            .build()).build();
      return next;
    }).toList();
    
    return items;
  }
  
  

  
  @Override
  public Uni<Envelope<List<Batch>>> findAll() {
    return batchDb.withTenant().onItem().transformToUni(db -> 
        Uni.combine().all().unis(
          db.query().queryBatches().findAll().collect().asList(),
          db.query().queryInstances().findLastN(20).collect().asList()
        )
        .asTuple()
        .onItem().transform(tuple -> onMap(db, tuple.getItem1(), tuple.getItem2(), Collections.emptyList(), Collections.emptyList()))
        .onItem().transform(items -> {
          
          final Envelope<List<Batch>> result = ImmutableEnvelope.<List<Batch>>builder()
            .tenant(db.getDataSource().getTenant())
            .tenantId(db.getDataSource().getTenant().getId())
            .operationStatus(Envelope.OperationStatus.OK)
            .object(items)
            .build();
          return result;
        })
        .onFailure().recoverWithItem(t -> onError(db, t))
    );
  }

  @Override
  public Uni<Envelope<Batch>> getOne(String batchId) {
    return batchDb.withTenant().onItem().transformToUni(db -> 
        Uni.combine().all().unis(
          db.query().queryBatches().findOneByName(batchId).onItem().transform(e -> e.map(Arrays::asList).orElse(Collections.emptyList())),
          db.query().queryInstances().findLastNByBatchName(200, batchId).collect().asList(),
          db.query().querySteps().findForLastNInstancesByBatchName(200, batchId).collect().asList(),
          db.query().queryMetrics().findForLastNInstancesByBatchName(200, batchId)
        )
        .asTuple()
        .onItem().transform(tuple -> onMap(db, tuple.getItem1(), tuple.getItem2(), tuple.getItem3(), tuple.getItem4()))
        .onItem().transform(items -> {
          if(items.isEmpty()) {
            throw new BatchException("Batch not found by id or name: " + batchId + "!");
          }
          
          final Envelope<Batch> result = ImmutableEnvelope.<Batch>builder()
            .tenant(db.getDataSource().getTenant())
            .tenantId(db.getDataSource().getTenant().getId())
            .operationStatus(Envelope.OperationStatus.OK)
            .object(items.iterator().next())
            .build();
          return result;
        })
        .onFailure().recoverWithItem(t -> onError(db, t))
    );
  }
}
