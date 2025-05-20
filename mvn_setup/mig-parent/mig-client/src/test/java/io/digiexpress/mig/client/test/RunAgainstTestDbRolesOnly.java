package io.digiexpress.mig.client.test;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.digiexpress.mig.client.api.ImmutableFormFilter;
import io.digiexpress.mig.client.spi.MigClientImpl;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;


//@Disabled
public class RunAgainstTestDbRolesOnly {
  final io.vertx.mutiny.pgclient.PgPool src_pg_pool = io.vertx.mutiny.pgclient.PgPool.pool(
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5462)
        .setDatabase("mig-data")
        .setUser("mig-data")
        .setPassword("password123"), 
      new PoolOptions().setMaxSize(5));
  final io.vertx.mutiny.pgclient.PgPool target_task_pg_pool = io.vertx.mutiny.pgclient.PgPool.pool(
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5433)
        .setDatabase("eveli-app")
        .setUser("eveli-app")
        .setPassword("password123")
        , 
      new PoolOptions().setMaxSize(10));
  
  final io.vertx.mutiny.pgclient.PgPool target_dialob_pg_pool = null;

  
  @Test
  public void test() {
    final var client = new MigClientImpl(
        src_pg_pool, src_pg_pool, src_pg_pool, src_pg_pool,
        target_dialob_pg_pool, target_task_pg_pool);
    
    // convert tasks
    final var tasks = client.taskQuery().findAll().await().atMost(Duration.ofMinutes(10));
    client.taskRolesBuilder().build(tasks, "TASK_TENAN13_").await().atMost(Duration.ofMinutes(10));
  
    
  }
}
