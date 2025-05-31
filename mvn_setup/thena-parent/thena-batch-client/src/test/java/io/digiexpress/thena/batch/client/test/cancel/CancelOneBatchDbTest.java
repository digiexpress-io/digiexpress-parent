package io.digiexpress.thena.batch.client.test.cancel;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.test.config.DbTestTemplate;
import io.digiexpress.thena.batch.client.test.config.PgProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class CancelOneBatchDbTest extends DbTestTemplate {
  final String batchName = "run-all-dialob-related-tasks";
  final CancelStep1 closeDialobs = new CancelStep1();
  BatchClient client;
  
  public void setUp() {
    final TenantCommitResult repo = getClient().manageTenants().commit()
        .name("CancelOneBatchDbTest")
        .build()
        .await().atMost(atMost);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    client = getClient().withTenant(repo.getRepo().getId());
  }
    
  @Test
  public void CreateAndCancelOneBatch() {
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
    
    
    new Thread(() -> {

      //closeDialobs.setEnvir(envir);
      
      while(closeDialobs.index.get() < 3) {
        log.debug("Waiting for increment, current: {}", closeDialobs.index.get());
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      
      envir.kill()
        .commitAuthor("jane.doe@batches.io")
        .commitMessage("Ooops :(")
        .killAll().await().atMost(Duration.ofMinutes(1));
      
    }).start();
    
    
    final var done = envir
      .executor()
      .commitMessage("test has started")
      .commitAuthor("junit")
      .execute(instance.getObject())
      .await().atMost(Duration.ofMinutes(1));
    
    
    Assertions.assertNotNull(done.getEndedAt());
    Assertions.assertEquals(RuntimeStatus.CANCELLED, done.getStatus());
    Assertions.assertEquals(RuntimeExecutionStatus.OK, done.getExecutionStatus());
    
    
    final var instances = client.queryRuntimeInstances().findAll().await().atMost(atMost).getObject();
    Assertions.assertEquals(1, instances.size());
    Assertions.assertEquals(RuntimeStatus.CANCELLED, instances.get(0).getStatus());
    
    Assertions.assertEquals(2, instances.get(0).getTransitives().getSteps().size());
    Assertions.assertEquals(RuntimeStatus.CANCELLED, instances.get(0).getTransitives().getSteps().get(0).getStatus());
    Assertions.assertEquals(RuntimeStatus.SKIPPED, instances.get(0).getTransitives().getSteps().get(1).getStatus());
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
          .build(new CancelStep2()))
      .commitAuthor("junitTest")
      .commitMessage("create batch with 2 consumers")
      .build()
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(OperationStatus.OK, config.getOperationStatus());
    
    final var envir = config.getObject();
    return envir;
  }
}
