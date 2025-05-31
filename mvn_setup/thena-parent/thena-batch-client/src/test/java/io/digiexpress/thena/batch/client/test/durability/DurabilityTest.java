package io.digiexpress.thena.batch.client.test.durability;

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
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.BatchEnvir;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.test.config.DbTestTemplate;
import io.digiexpress.thena.batch.client.test.config.PgProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class DurabilityTest extends DbTestTemplate {
  
  static DurabilityStep1 duability = new DurabilityStep1();
  static String batchName = "duability-batch";
  static BatchClient client;
  static BatchEnvir envir;
  
  public DurabilityTest() {
    super((client, tenant) -> {
      
      final var repo = client.manageTenants().commit().name("durability-test").build().await().atMost(atMost);
      Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
      DurabilityTest.client = client.withTenant("durability-test");
            
      final var config = DurabilityTest.client
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
              .build(duability))
          .addConsumer(worker -> worker
              .batchName(batchName)
              .consumerName("report-closed")
              .comment("test consumer")
              .build(new DurabilityStep2()))
          .commitAuthor("junitTest")
          .commitMessage("create batch with 2 consumers")
          .build()
          .await().atMost(Duration.ofMinutes(1));
        Assertions.assertEquals(OperationStatus.OK, config.getOperationStatus());

        DurabilityTest.envir = DurabilityTest.client.createBatchEnvir().config(config.getObject()).build();
    });
  }
  
  @Test
  public void testBatchDurability_setBlowUpInBeforeGetConfig() {
    final var instance = runBatch(test -> test.setBlowUpInBeforeGetConfig(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
    
  }
  
  
  @Test
  public void testBatchDurability_setBlowUpInBeforeFindAll() {
    final var instance = runBatch(test -> test.setBlowUpInBeforeFindAll(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
    
  }
  
  
  @Test
  public void testBatchDurability_setBlowUpInInFindAll() {
    final var instance = runBatch(test -> test.setBlowUpInInFindAll(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
  }
  
  
  @Test
  public void testBatchDurability_setBlowUpInInFindAllEntity() {
    final var instance = runBatch(test -> test.setBlowUpInInFindAllEntity(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
  }
  
  
  @Test
  public void testBatchDurability_setBlowUpOnSecondEntity() {
    final var instance = runBatch(test -> test.setBlowUpOnSecondEntity(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
  }
  
  
  @Test
  public void testBatchDurability_setBlowUpOnSecondEntityStream() {
    final var instance = runBatch(test -> test.setBlowUpOnSecondEntityStream(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    // some failures do not cause the whole batch to fail
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
    
    
    Assertions.assertEquals(14, instance.getTransitives().getStepRows().size());
    
    // 1 row failed
    Assertions.assertEquals(1, instance.getTransitives().getStepRows().stream()
        .filter(e -> e.getExecutionStatus() == RuntimeExecutionStatus.ERROR).count());
  }
  
  
  
  @Test
  public void testBatchDurability_blowUpInAfter() {
    final var instance = runBatch(test -> test.setBlowUpInAfter(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
    
    
    Assertions.assertEquals(9, instance.getTransitives().getStepRows().size());
    
    // 1 row failed
    Assertions.assertEquals(0, instance.getTransitives().getStepRows().stream()
        .filter(e -> e.getExecutionStatus() == RuntimeExecutionStatus.ERROR).count());
  }
  @Test
  public void testBatchDurability_blowUpInAfterStream() {
    final var instance = runBatch(test -> test.setBlowUpInAfterStream(true));
    
    // completed with error
    Assertions.assertEquals(RuntimeStatus.COMPLETED, instance.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getExecutionStatus());
    
    // two steps in total
    Assertions.assertEquals(2, instance.getTransitives().getSteps().size());

    // blew up @ step 1
    Assertions.assertEquals(RuntimeExecutionStatus.ERROR, instance.getTransitives().getSteps().get(0).getExecutionStatus());    

    // second step was skipped
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instance.getTransitives().getSteps().get(1).getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, instance.getTransitives().getSteps().get(1).getExecutionStatus());
    
    
    Assertions.assertEquals(9, instance.getTransitives().getStepRows().size());
    
    // 1 row failed
    Assertions.assertEquals(0, instance.getTransitives().getStepRows().stream()
        .filter(e -> e.getExecutionStatus() == RuntimeExecutionStatus.ERROR).count());
  }
  
  
  private RuntimeInstance runBatch(Consumer<DurabilityStep1> durability) {
    durability.accept(duability.reset());
    
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
      .build().await().atMost(atMost);
    Assertions.assertEquals(OperationStatus.OK, instance.getOperationStatus());
    
    envir
      .executor()
      .commitMessage("test has started")
      .commitAuthor("durability@durability.io")
      .execute(instance.getObject())
      .await().atMost(atMost);
    
    
    return client.queryRuntimeInstances()
        .findAll().await().atMost(atMost)
        .getObject().stream()
        .filter(t -> t.getId().equals(instance.getObject().getId()))
        .findFirst().get();
    
  }
}
