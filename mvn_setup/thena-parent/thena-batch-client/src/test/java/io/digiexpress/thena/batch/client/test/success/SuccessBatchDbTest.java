package io.digiexpress.thena.batch.client.test.success;

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

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.spi.batchenvir.step.StepEventSubscriber.MetricBody;
import io.digiexpress.thena.batch.client.test.config.DbTestTemplate;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SuccessBatchDbTest extends DbTestTemplate {
  final String batchName = "run-all-dialob-related-tasks";
  final SuccessStep1 closeDialobs = new SuccessStep1();
  BatchClient client;
  
  public void setUp() {
    client = getOrCreateTenant("success-batch-test");
  }
    
  @Test
  public void CreateOneBatch() {
    setUp();
    
    // create config to execute
    final var config = registerBatchesAndWorkers();
    
    // create instance what to execute
    final var instance = client
      .createOneRuntimeInstance()
      .batchName(batchName)
      .instanceName("junit-instance")
      .instanceSeq(true)
      .appId("test-app")
      .params(JsonObject.of(
          "dialobId", "123", 
          "formName", "super-form"))
      .commitAuthor("junit tester")
      .commitMessage("testing instance")
      .build().await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(OperationStatus.OK, instance.getOperationStatus());
    
    // execute instance
    final var envir = client.createBatchEnvir()
      .config(config)
      .build();
    
    final var done = envir
      .executor()
      .commitMessage("test has started")
      .commitAuthor("junit")
      .execute(instance.getObject())
      .await().atMost(Duration.ofMinutes(1));
    
    
    Assertions.assertNotNull(done.getEndedAt());
    Assertions.assertEquals(RuntimeStatus.COMPLETED, done.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, done.getExecutionStatus());
    
    
    final var instances = client.queryRuntimeInstances().includeStepRows().findAll().await().atMost(atMost).getObject();
    Assertions.assertEquals(1, instances.size());
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instances.get(0).getStatus());
    
    Assertions.assertEquals(3, instances.get(0).getTransitives().getSteps().size());
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instances.get(0).getTransitives().getSteps().get(0).getStatus());
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instances.get(0).getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instances.get(0).getTransitives().getSteps().get(2).getStatus());
    
    
    final var step1 = instances.get(0).getTransitives().getSteps().stream().filter(s -> s.getName().equals("close-all")).findFirst().get();
    final var metric1 = instances.get(0).getTransitives().getMetrics()
        .stream().filter(s -> s.getStepId().get().equals(step1.getId()))
        .findFirst().get().getValueStructured().get()
        .mapTo(MetricBody.class);
    
    final var step2 = instances.get(0).getTransitives().getSteps().stream().filter(s -> s.getName().equals("report-closed")).findFirst().get();
    final var metric2 = instances.get(0).getTransitives().getMetrics()
        .stream().filter(s -> s.getStepId().get().equals(step2.getId()))
        .findFirst().get().getValueStructured().get()
        .mapTo(MetricBody.class);;
    
    final var rows =  instances.get(0).getTransitives().getStepRows();
    Assertions.assertEquals(9, rows.stream().filter(row -> row.getStepId().equals(step1.getId())).toList().size());
    Assertions.assertEquals(5, rows.stream().filter(row -> row.getStepId().equals(step2.getId())).toList().size());//close-all

    Assertions.assertEquals(9, metric1.getSuccessCount());
    Assertions.assertEquals(0, metric1.getFailCount());
    //Assertions.assertNotNull(metric1.getCheapId());
    //Assertions.assertNotNull(metric1.getExpensiveId());
    
    Assertions.assertEquals(5, metric2.getSuccessCount());
    Assertions.assertEquals(0, metric2.getFailCount());
    //Assertions.assertNotNull(metric2.getCheapId());
    //Assertions.assertNotNull(metric2.getExpensiveId());
    
    
    {
      final var batchQuery = client.queryBatches().findAll().await().atMost(atMost).getObject();
      Assertions.assertEquals(1, batchQuery.size());
      Assertions.assertEquals(1, batchQuery.get(0).getTransitives().getInstances().size());
    }
    
    {
      
      final var batchQuery = client.queryBatches().getOne(batchName).await().atMost(atMost).getObject();
      
      Assertions.assertNotNull(batchQuery);
      Assertions.assertEquals(1, batchQuery.getTransitives().getInstances().size());
      
      final var firstStep = batchQuery.getTransitives().getInstances().get(0).getTransitives().getSteps().get(0);
      final var stepQuery = client.queryRuntimeSteps().getOne(firstStep.getId()).await().atMost(atMost).getObject();
      Assertions.assertEquals(9, stepQuery.getTransitives().getStepRows().size());
      
    }

  }
  
  
  private BatchConfig registerBatchesAndWorkers() {
    
    final var config = client
      .createBatchConfig()
      .appId("test-app")
      .addBatch(batch -> batch
          .batchName(batchName)
          .comment("dialob session check against session age")
          .externalId("some-super-good-id")
          .build())
      .addConsumer(worker -> worker
          .batchName(batchName)
          .consumerName("close-all")
          .comment("test consumer")
          .build(closeDialobs))
      .addConsumer(worker -> worker
          .batchName(batchName)
          .consumerName("report-closed")
          .comment("test consumer")
          .build(new SuccessStep2()))

      .addConsumer(worker -> worker
          .batchName(batchName)
          .consumerName("empty-step")
          .comment("empty step")
          .build(new SuccessStep3()))
      
      .commitAuthor("junitTest")
      .commitMessage("create batch with 3 consumers")
      .build()
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(OperationStatus.OK, config.getOperationStatus());
    
    final var envir = config.getObject();
    return envir;
  }
}
