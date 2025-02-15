package io.digiexpress.mig.client.test;

import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.digiexpress.mig.client.api.ImmutableFormFilter;
import io.digiexpress.mig.client.spi.MigClientImpl;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;


//@Disabled
public class RunAgainstTestDb {
  final io.vertx.mutiny.pgclient.PgPool src_pg_pool = io.vertx.mutiny.pgclient.PgPool.pool(
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5462)
        .setDatabase("mig-data")
        .setUser("mig-data")
        .setPassword("password123"), 
      new PoolOptions().setMaxSize(5));
  final io.vertx.mutiny.pgclient.PgPool target_pg_pool = io.vertx.mutiny.pgclient.PgPool.pool(
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5436)
        .setDatabase("dialob")
        .setUser("dialob")
        .setPassword("dialob123"), 
      new PoolOptions().setMaxSize(5));
  
  
  @Test
  public void test() {
    final var client = new MigClientImpl(src_pg_pool, src_pg_pool, target_pg_pool);
    
    // get all tasks
    final var tasks = client.taskQuery().findAll().await().atMost(Duration.ofMinutes(1));
    
    // get all dialob related data
    final var dialob = client.dialobQuery()
      .includeFromQuestionnaires(
          tasks.getProcesses().values().stream()
          .map(e -> e.getQuestionnaire_id())
          .filter(e -> e.isPresent())
          .map(e -> e.get())
          .toList()
      )
      .includeFrom(tasks.getWorkflows().values().stream()
          .map(e -> ImmutableFormFilter.builder()
              .formId(e.getForm_id())
              .formName(e.getForm_name())
              .formTag(e.getForm_tag())
              .build())
          .toList()
      )
      .findAll().await().atMost(Duration.ofMinutes(1));
    
    
    
    client.dialobBuilder().build(dialob)
      .await().atMost(Duration.ofMinutes(10));
  }
}
