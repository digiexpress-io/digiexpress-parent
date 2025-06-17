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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.thena.batch.client.api.BatchClient.RuntimeInstanceQuery;
import io.digiexpress.thena.batch.client.api.entities.Envelope;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelope;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstanceTransitives;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeMetric;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RuntimeInstanceQueryImpl implements RuntimeInstanceQuery {
  private final BatchDb batchDb;
  private final List<RuntimeStatus> status = new ArrayList<>();
  
  @Override
  public RuntimeInstanceQuery status(RuntimeStatus... status) {
    this.status.addAll(Arrays.asList(status));
    return this;
  }
  @Override
  public Uni<Envelope<List<RuntimeInstance>>> findAll() {
    return batchDb.withTenant().onItem().transformToUni(db -> onQuery(db)
        .onFailure().recoverWithItem(t -> onError(db, t)));
  }
  
  private Envelope<List<RuntimeInstance>> onError(BatchDb batchDb, Throwable throwable) {
    return ImmutableEnvelope.<List<RuntimeInstance>>builder()
        .tenantId(batchDb.getDataSource().getTenant().getId())
        .addOperationLogs(ImmutableEnvelopeLog.builder()
            .text(new StringBuilder()
              .append("Runtime instance query to: '").append(batchDb.getDataSource().getTenant().getId()).append("'").append(" is rejected.")
              .append(System.lineSeparator())
              .append("Message: ").append(throwable.getMessage())
              .toString())
            .exception(throwable)
            .build())
        .operationStatus(OperationStatus.ERROR)
      .build();
  }
  
  private Uni<Envelope<List<RuntimeInstance>>> onQuery(BatchDb batchDb) {
    return Uni.combine().all().unis(
        batchDb.query().queryInstances().findAllByStatus(status), 
        batchDb.query().querySteps().findAllByInstanceStatus(status),
        batchDb.query().queryStepRows().findAllByInstanceStatus(status),
        batchDb.query().queryMetrics().findAllByInstanceStatus(status)
      )
      .asTuple()
      .onItem().transform(tuple -> onMap(batchDb, tuple.getItem1(), tuple.getItem2(), tuple.getItem3(), tuple.getItem4()));
  }
  
  private Envelope<List<RuntimeInstance>> onMap(
      BatchDb batchDb, 
      List<RuntimeInstance> instances, 
      List<RuntimeStep> steps,
      List<RuntimeStepRow> stepRows,
      List<RuntimeMetric> metrics
  ) {
    
    final var groupedSteps = steps.stream().collect(Collectors.groupingBy(e -> e.getRuntimeId()));
    final var groupedStepRows = stepRows.stream().collect(Collectors.groupingBy(e -> e.getRuntimeId()));
    final var groupedMetrics = metrics.stream().collect(Collectors.groupingBy(e -> e.getRuntimeId()));
    
    final Envelope<List<RuntimeInstance>> result = ImmutableEnvelope.<List<RuntimeInstance>>builder()
        .tenant(batchDb.getDataSource().getTenant())
        .tenantId(batchDb.getDataSource().getTenant().getId())
        .operationStatus(Envelope.OperationStatus.OK)
        .object(instances.stream().map(instance -> {
          
          final RuntimeInstance built = ImmutableRuntimeInstance.builder()
              .from(instance)
              .transitives(ImmutableRuntimeInstanceTransitives.builder()
                  
                  .addAllSteps(
                    groupedSteps.getOrDefault(instance.getId(), Collections.emptyList())
                    .stream().sorted((a,b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .toList()
                  )
                  .addAllStepRows(
                      groupedStepRows.getOrDefault(instance.getId(), Collections.emptyList())
                      .stream().sorted((a,b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                      .toList()
                  )
                  .addAllMetrics(
                      groupedMetrics.getOrDefault(instance.getId(), Collections.emptyList())
                      .stream().sorted((a,b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                      .toList()
                  )
                  
                  .build())
              .build();
          
          return built;
        }).toList())
        .build();
    
    return result;
  }
}
