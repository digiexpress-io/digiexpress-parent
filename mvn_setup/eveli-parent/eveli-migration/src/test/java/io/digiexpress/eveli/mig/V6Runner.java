package io.digiexpress.eveli.mig;


import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.mig.v6.assets.V6Migration;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public class V6Runner {
  
  @Test
  public void xxx() {

    final io.vertx.sqlclient.Pool pool = PgBuilder.pool().connectingTo(
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5433)
        .setDatabase("eveli-app")
        .setUser("eveli-app")
        .setPassword("password123")
      )
      .with(new PoolOptions().setMaxSize(5))

      .build();
    
    
    final var mig = new V6Migration(new io.vertx.mutiny.sqlclient.Pool(pool))
        .stencil("stencil-assets")
        .envir("envir")
        .execute()
        .await().atMost(Duration.ofMinutes(1));
    
  }
  
}
