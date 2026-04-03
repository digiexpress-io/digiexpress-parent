package io.digiexpress.eveli.mig;


import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.mig.v6.V6Migration;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public class V6Runner {
  static {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

  }
  
  
  @Test
  public void vs6runner() {
    final io.vertx.sqlclient.Pool pool_target = PgBuilder.pool().connectingTo(
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5433)
        .setDatabase("eveli-app")
        .setUser("eveli-app")
        .setPassword("password123")
      )
      .with(new PoolOptions().setMaxSize(5))
      .build();
    
    
    final io.vertx.sqlclient.Pool pool_src = PgBuilder.pool().connectingTo(
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5462)
        .setDatabase("mig-data")
        .setUser("mig-data")
        .setPassword("password123")
      )
      .with(new PoolOptions().setMaxSize(5))
      .build();
    
    final var mig = new V6Migration(
        new io.vertx.mutiny.sqlclient.Pool(pool_src),
        new io.vertx.mutiny.sqlclient.Pool(pool_target)
      )
      .stencil("stencil-assets")
      .wrench("wrench-assets")
      .envir("envir")
      .fs("assets")
      .execute().await().atMost(Duration.ofMinutes(1));
  }
}
