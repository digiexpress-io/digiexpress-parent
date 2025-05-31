package io.digiexpress.thena.batch.client.spi.batchenvir.step;

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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscription;

import io.digiexpress.thena.batch.client.api.entities.BatchConfig.BatchConfigWithExecutor;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeMetric;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.spi.batchenvir.BatchEnvirLogger;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RequiredArgsConstructor
public class StepEventSubscriber implements MultiSubscriber<StepEvent> {
  private final ExecutorContext context;
  private final BatchConfigWithExecutor config;
  private final RuntimeStep step;
  
  private long time;

  private ImmutableRuntimeMetric metric;
  private MetricBody body;
  
  private Subscription subscription;
  private final List<StepEvent> collected = new ArrayList<>();
  
  @Override
  public void onSubscribe(Subscription subscription) {
    subscription.request(1);
    
    this.body = MetricBody.builder().stepName(step.getName()).build();
    this.subscription = subscription;
    this.metric = ImmutableRuntimeMetric.builder()
        .id(OidUtils.gen())
        .createdAt(OffsetDateTime.now())
        .stepId(step.getId())
        .runtimeId(step.getRuntimeId())
        .name("batch-metrics")
        .valueStructured(JsonObject.mapFrom(body))
        .build();
    
    // create the metric
    context.getDb().withTransaction(context.getScope(), db -> {
      return db.builder()
          .persist(context.createPersistContainer()
          .addRuntimeMetricInserts(metric)
          .build());      
    }).subscribe().with(
        (success) -> {
          // great success :)
        },
        (failure) -> {
          // :(
          BatchEnvirLogger.METRIC_ERROR
            .withContext(context)
            .addProps(config)
            .addProps(step)
            .append("Failed to create batch metric, message: {}", failure.getMessage());
        });

  }

  @Override
  public void onItem(StepEvent item) {
    subscription.request(1);
    collected.add(item);
    body.addEvent(item);
    if(isFlush()) {
      flush();
    }
  }

  @Override
  public void onFailure(Throwable failure) {
    // Probably something went wrong with the whole stream    
    BatchEnvirLogger.METRIC_ERROR
      .withContext(context)
      .addProps(config)
      .addProps(step)
      .append("Failed to append batch metric, message: {}", failure.getMessage());
  }

  @Override
  public void onCompletion() {
    flush();
  }

  
  private void flush() {

    time = System.currentTimeMillis();
    context.getDb().withTransaction(context.getScope(), db -> {
      return db.builder()
          .persist(context.createPersistContainer()
          .addRuntimeMetricUpdates(metric
              .withUpdatedAt(OffsetDateTime.now())
              .withValueStructured(JsonObject.mapFrom(body)))
          .build());      
    }).subscribe().with(
        (success) -> {
          // great success :)
        },
        (failure) -> {
          // :(
          BatchEnvirLogger.METRIC_ERROR
            .withContext(context)
            .addProps(config)
            .addProps(step)
            .append("Failed to create batch metric, message: {}", failure.getMessage());
        });


  }
  
  private boolean isFlush() {
    final long timeout = 1000 * 5; // 5 second
    return System.currentTimeMillis() - time > timeout;
  }
  
  
  @Data @Builder
  public static class MetricBody {
    private final String stepName;
    private long successCount; 
    private long failCount;
    
    private long maxCost;
    private long minCost;
    
    private String cheapId;
    private String expensiveId;
    
    public void addEvent(StepEvent item) {
      if(item.isOk()) {
        successCount++;
        addCosts(item);
      } else {
        failCount++;
      }
    }
    
    private void addCosts(StepEvent item) {
      final var cost = item.getEndedAt().toInstant().toEpochMilli() - item.getCreatedAt().toInstant().toEpochMilli();
      if(cost > maxCost) {
        maxCost = cost;
        expensiveId = item.getProcessed().get().getEntityId();
      }
      
      if(cost < minCost || cost == 0) {
        minCost = cost;
        cheapId = item.getProcessed().get().getEntityId();
      }
    }
  }
}
