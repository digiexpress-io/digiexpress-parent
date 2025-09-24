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

import java.util.List;

import io.digiexpress.thena.batch.client.api.BatchClient.RuntimeStepQuery;
import io.digiexpress.thena.batch.client.api.entities.Envelope;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelope;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStepTransitives;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeMetric;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RuntimeStepQueryImpl implements RuntimeStepQuery {
  private final BatchDb batchDb;
  
  @Override
  public Uni<Envelope<RuntimeStep>> getOne(String stepId) {
    return batchDb.withTenant().onItem().transformToUni(db -> onQuery(db, stepId)
        .onFailure().recoverWithItem(t -> onError(db, t)));
  }
  
  private Envelope<RuntimeStep> onError(BatchDb batchDb, Throwable throwable) {
    return ImmutableEnvelope.<RuntimeStep>builder()
        .tenantId(batchDb.getDataSource().getTenant().getId())
        .addOperationLogs(ImmutableEnvelopeLog.builder()
            .text(new StringBuilder()
              .append("Runtime instance step query to: '").append(batchDb.getDataSource().getTenant().getId()).append("'").append(" is rejected.")
              .append(System.lineSeparator())
              .append("Message: ").append(throwable.getMessage())
              .toString())
            .exception(throwable)
            .build())
        .operationStatus(OperationStatus.ERROR)
      .build();
  }
  
  private Uni<Envelope<RuntimeStep>> onQuery(BatchDb batchDb, String stepId) {
    return Uni.combine().all().unis(
        batchDb.query().querySteps().getById(stepId, false),
        batchDb.query().queryStepRows().findAllByStepId(stepId),
        batchDb.query().queryMetrics().findAllByStepId(stepId),
        batchDb.query().queryLogs().findAllByStepId(stepId)
      )
      .asTuple()
      .onItem().transform(tuple -> onMap(batchDb, tuple.getItem1(), tuple.getItem2(), tuple.getItem3(), tuple.getItem4()));
  }
  
  private Envelope<RuntimeStep> onMap(
      BatchDb batchDb, 
      RuntimeStep step,
      List<RuntimeStepRow> stepRows,
      List<RuntimeMetric> metrics,
      List<RuntimeLog> logs
  ) {
  
    final Envelope<RuntimeStep> result = ImmutableEnvelope.<RuntimeStep>builder()
        .tenant(batchDb.getDataSource().getTenant())
        .tenantId(batchDb.getDataSource().getTenant().getId())
        .operationStatus(Envelope.OperationStatus.OK)
        .object(ImmutableRuntimeStep.builder()
            .from(step)
            .transitives(ImmutableRuntimeStepTransitives.builder()
              .addAllStepRows(stepRows)
              .addAllMetrics(metrics)
              .addAllLogs(logs)
              .build())
            .build())
        .build();
    
    return result;
  }
}
