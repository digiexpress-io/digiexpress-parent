package io.digiexpress.thena.batch.client.test;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
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
public class CreateOneBatchDbTest extends DbTestTemplate {
  final String batchName = "run-all-dialob-related-tasks";
  final ExecutorForClosingOldDialobs closeDialobs = new ExecutorForClosingOldDialobs();
  BatchClient client;
  
  public void setUp() {
    final TenantCommitResult repo = getClient().manageTenants().commit()
        .name("create-one-batch")
        .build()
        .await().atMost(atMost);
    Assertions.assertEquals(CommitStatus.OK, repo.getStatus());
    client = getClient().withTenant("create-one-batch");
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
    
    
    new Thread(() -> {

      //closeDialobs.setEnvir(envir);
      
      while(closeDialobs.index.get() < 3) {
        log.debug("Waiting for increment, current: {}", closeDialobs.index.get());
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      
      envir.kill()
        .commitAuthor("jane.doe@batches.io")
        .commitMessage("Ooops :(")
        .killAll().await().atMost(Duration.ofMinutes(1));
      
    }).start();
    
    
    envir
      .executor()
      .commitMessage("test has started")
      .commitAuthor("junit")
      .execute(instance.getObject())
      .await().atMost(Duration.ofMinutes(1));
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
          .build(new ExecutorForReportingOldDialobs()))
      .commitAuthor("junitTest")
      .commitMessage("create batch with 2 consumers")
      .build()
      .await().atMost(Duration.ofMinutes(1));
    Assertions.assertEquals(OperationStatus.OK, config.getOperationStatus());
    
    final var envir = config.getObject();
    
    // before
    
    // Promise<iterator>
    
    // after
    return envir;
  }
}
